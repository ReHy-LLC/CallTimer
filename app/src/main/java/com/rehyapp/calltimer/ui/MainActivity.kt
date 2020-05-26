package com.rehyapp.calltimer.ui

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_SET_DEFAULT_DIALER = 3644
        private const val LOG_TAG = "ActivityMain"
    }

    //koin dependency injected ViewModel
    private val sharedViewModel by viewModel<MainSharedViewModel>()

    //init in onCreateView
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //set data binding vars
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = sharedViewModel

        //setup recents recycler switch
        setupRecentsRecyclerSwitch()

        setSupportActionBar(binding.mainToolbar)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        val navView: BottomNavigationView = binding.navViewNav
        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        super.onPostCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        checkDefaultDialer()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val menuItem = menu?.findItem(R.id.main_menu_recents_clear_all)
        menuItem?.isVisible = sharedViewModel.activityIsRecentsFragShowing.value ?: false
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.main_menu_settings -> Toast.makeText(
                this,
                "Will open settings activity.",
                Toast.LENGTH_SHORT
            ).show()
            R.id.main_menu_recents_clear_all -> {
                this.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setTitle(getString(R.string.title_clear_all_calls_dialog))
                        setMessage(getString(R.string.message_clear_all_calls_dialog))
                        setIcon(getDrawable(R.drawable.ic_alert))
                        setPositiveButton(getString(R.string.ok)) { _, _ -> sharedViewModel.recentsDeleteAllCallLogs() }
                        setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                        setCancelable(true)
                        setFinishOnTouchOutside(true)
                    }.create()
                }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecentsRecyclerSwitch() {
        //handle switch changes for filtered recycler
        binding.recentsRecyclerSwitch.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked && checkedId == R.id.btnAll) {
                //show all if set to all via switch
                sharedViewModel.recentsShowAllCallLogs()
            } else if (!isChecked && checkedId == R.id.btnAll) {
                //if nothing is checked now but all was checked then check missed
                group.check(R.id.btnMissed)
            } else if (isChecked && checkedId == R.id.btnMissed) {
                //if missed is checked then filter recycler to missed logs only
                sharedViewModel.recentsShowMissedCallLogs()
            } else if (!isChecked && checkedId == R.id.btnMissed) {
                //if nothing is checked but missed was checked then check all
                group.check(R.id.btnAll)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SET_DEFAULT_DIALER -> checkSetDefaultDialerResult(resultCode)
        }
    }

    private fun checkDefaultDialer() {
        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
        val isAlreadyDefaultDialer = packageName == telecomManager.defaultDialerPackage
        if (!isAlreadyDefaultDialer) {
            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.snack_text_permissions), Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.grant)) {
                    requestDialerRole()
                }.setAnchorView(binding.fab).show()
        }
    }

    private fun requestDialerRole() {
        if (Build.VERSION.SDK_INT >= 29) {
            val roleManager = getSystemService(RoleManager::class.java)
            val intent = roleManager?.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            startActivityForResult(
                intent,
                REQUEST_CODE_SET_DEFAULT_DIALER
            )
        } else {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            startActivityForResult(
                intent,
                REQUEST_CODE_SET_DEFAULT_DIALER
            )
        }
    }

    private fun checkSetDefaultDialerResult(resultCode: Int) {
        val message = when (resultCode) {
            RESULT_OK -> getString(R.string.toast_accepted)
            RESULT_CANCELED -> getString(R.string.toast_declined)
            else -> "Unexpected result code $resultCode"
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
