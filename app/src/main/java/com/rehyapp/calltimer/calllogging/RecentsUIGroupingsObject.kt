package com.rehyapp.calltimer.calllogging

import android.net.Uri

data class RecentsUIGroupingsObject(
    var groupUniqueId: Long = 0,
    var groupTimeDayDate: String? = "",
    var groupIconDrawableId: Int = 0,
    var groupTopText: String? = "",
    var groupBottomText: String? = "",
    var groupTopTextRed: Boolean? = false,
    var groupNumber: String? = "",
    var groupType: String? = "",
    var groupCallIds: MutableList<Long> = mutableListOf(),
    var groupIsHeader: Boolean? = false,
    var isContact: Boolean? = false,
    var contactId: Long? = 0,
    var contactUri: Uri? = null,
    var contactThumbUri: String? = "NULL"
)