package com.rehyapp.calltimer.permissions

import android.Manifest

enum class PermissionRequest(val request: Int) {
    CONTACTS(1),
    CALL_LOGS(2),
    MAKE_CALLS(3);

    fun getPermissionTextArray(): Array<String> {
        return when (this) {
            CONTACTS -> arrayOf(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS
            )
            CALL_LOGS -> arrayOf(
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG
            )
            MAKE_CALLS -> arrayOf(Manifest.permission.CALL_PHONE)
        }
    }

    companion object {
        fun getPermissionRequestFromText(permission: String): PermissionRequest? {
            return when (permission) {
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS -> CONTACTS
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG -> CALL_LOGS
                Manifest.permission.CALL_PHONE -> MAKE_CALLS
                else -> null
            }
        }
    }
}