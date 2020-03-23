package com.rehyapp.calltimer

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_SET_DEFAULT_DIALER = 3644
        private const val REQUEST_CODE_PERMISSIONS_ALL = 11
        private const val LOG_TAG = "CallActivity"
    }

    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fab = findViewById(R.id.dial_fab)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_recents, R.id.navigation_contacts,
                R.id.navigation_timer, R.id.navigation_usage))
        setupActionBarWithNavController(navController, appBarConfiguration)*/
        navView.setupWithNavController(navController)
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
        if (isAlreadyDefaultDialer) {
            fab.visibility = View.VISIBLE
            return
        }

        Snackbar.make(findViewById(android.R.id.content),
                getString(R.string.snack_text_permissions), Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.grant)) {
                requestDialerRole()
            }.show()
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
        var message = getString(R.string.toast_accepted)
        when (resultCode) {
            RESULT_OK       -> {
                fab.visibility = View.VISIBLE
                if (!hasCallLogPermission() || !hasContactsPermission() || !hasMakeCallPermission()
                    || !hasVibratePermission() || !hasWriteCallLogPermission()) {
                    ActivityCompat.requestPermissions(
                        this
                        , arrayOf(
                            Manifest.permission.READ_CALL_LOG
                            , Manifest.permission.WRITE_CALL_LOG
                            , Manifest.permission.CALL_PHONE
                            , Manifest.permission.VIBRATE
                            , Manifest.permission.READ_CONTACTS
                        ), REQUEST_CODE_PERMISSIONS_ALL
                    )
                }
            }
            RESULT_CANCELED -> message = getString(R.string.toast_declined)
            else            -> message = "Unexpected result code $resultCode"
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun hasContactsPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasCallLogPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasWriteCallLogPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.WRITE_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasVibratePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.VIBRATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasMakeCallPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSIONS_ALL -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(
                        applicationContext, "Thanks for permission!"
                        , Toast.LENGTH_SHORT
                    ).show()
                } else {
                    when {
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE) -> {
                            Toast.makeText(applicationContext, getString(R.string.toast_declined_call_permission), Toast.LENGTH_LONG).show()
                        }
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALL_LOG) -> {
                            Toast.makeText(applicationContext, getString(R.string.call_log_denied_toast), Toast.LENGTH_LONG).show()
                        }
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALL_LOG) -> {
                            Toast.makeText(applicationContext, getString(R.string.call_log_denied_toast), Toast.LENGTH_LONG).show()
                        }
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.VIBRATE) -> {
                            Toast.makeText(applicationContext, getString(R.string.vibrate_denied_toast), Toast.LENGTH_LONG).show()
                        }
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS) -> {
                            Toast.makeText(applicationContext, getString(R.string.contacts_denied_toast), Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            goToSettings()
                        }
                    }
                }
            }
        }
    }

    private fun goToSettings() {
        val myAppSettings = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:$packageName")
        )
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
        myAppSettings.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivityForResult(myAppSettings, 5)
    }
}
