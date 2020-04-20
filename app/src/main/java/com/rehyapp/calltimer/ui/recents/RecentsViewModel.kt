package com.rehyapp.calltimer.ui.recents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.rehyapp.calltimer.calllogging.LogManager
import com.rehyapp.calltimer.calllogging.RecentsUIGroupingsObject

class RecentsViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val LOG_TAG = "RecentsViewModel"
    }

    private var _noPermissionRecentsText = MutableLiveData<String>()
    var noPermissionRecentsText: MutableLiveData<String> = _noPermissionRecentsText

    private var _noPermissionRecentsLink = MutableLiveData<String>()
    var noPermissionRecentsLink: MutableLiveData<String> = _noPermissionRecentsLink

    fun setTextNoPermissions(newDescValue: String, newLinkValue: String) {
        _noPermissionRecentsText.value = newDescValue
        _noPermissionRecentsLink.value = newLinkValue
    }

    private var logManager = LogManager(application.applicationContext)
    val logList: MutableLiveData<MutableList<RecentsUIGroupingsObject>>
        get() = logManager.convertToRecentsUIGroupings(
            logManager.getCallLogsAll().asReversed()
        )

}