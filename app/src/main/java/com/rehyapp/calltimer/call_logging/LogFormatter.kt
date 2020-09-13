package com.rehyapp.calltimer.call_logging

import android.content.Context
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import com.rehyapp.calltimer.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class LogFormatter(_context: Context) {

    private val context = _context

    private fun formatTime(timeMillis: Long): String {

        return DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(timeMillis)

    }

    fun formatPhoneNumber(phoneNumber: String): String {

        return PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().country)

    }

    fun formatPhoneNumber(phoneNumber: String, callCount: Int): String {

        return if (callCount == 1) {
            PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().country)
        } else {

            PhoneNumberUtils.formatNumber(phoneNumber, Locale.getDefault().country)
                .plus(" ($callCount)")

        }

    }

    fun formatContactDisplayName(contactDisplayName: String, callCount: Int): String {

        return if (callCount == 1) {
            contactDisplayName
        } else {

            contactDisplayName.plus(" ($callCount)")

        }

    }

    fun formatDatetimeMillis(datetimeMillis: Long): String {

        //get short date format for users locale.
        val dateFormat: DateFormat =
            DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())

        //need this to know what today's date is
        val now = System.currentTimeMillis()

        //need these for date compares
        val second = 1000
        val min = second * 60
        val hour = min * 60
        val day = hour * 24
        //val week = day * 7
        val today = dateFormat.format(now)
        val oneDayAgo = dateFormat.format(Date(now - day))
        val twoDaysAgo = dateFormat.format(Date(now - (day * 2)))
        val threeDaysAgo = dateFormat.format(Date(now - (day * 3)))
        val fourDaysAgo = dateFormat.format(Date(now - (day * 4)))
        val fiveDaysAgo = dateFormat.format(Date(now - (day * 5)))
        val sixDaysAgo = dateFormat.format(Date(now - (day * 6)))

        if (TextUtils.equals(dateFormat.format(Date(datetimeMillis)), today)) {

            //return time because it occurred today
            return formatTime(datetimeMillis)


        } else if (TextUtils.equals(dateFormat.format(Date(datetimeMillis)), oneDayAgo)) {

            //display yesterday with time because it occurred yesterday
            return context.getString(R.string.yesterday).plus(" ").plus(formatTime(datetimeMillis))

        } else if (TextUtils.equals(dateFormat.format(Date(datetimeMillis)), twoDaysAgo)
            || TextUtils.equals(dateFormat.format(Date(datetimeMillis)), threeDaysAgo)
            || TextUtils.equals(dateFormat.format(Date(datetimeMillis)), fourDaysAgo)
            || TextUtils.equals(dateFormat.format(Date(datetimeMillis)), fiveDaysAgo)
            || TextUtils.equals(dateFormat.format(Date(datetimeMillis)), sixDaysAgo)
        ) {

            //return dayOfWeek and time because it occurred between 2 and 7 days
            return SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(datetimeMillis))
                .plus(" ").plus(formatTime(datetimeMillis))

        } else {

            //if nothing else display date and time
            return dateFormat.format(datetimeMillis).plus(" ").plus(formatTime(datetimeMillis))

        }

    }

    fun formatContactPhoneType(phoneType: Int): String {

        return ContactsContract.CommonDataKinds.Phone.getTypeLabel(
            context.resources,
            phoneType,
            context.getString(R.string.custom)
        ).toString()

    }

    fun pickTypeDrawableId(callType: Int, features: Int): Int {

        //set type drawable id based on call type and features
        if (features == CallLog.Calls.FEATURES_VIDEO) {

            return if (CallLog.Calls.MISSED_TYPE == callType) {

                R.drawable.ic_video_red

            } else {

                R.drawable.ic_video

            }

        } else {

            when (callType) {

                CallLog.Calls.MISSED_TYPE -> {

                    return R.drawable.ic_call_missed

                }
                CallLog.Calls.OUTGOING_TYPE -> {

                    return R.drawable.ic_call_made

                }
                CallLog.Calls.INCOMING_TYPE -> {

                    return R.drawable.ic_call_received

                }
                CallLog.Calls.ANSWERED_EXTERNALLY_TYPE -> {

                    return R.drawable.ic_call_received

                }
                CallLog.Calls.BLOCKED_TYPE -> {

                    return R.drawable.ic_call_blocked

                }
                CallLog.Calls.REJECTED_TYPE -> {

                    return R.drawable.ic_call_missed

                }
                else -> return 0
            }
        }
    }

}