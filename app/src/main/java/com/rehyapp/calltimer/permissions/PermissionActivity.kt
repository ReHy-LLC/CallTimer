package com.rehyapp.calltimer.permissions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

abstract class PermissionActivity : AppCompatActivity() {

    abstract fun onPermissionsGranted(businessRequest: BusinessRequest)

    private lateinit var permissionManager: PermissionManager
    private lateinit var businessRequest: BusinessRequest
    private var isComingFromSettings = false
    private var exitOnDenied = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissionListener = object : PermissionListener {
            override fun onNativePermissionRequestReady(
                permissions: Array<String>,
                requestCode: Int
            ) {
                ActivityCompat.requestPermissions(this@PermissionActivity, permissions, requestCode)
            }

            override fun grantPermissions(businessRequest: BusinessRequest) {
                onPermissionsGranted(businessRequest)
            }

            override fun navigateToSettings() {
                isComingFromSettings = true
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null)
                )
                startActivity(intent)
            }

            override fun deny() {
                onPermissionsDenied()
            }

            override fun retry() {
                checkPermissionsAreGranted(businessRequest, exitOnDenied)
            }
        }

        permissionManager = PermissionManager(permissionListener)
    }

    fun checkPermissionsAreGranted(
        businessRequest: BusinessRequest,
        exitOnDenied: Boolean = false
    ) {
        this.businessRequest = businessRequest
        this.exitOnDenied = exitOnDenied
        if (::permissionManager.isInitialized) {
            val permissionRequests =
                permissionManager.getPermissionRequestsFromBusiness(businessRequest)

            if (permissionManager.areAllPermissionsGranted(this, permissionRequests)) {
                onPermissionsGranted(businessRequest)
            } else {
                permissionManager.prepareNativePermissionRequest(permissionRequests)
            }
        }
    }

    @CallSuper
    open fun onPermissionsDenied() {
        if (exitOnDenied) {
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (::businessRequest.isInitialized) {
            permissionManager.handleRequestPermissionsResult(
                this,
                businessRequest,
                permissions,
                grantResults
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (isComingFromSettings && ::businessRequest.isInitialized) {
            isComingFromSettings = false
            permissionManager.handleResume(this)
        }
    }
}