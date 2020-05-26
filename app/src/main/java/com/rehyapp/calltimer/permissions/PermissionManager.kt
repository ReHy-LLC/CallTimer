package com.rehyapp.calltimer.permissions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.rehyapp.calltimer.R

class PermissionManager(private val permissionListener: PermissionListener) {

    fun getPermissionRequestsFromBusiness(businessRequest: BusinessRequest): Array<PermissionRequest> {
        return when (businessRequest) {
            BusinessRequest.READ_WRITE_CONTACTS -> arrayOf(PermissionRequest.CONTACTS)
            BusinessRequest.READ_WRITE_CALL_LOGS -> arrayOf(PermissionRequest.CALL_LOGS)
            BusinessRequest.MAKE_CALLS -> arrayOf(PermissionRequest.MAKE_CALLS)
            BusinessRequest.CALLS_CALL_LOGS_CONTACTS -> arrayOf(
                PermissionRequest.MAKE_CALLS,
                PermissionRequest.CALL_LOGS,
                PermissionRequest.CONTACTS
            )
        }
    }

    fun areAllPermissionsGranted(
        context: Context,
        permissionRequests: Array<out PermissionRequest>
    ): Boolean {
        return permissionRequests.all { permissionRequest ->
            permissionRequest.getPermissionTextArray().all { permission ->
                val permissionStatus = ContextCompat.checkSelfPermission(context, permission)
                permissionStatus == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    fun prepareNativePermissionRequest(permissionRequests: Array<out PermissionRequest>) {
        val permissions = getPermissions(permissionRequests)
        val requestCode = getRequestCode(permissionRequests)
        permissionListener.onNativePermissionRequestReady(permissions, requestCode)
    }

    private fun getPermissions(permissionRequests: Array<out PermissionRequest>): Array<String> {
        val permissions = ArrayList<String>()

        permissionRequests.forEach { permissionRequest ->
            val permissionTexts = permissionRequest.getPermissionTextArray()
            permissions.addAll(permissionTexts)
        }

        return permissions.toTypedArray()
    }

    private fun getRequestCode(permissionRequests: Array<out PermissionRequest>) =
        permissionRequests.sumBy { it.request }

    @StringRes
    private fun getPermissionTitle(businessRequest: BusinessRequest): Int {
        return when (businessRequest) {
            BusinessRequest.MAKE_CALLS -> R.string.permission_title_make_calls
            BusinessRequest.READ_WRITE_CALL_LOGS -> R.string.permission_title_read_write_call_log
            BusinessRequest.READ_WRITE_CONTACTS -> R.string.permission_title_read_write_contacts
            BusinessRequest.CALLS_CALL_LOGS_CONTACTS -> R.string.permission_title_calls_call_logs_contacts
        }
    }

    private fun getMessageForRationale(context: Context, businessRequest: BusinessRequest): String {
        return when (businessRequest) {
            BusinessRequest.MAKE_CALLS -> context.getString(R.string.permission_rationale_make_calls)
            BusinessRequest.READ_WRITE_CONTACTS -> context.getString(R.string.permission_rationale_read_write_contacts)
            BusinessRequest.READ_WRITE_CALL_LOGS -> context.getString(R.string.permission_rationale_read_write_call_logs)
            BusinessRequest.CALLS_CALL_LOGS_CONTACTS -> context.getString(R.string.permission_rationale_calls_call_logs_contacts)
        }
    }

    private fun getMessageForPermanentDenial(
        context: Context,
        businessRequest: BusinessRequest,
        deniedPermissions: ArrayList<String>
    ): String {
        return when (businessRequest) {
            BusinessRequest.MAKE_CALLS -> context.getString(R.string.permission_permanent_denial_make_calls)
            BusinessRequest.READ_WRITE_CALL_LOGS -> context.getString(R.string.permission_permanent_denial_read_write_call_logs)
            BusinessRequest.READ_WRITE_CONTACTS -> context.getString(R.string.permission_permanent_denial_read_write_contacts)
            BusinessRequest.CALLS_CALL_LOGS_CONTACTS -> context.getString(R.string.permission_permanent_denial_calls_call_logs_contacts)
        }
    }

    fun handleRequestPermissionsResult(
        activity: Activity,
        businessRequest: BusinessRequest,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val deniedPermissions = ArrayList<String>()
        var areAllPermissionsGranted = true
        grantResults.indices.forEach { i ->
            val grantResult = grantResults[i]
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                areAllPermissionsGranted = false
                deniedPermissions.add(permissions[i])
            }
        }

        if (areAllPermissionsGranted) {
            permissionListener.grantPermissions(businessRequest)
        } else {
            val shouldShowRationale = deniedPermissions.any { deniedPermission ->
                ActivityCompat.shouldShowRequestPermissionRationale(activity, deniedPermission)
            }

            if (shouldShowRationale) {
                showPermissionRationaleDialog(activity, businessRequest)
            } else {
                showPermissionPermanentDenialDialog(activity, businessRequest, deniedPermissions)
            }
        }
    }

    private fun showPermissionRationaleDialog(
        activity: Activity,
        businessRequest: BusinessRequest
    ) {
        if (!activity.isFinishing) {
            val title = getPermissionTitle(businessRequest)
            val message = getMessageForRationale(activity, businessRequest)
            activity.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setTitle(title)
                    setMessage(message)
                    setCancelable(false)
                    setPositiveButton(R.string.retry) { _, _ ->
                        permissionListener.retry()
                    }
                    setNegativeButton(R.string.cancel) { dialog, _ ->
                        if (!activity.isFinishing) {
                            dialog.dismiss()
                            permissionListener.deny()
                        }
                    }
                }.create()
            }.show()
        }
    }

    private fun showPermissionPermanentDenialDialog(
        activity: Activity,
        businessRequest: BusinessRequest,
        deniedPermissions: ArrayList<String>
    ) {
        if (!activity.isFinishing) {
            val title = getPermissionTitle(businessRequest)
            val message = getMessageForPermanentDenial(activity, businessRequest, deniedPermissions)

            activity.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setTitle(title)
                    setMessage(message)
                    setCancelable(false)
                    setPositiveButton(R.string.settings) { _, _ ->
                        permissionListener.navigateToSettings()
                    }
                    setNegativeButton(R.string.cancel) { dialog, _ ->
                        if (!activity.isFinishing) {
                            dialog.dismiss()
                            permissionListener.deny()
                        }
                    }
                }.create()
            }.show()
        }
    }

    fun handleResume(activity: Activity) {
        if (!activity.isFinishing) {
            activity.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setTitle(R.string.retry)
                    setMessage(R.string.retry_business)
                    setCancelable(false)
                    setPositiveButton(R.string.yes) { _, _ ->
                        permissionListener.retry()
                    }
                    setNegativeButton(R.string.no) { dialog, _ ->
                        if (!activity.isFinishing) {
                            dialog.dismiss()
                            permissionListener.deny()
                        }
                    }
                }.create()
            }.show()
        }
    }
}