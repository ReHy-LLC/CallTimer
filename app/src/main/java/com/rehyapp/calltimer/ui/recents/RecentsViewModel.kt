package com.rehyapp.calltimer.ui.recents

import android.provider.CallLog
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rehyapp.calltimer.calllogging.LogManager
import com.rehyapp.calltimer.calllogging.RecentsUIGroupingsObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RecentsViewModel(private val logManager: LogManager) : ViewModel() {

    companion object {
        private const val LOG_TAG = "RecentsViewModel"
    }

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
        showAllLogs()
    }

    fun showAllLogs() {
        loadLogs(-1)
    }

    fun showMissedLogs() {
        loadLogs(CallLog.Calls.MISSED_TYPE)
    }

    fun deleteLogFromRecentsObject(recentsUIGroupingsObject: RecentsUIGroupingsObject) {
        viewModelScope.launch(Dispatchers.IO) {
            logManager.deleteLogFromRecentsObject(recentsUIGroupingsObject)
        }
    }

    private fun loadLogs(callType: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (callType) {
                    CallLog.Calls.MISSED_TYPE -> _logData.postValue(
                        logManager.convertToRecentsUIGroupings(
                            logManager.getCallsLogsByType(callType)
                        )
                    )
                    else -> _logData.postValue(logManager.convertToRecentsUIGroupings(logManager.getCallLogsAll()))
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, e.message.toString())
            }
        }
    }

    fun recentsClearAll(v: View) {
        viewModelScope.launch(Dispatchers.IO) {
            logManager.deleteAllCallLogs()
        }
    }

}