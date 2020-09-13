package com.rehyapp.calltimer.call_logging

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.text.TextUtils
import com.rehyapp.calltimer.R
import java.text.DateFormat
import java.util.*


@SuppressLint("MissingPermission")
class LogManager(_context: Context) {

    private var context: Context = _context
    private var logTag = "LogManager"

    fun getCallById(callId: Int): LogObjectRaw {

        val logObject: LogObjectRaw
        var cursor: Cursor? = null
        val selection = CallLog.Calls._ID + " = " + callId

        try {
            cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null,
                selection,
                null,
                null
            )

            logObject = createRawLogObjectFromCursor(cursor!!)
        } finally {
            cursor?.close()
        }
        return logObject

    }

    fun getCallsLogsByType(callType: Int): MutableList<LogObjectRaw> {

        val selection = CallLog.Calls.TYPE + " = " + callType

        return getCallLogList(selection)

    }

    fun getCallLogsAll(): MutableList<LogObjectRaw> {

        //empty selection to get unfiltered call log list
        return getCallLogList("")

    }

    fun deleteLogFromRecentsObject(recentsUIGroupingsObject: RecentsUIGroupingsObject) {
        recentsUIGroupingsObject.groupCallIds.forEach {
            context.contentResolver.apply {
                delete(
                    CallLog.Calls.CONTENT_URI,
                    CallLog.Calls._ID.plus("=?"),
                    arrayOf(it.toString())
                )
            }
        }

    }

    fun canShowCallLogList(selection: String): Boolean {

        var canShow = false

        var cursor: Cursor? = null

        try {

            cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null,
                selection,
                null,
                null
            )

            cursor!!.moveToFirst()

            if (cursor.count > 0) {
                canShow = true
            }

        } finally {
            cursor?.close()
        }

        return canShow
    }

    private fun getCallLogList(selection: String): MutableList<LogObjectRaw> {

        var cursor: Cursor? = null
        val logList = mutableListOf<LogObjectRaw>()

        try {
            cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                null,
                selection,
                null,
                null
            )

            if (cursor!!.count > 0) {
                while (cursor.moveToNext()) {
                    val logObject = createRawLogObjectFromCursor(cursor)
                    logList.add(logObject)
                }
            }
        } finally {
            cursor?.close()
        }

        return logList.asReversed()

    }

    private fun createRawLogObjectFromCursor(cursor: Cursor): LogObjectRaw {
        val logObject = LogObjectRaw()

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
        logObject.features = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.FEATURES))

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

    fun convertToRecentsUIGroupings(rawLogList: MutableList<LogObjectRaw>): MutableList<RecentsUIGroupingsObject> {

        //empty group list to add to as we loop raw list
        val recentsUIGroupedList = mutableListOf<RecentsUIGroupingsObject>()

        //if no items then just return header
        if (rawLogList.isNullOrEmpty()) {
            return recentsUIGroupedList
        }

        //need this to compare if call is on same day (we only group if same day)
        val dateFormat: DateFormat =
            DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())

        //need this to know what today's date is
        val today = System.currentTimeMillis()

        //need these for date compares
        val second = 1000
        val min = second * 60
        val hour = min * 60
        val day = hour * 24
        //val week = day * 7

        //start group call count
        var groupCallCount = 1

        //loop through raw list
        for (i in 0 until rawLogList.size) {

            //initialize vars for each loop
            val rawLogObject = rawLogList[i]
            val recentsObject = RecentsUIGroupingsObject()
            var isSame = false
            var isContact = false

            //set new with new values
            val newGroupNumber = rawLogObject.number
            val newGroupType = rawLogObject.type
            val newGroupDate = rawLogObject.date
            val newGroupFeatures = rawLogObject.features
            val newGroupCallId = rawLogObject.callId

            //initialize old value vars
            var oldGroupNumber: String
            var oldGroupType: Int
            var oldGroupDate: Long
            var oldGroupFeatures: Int

            //only compare if not the first item
            if (i > 0) {
                //set prior with prior values
                oldGroupNumber = rawLogList[i - 1].number ?: ""
                oldGroupType = rawLogList[i - 1].type
                oldGroupDate = rawLogList[i - 1].date
                oldGroupFeatures = rawLogList[i - 1].features

                //check if same and can be grouped (we group by: number, date, call type and feature)
                if (TextUtils.equals(newGroupNumber, oldGroupNumber) &&
                    newGroupType == oldGroupType &&
                    TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(Date(oldGroupDate))
                    ) &&
                    newGroupFeatures == oldGroupFeatures
                ) {
                    //set as same
                    isSame = true
                }
            }

            //if new group is different or there is only one call, initialize group object and add to group list
            if (!isSame) {

                //reset groupCallCount since for new group
                groupCallCount = 1

                //store call id in list
                recentsObject.groupCallIds.add(newGroupCallId)

                //return contact cursor from number look up
                var contactCursor: Cursor?

                //get contact cursor
                contactCursor = getContactCursorByNumber(newGroupNumber!!)

                //set contact boolean
                if (contactCursor.moveToFirst()) {
                    isContact = true
                }

                //set isContact field in group
                recentsObject.isContact = isContact

                //get contact info if available
                if (isContact) {

                    try {

                        //if not empty then display contact name and number type
                        recentsObject.contactDisplayName =
                            contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME))

                        //get phone type code
                        recentsObject.contactPhoneType =
                            contactCursor.getInt(contactCursor.getColumnIndex(ContactsContract.PhoneLookup.TYPE))

                        //get contact _id and save to group
                        recentsObject.contactId =
                            contactCursor.getLong(contactCursor.getColumnIndex(ContactsContract.PhoneLookup._ID))

                        //get contact lookup uri and save to group
                        recentsObject.contactUri = ContactsContract.Contacts.getLookupUri(
                            recentsObject.contactId,
                            contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.PhoneLookup.LOOKUP_KEY))
                        )

                        //get contact thumb uri string and save to group
                        recentsObject.contactThumbUri =
                            contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                                ?: ""

                    } finally {

                        //close contact cursor
                        contactCursor.close()

                    }

                }

                //set isRead
                recentsObject.isRead = rawLogObject.isRead == 1

                //set isNew
                recentsObject.isNew = rawLogObject.new == 1

                //set group latest time
                recentsObject.groupLatestTimeMillis = newGroupDate

                //set geocoded location
                recentsObject.groupGeocodedLocation = rawLogObject.geoCodedLocation.toString()

                //set group number
                recentsObject.groupNumber = rawLogObject.number.toString()

                //set call type
                recentsObject.groupType = rawLogObject.type

                //set feature
                recentsObject.groupFeature = rawLogObject.features

                //set unique id for group item as call id (sum all call ids for groups with multiple calls)
                recentsObject.groupUniqueId = newGroupCallId

                //add to group list
                recentsUIGroupedList.add(recentsObject)

            } else {

                //increment groupCallCount
                groupCallCount++

                //get last group item
                val priorGroupItem = recentsUIGroupedList[recentsUIGroupedList.lastIndex]

                //update group call count
                priorGroupItem.groupCallCount = groupCallCount

                //store call id in list
                priorGroupItem.groupCallIds.add(newGroupCallId)

                //sum call ids as unique ids
                priorGroupItem.groupUniqueId = priorGroupItem.groupUniqueId.plus(newGroupCallId)

            }
        }

        var addedToday = false
        var addedYesterday = false
        var addedOlder = false

        //loop list to add headers
        for (i in 0 until recentsUIGroupedList.size) {

            //get current item
            val item = recentsUIGroupedList[i]

            //format as date
            val groupLatestTime = dateFormat.format(Date(item.groupLatestTimeMillis))

            //skip if header
            if (!item.isHeader) {

                //check if today by checking if formatted as time
                if (groupLatestTime == dateFormat.format(Date(today)) && !addedToday) {

                    recentsUIGroupedList.add(
                        i,
                        RecentsUIGroupingsObject(
                            groupUniqueId = 123456,
                            isHeader = true,
                            headerText = context.getString(R.string.today)
                        )
                    )
                    addedToday = true

                } else if (groupLatestTime == dateFormat.format(Date(today - day)) && !addedYesterday) {

                    //check if yesterday by checking if formatted as time
                    recentsUIGroupedList.add(
                        i,
                        RecentsUIGroupingsObject(
                            groupUniqueId = 654321,
                            isHeader = true,
                            headerText = context.getString(R.string.yesterday)
                        )
                    )
                    addedYesterday = true

                } else if (!addedOlder && groupLatestTime != dateFormat.format(Date(today)) && groupLatestTime != dateFormat.format(
                        Date(today - day)
                    )
                ) {

                    //if not today or yesterday then its older
                    recentsUIGroupedList.add(
                        i,
                        RecentsUIGroupingsObject(
                            groupUniqueId = 987654,
                            isHeader = true,
                            headerText = context.getString(R.string.older)
                        )
                    )
                    addedOlder = true

                }
            }
        }

        return recentsUIGroupedList
    }

    @SuppressLint("Recycle")
    private fun getContactCursorByNumber(number: String): Cursor {

        val projection = arrayOf(
            ContactsContract.PhoneLookup._ID,
            ContactsContract.PhoneLookup.LOOKUP_KEY,
            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI,
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.TYPE
        )

        return context.contentResolver.query(
            Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            ),
            projection,
            null,
            null,
            null
        )!!

    }

    fun deleteAllCallLogs() {
        context.contentResolver.delete(CallLog.Calls.CONTENT_URI, null, null)
    }

    fun getNewMissedCallCount(): Int {

        val selection = CallLog.Calls.TYPE
            .plus(" = ")
            .plus(CallLog.Calls.MISSED_TYPE)
            .plus(" AND ")
            .plus(CallLog.Calls.NEW)
            .plus(" = ")
            .plus(1)

        return getCallLogList(selection).size

    }

}