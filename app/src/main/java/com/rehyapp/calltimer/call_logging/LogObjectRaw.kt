package com.rehyapp.calltimer.call_logging

data class LogObjectRaw(
    //Added API 1
    var callId: Long = -1,
    var type: Int = -1,
    var date: Long = -1,
    var duration: Long = -1,
    var new: Int = -1,
    var number: String? = "",

    //Added API 14
    var isRead: Int = -1,

    //Added API 19
    var numberPresentation: String? = "",

    //Added API 21
    var countryISO: String? = "",
    var dataUsage: Long = -1,
    var features: Int = -1,
    var geoCodedLocation: String? = "",
    var phoneAccountComponentName: String? = "",
    var phoneAccountId: String? = "",

    //Added API 24
    var postDialDigits: String? = "",
    var viaNumber: String? = "",

    //Added API 29
    var blockReason: Int = -1,
    var callScreeningAppName: String? = "",
    var callScreeningComponentName: String? = ""
)