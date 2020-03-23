package com.rehyapp.calltimer.in_call_utils

import android.util.Log

object Constants {

    private const val LOG_TAG = "CallActivity"

    fun asString(data: Int): String {
        return when (data) {
            0 -> "NEW"
            1 -> "DIALING"
            2 -> "RINGING"
            3 -> "HOLDING"
            4 -> "ACTIVE"
            7 -> "DISCONNECTED"
            8 -> "SELECT_PHONE_ACCOUNT"
            9 -> "CONNECTING"
            10 -> "DISCONNECTING"
            else -> {
                Log.w(LOG_TAG,"Unknown state $data")
                "UNKNOWN"
            }
        }
    }
}