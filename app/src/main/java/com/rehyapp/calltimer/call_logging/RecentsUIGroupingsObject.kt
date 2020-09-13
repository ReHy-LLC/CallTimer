package com.rehyapp.calltimer.call_logging

import android.net.Uri

data class RecentsUIGroupingsObject(
    var groupUniqueId: Long = 0,
    var groupLatestTimeMillis: Long = 0,
    var groupNumber: String = "",
    var groupType: Int = 0,
    var groupFeature: Int = 0,
    var groupGeocodedLocation: String = "",
    var groupCallCount: Int = 1,
    var groupCallIds: MutableList<Long> = mutableListOf(),
    var isContact: Boolean = false,
    var contactId: Long = 0,
    var contactDisplayName: String = "",
    var contactPhoneType: Int = 0,
    var contactUri: Uri = Uri.EMPTY,
    var contactThumbUri: String = "",
    var isHeader: Boolean = false,
    var headerText: String = "",
    var isRead: Boolean = true,
    var isNew: Boolean = false
)