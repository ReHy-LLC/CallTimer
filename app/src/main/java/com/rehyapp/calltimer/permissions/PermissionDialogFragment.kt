package com.rehyapp.calltimer.permissions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment

abstract class PermissionDialogFragment : DialogFragment() {

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
                requestPermissions(permissions, requestCode)
            }

            override fun grantPermissions(businessRequest: BusinessRequest) {
                onPermissionsGranted(businessRequest)
            }

            override fun navigateToSettings() {
                activity?.let {
                    isComingFromSettings = true
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", it.packageName, null)
                    )
                    startActivity(intent)
                }
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

            context?.let {
                if (permissionManager.areAllPermissionsGranted(it, permissionRequests)) {
                    onPermissionsGranted(businessRequest)
                } else {
                    permissionManager.prepareNativePermissionRequest(permissionRequests)
                }
            }
        }
    }

    @CallSuper
    open fun onPermissionsDenied() {
        if (exitOnDenied) {
            dismiss()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        activity?.let {
            if (::businessRequest.isInitialized) {
                permissionManager.handleRequestPermissionsResult(
                    it,
                    businessRequest,
                    permissions,
                    grantResults
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            if (isComingFromSettings && ::businessRequest.isInitialized) {
                isComingFromSettings = false
                permissionManager.handleResume(it)
            }
        }
    }
}