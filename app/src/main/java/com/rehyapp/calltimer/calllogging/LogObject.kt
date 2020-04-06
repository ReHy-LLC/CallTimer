package com.rehyapp.calltimer.calllogging

data class LogObject(
    var id: Int,
    var number: String,
    var numberPresentation: Int,
    var presentationAllowed: Int,
    var postDialDigits: String,
    var viaNumberDualSim: String,
    var dateEpochMillis: Long,
    var durationMillis: Long,
    var dataUsageBytes: Long,
    var type: Int,
    var features: Int,
    var phoneAccountName: String,
    var phoneAccountID: String,
    var phoneAccountAddress: String,
    var phoneAccountHidden: Int

)