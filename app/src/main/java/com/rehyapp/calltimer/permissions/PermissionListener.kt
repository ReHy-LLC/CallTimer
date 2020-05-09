package com.rehyapp.calltimer.permissions

interface PermissionListener {

    fun onNativePermissionRequestReady(permissions: Array<String>, requestCode: Int)

    fun grantPermissions(businessRequest: BusinessRequest)

    fun navigateToSettings()

    fun deny()

    fun retry()
}