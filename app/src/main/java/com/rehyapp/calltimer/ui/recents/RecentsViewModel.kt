package com.rehyapp.calltimer.ui.recents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

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

}