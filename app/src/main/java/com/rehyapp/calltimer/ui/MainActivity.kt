package com.rehyapp.calltimer.ui

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
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

        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

        // Attach a callback used to capture the shared elements from this Activity to be used
        // by the container transform transition
        setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        // Keep system bars (status bar, navigation bar) persistent throughout the transition.
        window.sharedElementsUseOverlay = false

        super.onCreate(savedInstanceState)

        //set data binding vars
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = sharedViewModel

        //observe changes to new call count to update badge -- refreshes in onResume
        sharedViewModel.recentsUnreadMissedCount.observe(this, {

            if (it > 0) {

                binding.navViewNav.getOrCreateBadge(R.id.navigation_recents).number = it

            } else {

                binding.navViewNav.removeBadge(R.id.navigation_recents)

            }

        })

        //setup recents recycler switch
        setupRecentsRecyclerSwitch()

        setSupportActionBar(binding.mainToolbar)

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {

        super.onPostCreate(savedInstanceState)

        val navView: BottomNavigationView = binding.navViewNav
        val navController = findNavController(R.id.nav_host_fragment)

        navView.setupWithNavController(navController)
        navView.setOnNavigationItemReselectedListener { }

    }

    override fun onResume() {

        super.onResume()

        checkDefaultDialer()

        if (sharedViewModel.activityIsDefaultDialer.value == true && sharedViewModel.recentsHasPermissions.value == true) {

            sharedViewModel.getNewMissedCallCount()

        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)

        return super.onCreateOptionsMenu(menu)

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        val menuItem = menu?.findItem(R.id.main_menu_recents_clear_all)

        menuItem?.isVisible = (sharedViewModel.activityIsRecentsFragShowing.value!!
                && sharedViewModel.recentsHasPermissions.value!!
                && sharedViewModel.recentsHasLogsToShow.value!!)

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
                        setIcon(
                            ContextCompat.getDrawable(
                                it.applicationContext,
                                R.drawable.ic_alert
                            )
                        )
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

    private fun setupRecentsRecyclerSwitch() =

        //handle switch changes for filtered recycler
        binding.recentsRecyclerSwitch.addOnButtonCheckedListener { group, checkedId, isChecked ->

            when {

                //show all if set to all via switch
                isChecked && checkedId == R.id.btnAll -> sharedViewModel.recentsShowAllCallLogs()

                //if nothing is checked now but all was checked then check missed
                !isChecked && checkedId == R.id.btnAll -> group.check(R.id.btnMissed)

                //if missed is checked then filter recycler to missed logs only
                isChecked && checkedId == R.id.btnMissed -> sharedViewModel.recentsShowMissedCallLogs()

                //if nothing is checked but missed was checked then check all
                !isChecked && checkedId == R.id.btnMissed -> group.check(R.id.btnAll)

            }

        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) checkRequestDialerRoleResult(resultCode)

    }

    private fun checkDefaultDialer() {

        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
        val isAlreadyDefaultDialer = packageName == telecomManager.defaultDialerPackage

        if (isAlreadyDefaultDialer) {

            sharedViewModel.activitySetIsDefaultDialer(true)

        } else {

            Snackbar.make(
                findViewById(android.R.id.content),
                getString(R.string.snack_text_permissions),
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(getString(R.string.grant)) {
                    requestDialerRole()
                }.setAnchorView(binding.fab).show()

        }

    }

    private fun requestDialerRole() =

        if (Build.VERSION.SDK_INT >= 29) {

            val roleManager = getSystemService(RoleManager::class.java)
            val intent = roleManager?.createRequestRoleIntent(RoleManager.ROLE_DIALER)

            startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)

        } else {

            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)

            startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)

        }

    private fun checkRequestDialerRoleResult(resultCode: Int) {

        val message: String

        when (resultCode) {

            RESULT_OK -> {
                message = getString(R.string.toast_accepted)
                sharedViewModel.activitySetIsDefaultDialer(true)
            }

            RESULT_CANCELED -> message = getString(R.string.toast_declined)

            else -> message = "Unexpected result code $resultCode"

        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    }

}
