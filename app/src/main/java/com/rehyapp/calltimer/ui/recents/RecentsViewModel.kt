package com.rehyapp.calltimer.ui.recents

import android.app.Application
import android.provider.CallLog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.calllogging.LogManager
import com.rehyapp.calltimer.calllogging.RecentsUIGroupingsObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecentsViewModel(private val logManager: LogManager, application: Application) :
    AndroidViewModel(application) {

    companion object {
        private const val LOG_TAG = "RecentsViewModel"
    }

    private val context = getApplication<Application>().applicationContext

    private val _hasPermissions = MutableLiveData<Boolean>()
    val hasPermissions: MutableLiveData<Boolean> = _hasPermissions

    private val _hasLogsToShow = MutableLiveData<Boolean>()
    val hasLogsToShow: MutableLiveData<Boolean> = _hasLogsToShow

    private val _noPermissionRecentsText = MutableLiveData<String>()
    val noPermissionRecentsText: MutableLiveData<String> = _noPermissionRecentsText

    private val _noPermissionRecentsLink = MutableLiveData<String>()
    val noPermissionRecentsLink: MutableLiveData<String> = _noPermissionRecentsLink

    fun setTextNoPermissions(newDescValue: String, newLinkValue: String) {
        _noPermissionRecentsText.postValue(newDescValue)
        _noPermissionRecentsLink.postValue(newLinkValue)
    }

    private val _logData = MutableLiveData<MutableList<RecentsUIGroupingsObject>>()
    val logData: LiveData<MutableList<RecentsUIGroupingsObject>>
        get() = _logData

    init {
        _hasPermissions.value = false
        _hasLogsToShow.value = false
    }

    fun showAllLogs() {
        loadLogs(-1)
    }

    fun showMissedLogs() {
        loadLogs(CallLog.Calls.MISSED_TYPE)
    }

    fun updateHasPermissions(__hasPermissions: Boolean) {
        _hasPermissions.postValue(__hasPermissions)
        if (__hasPermissions) {
            showAllLogs()
        }
    }

    fun deleteLogFromRecentsObject(recentsUIGroupingsObject: RecentsUIGroupingsObject) {
        viewModelScope.launch(Dispatchers.IO) {
            logManager.deleteLogFromRecentsObject(recentsUIGroupingsObject)
        }
    }

    private fun loadLogs(callType: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            when (callType) {
                CallLog.Calls.MISSED_TYPE -> _logData.postValue(
                    logManager.convertToRecentsUIGroupings(
                        logManager.getCallsLogsByType(callType)
                    )
                )
                else -> _logData.postValue(logManager.convertToRecentsUIGroupings(logManager.getCallLogsAll()))
            }
            val canShow = logManager.canShowCallLogList("")
            _hasLogsToShow.postValue(canShow)
            if (!canShow) {
                setTextNoPermissions(
                    context.getString(R.string.text_no_call_log),
                    context.getString(R.string.call)
                )
            }
        }
    }

    fun recentsClearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            _hasLogsToShow.postValue(false)
            logManager.deleteAllCallLogs()
        }
    }

}