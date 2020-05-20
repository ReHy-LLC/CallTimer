package com.rehyapp.calltimer

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.rehyapp.calltimer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_SET_DEFAULT_DIALER = 3644
        private const val LOG_TAG = "ActivityMain"
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                }.show()
        }
    }

    private fun requestDialerRole() {
        if (Build.VERSION.SDK_INT >= 29) {
            val roleManager = getSystemService(RoleManager::class.java)
            val intent = roleManager?.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
        } else {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
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
