package com.rehyapp.calltimer.ui.recents

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wickerlabs.logmanager.LogObject

class RecentsViewModel : ViewModel() {

    companion object {
        private const val LOG_TAG = "RecentsViewModel"
    }

    private var _noPermissionRecentsText = MutableLiveData<String>()
    var noPermissionRecentsText: LiveData<String> = _noPermissionRecentsText

    private var _noPermissionRecentsLink = MutableLiveData<String>()
    var noPermissionRecentsLink: LiveData<String> = _noPermissionRecentsLink

    fun setTextNoPermissions(newDescValue: String, newLinkValue: String) {
        _noPermissionRecentsText.value = newDescValue
        _noPermissionRecentsLink.value = newLinkValue
    }

    private var _logList = MutableLiveData<MutableList<LogObject>>()
    var logList: LiveData<MutableList<LogObject>> = _logList

    fun setLogList(list: MutableList<LogObject>) {
        _logList.value = list
    }
}