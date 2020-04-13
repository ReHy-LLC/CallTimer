package com.rehyapp.calltimer.calllogging

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.CallLog

@SuppressLint("MissingPermission")
class LogManager(_context: Context) {

    private var context: Context = _context

    fun getCallById(callId: Int): LogObject {

        val selection = CallLog.Calls._ID + " = " + callId

        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI
            , null
            , selection
            , null
            , null
        )!!

        val logObject = createLogObjectFromCursor(cursor)

        cursor.close()

        return logObject

    }

    fun getCallsLogsByType(callType: Int): List<LogObject> {

        val selection = CallLog.Calls.TYPE + " = " + callType

        return getCallLogList(selection)

    }

    fun getCallLogsAll(): MutableList<LogObject> {

        //empty selection to get unfiltered call log list
        return getCallLogList("")

    }

    private fun getCallLogList(selection: String): MutableList<LogObject> {

        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI
            , null
            , selection
            , null
            , null
        )!!

        val logList = mutableListOf<LogObject>()

        while (cursor.moveToNext()) {
            val logObject = createLogObjectFromCursor(cursor)
            logList.add(logObject)
        }

        cursor.close()

        return logList

    }

    private fun createLogObjectFromCursor(cursor: Cursor): LogObject {
        val logObject = LogObject()

        //Min SDK is 23 so always set values for everything up to API 23
        logObject.callId = cursor.getLong(cursor.getColumnIndex(CallLog.Calls._ID))
        logObject.type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))
        logObject.date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE))
        logObject.duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION))
        logObject.new = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.NEW))
        logObject.number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
        logObject.isRead = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.IS_READ))
        logObject.numberPresentation =
            cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER_PRESENTATION))
        logObject.countryISO = cursor.getString(cursor.getColumnIndex(CallLog.Calls.COUNTRY_ISO))
        logObject.dataUsage = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATA_USAGE))
        logObject.geoCodedLocation =
            cursor.getString(cursor.getColumnIndex(CallLog.Calls.GEOCODED_LOCATION))
        logObject.phoneAccountComponentName =
            cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_COMPONENT_NAME))
        logObject.phoneAccountId =
            cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))

        //Only pull if SDK >= 24, fields didn't exist until API 24.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            logObject.postDialDigits =
                cursor.getString(cursor.getColumnIndex(CallLog.Calls.POST_DIAL_DIGITS))
            logObject.viaNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.VIA_NUMBER))
        }

        //Only pull if SDK >= 29, fields didn't exist until API 24.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            logObject.blockReason = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.BLOCK_REASON))
            logObject.callScreeningAppName =
                cursor.getString(cursor.getColumnIndex(CallLog.Calls.CALL_SCREENING_APP_NAME))
            logObject.callScreeningComponentName =
                cursor.getString(cursor.getColumnIndex(CallLog.Calls.CALL_SCREENING_COMPONENT_NAME))
        }

        return logObject
    }

}