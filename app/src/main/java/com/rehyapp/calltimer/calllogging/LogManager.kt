package com.rehyapp.calltimer.calllogging

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Log
import com.rehyapp.calltimer.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("MissingPermission")
class LogManager(_context: Context) {

    private var context: Context = _context
    private var logTag = "LogManager"

    suspend fun getCallById(callId: Int): LogObjectRaw {

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

    suspend fun getCallsLogsByType(callType: Int): MutableList<LogObjectRaw> {

        val selection = CallLog.Calls.TYPE + " = " + callType

        return getCallLogList(selection)

    }

    suspend fun getCallLogsAll(): MutableList<LogObjectRaw> {

        //empty selection to get unfiltered call log list
        return getCallLogList("")

    }

    suspend fun deleteLogFromRecentsObject(recentsUIGroupingsObject: RecentsUIGroupingsObject) {

        Log.e(logTag, "Size = " + recentsUIGroupingsObject.groupCallIds.size)

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

    suspend fun canShowCallLogList(selection: String): Boolean {

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

    private suspend fun getCallLogList(selection: String): MutableList<LogObjectRaw> {

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

    private suspend fun createRawLogObjectFromCursor(cursor: Cursor): LogObjectRaw {
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

    suspend fun convertToRecentsUIGroupings(rawLogList: MutableList<LogObjectRaw>): MutableList<RecentsUIGroupingsObject> {

        //empty group list to add to as we loop raw list
        val recentsUIGroupedList = mutableListOf<RecentsUIGroupingsObject>()

        //create header group
        val headerGrouping = RecentsUIGroupingsObject(
            "",
            0,
            context.getString(R.string.title_recents),
            "",
            false,
            "",
            "",
            mutableListOf(),
            true,
            false,
            0,
            null
        )

        //add header to list
        recentsUIGroupedList.add(headerGrouping)

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

                //append group count to end of prior group topText if > 1
                if (groupCallCount > 1) {
                    val priorGroupItem =
                        recentsUIGroupedList[recentsUIGroupedList.lastIndex]
                    priorGroupItem.groupTopText =
                        priorGroupItem.groupTopText.plus(" ($groupCallCount)")
                }

                //reset groupCallCount since for new group
                groupCallCount = 1

                //store call id in list
                recentsObject.groupCallIds.add(newGroupCallId)

                //set group number field, formatted
                recentsObject.groupNumber = PhoneNumberUtils.formatNumber(
                    newGroupNumber,
                    Locale.getDefault().country
                )

                //different scenarios for groupTimeDayDate field
                if (TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(today)
                    )
                ) {

                    //display time in timeDayDate field because it occurred today
                    recentsObject.groupTimeDayDate =
                        DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
                            .format(newGroupDate)

                } else if (TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(Date(today - day))
                    )
                ) {
                    //display yesterday in timeDayDate field because it occurred yesterday
                    recentsObject.groupTimeDayDate = context.getString(R.string.yesterday)

                } else if (TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(Date(today - (day * 2)))
                    ) ||
                    TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(Date(today - (day * 3)))
                    ) ||
                    TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(Date(today - (day * 4)))
                    ) ||
                    TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(Date(today - (day * 5)))
                    ) ||
                    TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(Date(today - (day * 6)))
                    )
                ) {

                    //display dayOfWeek in timeDayDate field because it occurred between 2 and 7 days
                    recentsObject.groupTimeDayDate =
                        SimpleDateFormat("EEEE", Locale.getDefault()).format(
                            Date(newGroupDate)
                        )

                } else {

                    //if nothing else display date
                    recentsObject.groupTimeDayDate = dateFormat.format(newGroupDate)

                }

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

                //different scenarios for topText and bottomText fields since they're dependent on each other
                if (isContact) {

                    try {
                        //if not empty then display contact name and number type
                        recentsObject.groupTopText =
                            contactCursor.getString(
                                contactCursor.getColumnIndex(
                                    ContactsContract.PhoneLookup.DISPLAY_NAME
                                )
                            )

                        //get phone type code
                        val phoneType =
                            contactCursor.getInt(
                                contactCursor.getColumnIndex(
                                    ContactsContract.PhoneLookup.TYPE
                                )
                            )

                        //convert phone type code to string and assign to group
                        recentsObject.groupBottomText =
                            ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                                context.resources,
                                phoneType,
                                context.getString(R.string.custom)
                            ).toString()

                        //get contact _id and save to group
                        recentsObject.contactId =
                            contactCursor.getLong(
                                contactCursor.getColumnIndex(
                                    ContactsContract.PhoneLookup._ID
                                )
                            )

                        //get contact lookup uri and save to group
                        recentsObject.contactUri = ContactsContract.Contacts.getLookupUri(
                            recentsObject.contactId!!
                            ,
                            contactCursor.getString(
                                contactCursor.getColumnIndex(
                                    ContactsContract.PhoneLookup.LOOKUP_KEY
                                )
                            )
                        )
                    } finally {
                        //close contact cursor
                        contactCursor.close()
                    }

                } else {

                    //if empty then not in contacts so show formatted number and location
                    recentsObject.groupTopText = recentsObject.groupNumber

                    if (rawLogObject.geoCodedLocation == "") {
                        recentsObject.groupBottomText = context.getString(R.string.unknown)
                    } else {
                        recentsObject.groupBottomText = rawLogObject.geoCodedLocation
                    }

                }

                Log.e(logTag, "features = ${rawLogObject.features}")
                //set drawable id number, group type and topTextRed based on call type
                if (rawLogObject.features == CallLog.Calls.FEATURES_VIDEO) {
                    recentsObject.groupType = context.getString(R.string.video)
                    if (CallLog.Calls.MISSED_TYPE == rawLogObject.type) {
                        recentsObject.groupTopTextRed = true
                        recentsObject.groupIconDrawableId = R.drawable.ic_video_red
                    } else {
                        recentsObject.groupIconDrawableId = R.drawable.ic_video
                    }
                } else {
                    when (newGroupType) {
                        CallLog.Calls.MISSED_TYPE -> {
                            recentsObject.groupIconDrawableId =
                                R.drawable.ic_phone_incoming_missed
                            recentsObject.groupType =
                                context.getString(R.string.call_type_missed)
                            recentsObject.groupTopTextRed = true
                        }
                        CallLog.Calls.OUTGOING_TYPE -> {
                            recentsObject.groupIconDrawableId = R.drawable.ic_phone_outgoing
                            recentsObject.groupType =
                                context.getString(R.string.call_type_outgoing)
                            recentsObject.groupTopTextRed = false
                        }
                        CallLog.Calls.INCOMING_TYPE -> {
                            recentsObject.groupIconDrawableId =
                                R.drawable.ic_phone_incoming_answered
                            recentsObject.groupType =
                                context.getString(R.string.call_type_incoming_answered)
                            recentsObject.groupTopTextRed = false
                        }
                        CallLog.Calls.ANSWERED_EXTERNALLY_TYPE -> {
                            recentsObject.groupIconDrawableId = R.drawable.ic_phone_incoming
                            recentsObject.groupType =
                                context.getString(R.string.call_type_answered_externally)
                            recentsObject.groupTopTextRed = false
                        }
                        CallLog.Calls.BLOCKED_TYPE -> {
                            recentsObject.groupIconDrawableId = R.drawable.ic_phone_blocked
                            recentsObject.groupType =
                                context.getString(R.string.call_type_blocked)
                            recentsObject.groupTopTextRed = false
                        }
                        CallLog.Calls.REJECTED_TYPE -> {
                            recentsObject.groupIconDrawableId = R.drawable.ic_phone_rejected
                            recentsObject.groupType =
                                context.getString(R.string.call_type_rejected)
                            recentsObject.groupTopTextRed = true
                        }
                    }
                }

                //add to group list
                recentsUIGroupedList.add(recentsObject)

            } else {

                //increment groupCallCount
                groupCallCount++

                //get last group item
                val priorGroupItem = recentsUIGroupedList[recentsUIGroupedList.lastIndex]

                //store call id in list
                priorGroupItem.groupCallIds.add(newGroupCallId)

                //different scenarios for groupTimeDayDate field
                if (TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(today)
                    )
                ) {

                    //display time in timeDayDate field because it occurred today
                    val display = DateFormat.getTimeInstance(
                        DateFormat.SHORT
                        , Locale.getDefault()
                    ).format(newGroupDate)
                    priorGroupItem.groupTimeDayDate = display

                } else if (TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(Date(today - day))
                    )
                ) {

                    //display yesterday in timeDayDate field because it occurred yesterday
                    priorGroupItem.groupTimeDayDate = context.getString(R.string.yesterday)

                } else if (TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(Date(today - (day * 2)))
                    ) ||
                    TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(Date(today - (day * 3)))
                    ) ||
                    TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(Date(today - (day * 4)))
                    ) ||
                    TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(Date(today - (day * 5)))
                    ) ||
                    TextUtils.equals(
                        dateFormat.format(Date(newGroupDate)),
                        dateFormat.format(Date(today - (day * 6)))
                    )
                ) {

                    //display dayOfWeek in timeDayDate field because it occurred between 2 and 7 days
                    priorGroupItem.groupTimeDayDate =
                        SimpleDateFormat("EEEE", Locale.getDefault()).format(
                            Date(newGroupDate)
                        )

                } else {

                    //if nothing else display date
                    priorGroupItem.groupTimeDayDate = dateFormat.format(newGroupDate)

                }

                //if last item in cursor
                if (i == rawLogList.size - 1) {

                    //append group count to end of prior group topText if > 1
                    priorGroupItem.groupTopText =
                        priorGroupItem.groupTopText.plus(" ($groupCallCount)")

                }
            }
        }

        return recentsUIGroupedList
    }

    private suspend fun getContactCursorByNumber(number: String): Cursor {

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

    suspend fun deleteAllCallLogs() {
        context.contentResolver.delete(CallLog.Calls.CONTENT_URI, null, null)
    }

}