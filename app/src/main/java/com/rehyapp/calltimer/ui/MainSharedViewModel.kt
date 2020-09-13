package com.rehyapp.calltimer.ui

import android.app.Application
import android.provider.CallLog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.call_logging.LogManager
import com.rehyapp.calltimer.call_logging.RecentsUIGroupingsObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainSharedViewModel(private val logManager: LogManager, application: Application) :
    AndroidViewModel(application) {

    companion object {
        private const val LOG_TAG = "MainSharedViewModel"
    }

    private val context = getApplication<Application>().applicationContext

    private val _activityIsRecentsFragShowing = MutableLiveData<Boolean>()
    val activityIsRecentsFragShowing: MutableLiveData<Boolean> = _activityIsRecentsFragShowing

    fun setActivityIsRecentsFragShowing(isRecentsFragShowing: Boolean) {
        _activityIsRecentsFragShowing.value = isRecentsFragShowing
    }

    /****************************** BEGIN RECENTS FRAGMENT SECTION ************************************/
    private val _recentsUnreadMissedCount = MutableLiveData<Int>()
    val recentsUnreadMissedCount: MutableLiveData<Int> = _recentsUnreadMissedCount

    private val _recentsHasPermissions = MutableLiveData<Boolean>()
    val recentsHasPermissions: MutableLiveData<Boolean> = _recentsHasPermissions

    private val _recentsHasLogsToShow = MutableLiveData<Boolean>()
    val recentsHasLogsToShow: MutableLiveData<Boolean> = _recentsHasLogsToShow

    private val _recentsNoPermissionText = MutableLiveData<String>()
    val recentsNoPermissionText: MutableLiveData<String> = _recentsNoPermissionText

    private val _recentsNoPermissionButtonText = MutableLiveData<String>()
    val recentsNoPermissionButtonText: MutableLiveData<String> = _recentsNoPermissionButtonText

    private val _recentsFilteredMissed = MutableLiveData<Boolean>()
    val recentsFilteredMissed: MutableLiveData<Boolean> = _recentsFilteredMissed

    fun recentsSetNoPermissionTexts(noPermissionTextLong: String, noPermissionButtonText: String) {
        _recentsNoPermissionText.postValue(noPermissionTextLong)
        _recentsNoPermissionButtonText.postValue(noPermissionButtonText)
    }

    private val _recentsCallLogData = MutableLiveData<MutableList<RecentsUIGroupingsObject>>()
    val recentsCallLogData: LiveData<MutableList<RecentsUIGroupingsObject>>
        get() = _recentsCallLogData

    init {
        _recentsHasPermissions.value = true
        _recentsHasLogsToShow.value = true
        _activityIsRecentsFragShowing.value = true
    }

    fun getNewMissedCallCount() {
        viewModelScope.launch(Dispatchers.IO) {
            _recentsUnreadMissedCount.postValue(logManager.getNewMissedCallCount())
        }
    }

    fun recentsShowAllCallLogs() {
        recentsLoadCallLogsByType(-1)
        _recentsFilteredMissed.value = false
    }

    fun recentsShowMissedCallLogs() {
        recentsLoadCallLogsByType(CallLog.Calls.MISSED_TYPE)
        _recentsFilteredMissed.value = true
    }

    fun recentsUpdateHasPermissions(__hasPermissions: Boolean) {
        _recentsHasPermissions.postValue(__hasPermissions)
        if (__hasPermissions) {
            recentsShowAllCallLogs()
        }
    }

    private fun recentsLoadCallLogsByType(callType: Int) {
        var canShow: Boolean
        viewModelScope.launch(Dispatchers.IO) {
            when (callType) {
                CallLog.Calls.MISSED_TYPE -> {
                    canShow = logManager.canShowCallLogList(
                        CallLog.Calls.TYPE.plus(" = ")
                            .plus(CallLog.Calls.MISSED_TYPE.toString())
                    )
                    _recentsCallLogData.postValue(
                        logManager.convertToRecentsUIGroupings(
                            logManager.getCallsLogsByType(callType)
                        )
                    )
                }
                else -> {
                    canShow = logManager.canShowCallLogList("")
                    _recentsCallLogData.postValue(
                        logManager.convertToRecentsUIGroupings(
                            logManager.getCallLogsAll()
                        )
                    )
                }
            }
            _recentsHasLogsToShow.postValue(canShow)
            if (!canShow) {
                recentsSetNoPermissionTexts(
                    context.getString(R.string.text_no_call_log),
                    context.getString(R.string.call)
                )
            }
        }
        getNewMissedCallCount()
    }

    fun recentsDeleteAllCallLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            _recentsHasLogsToShow.postValue(false)
            logManager.deleteAllCallLogs()
        }
    }
    /****************************** END RECENTS FRAGMENT SECTION **************************************/

    /****************************** BEGIN CONTACTS FRAGMENT SECTION ***********************************/

    /****************************** END CONTACTS FRAGMENT SECTION *************************************/

    /****************************** BEGIN TIMER USAGE FRAGMENT SECTION ********************************/

    /****************************** END TIMER USAGE FRAGMENT SECTION **********************************/

    /****************************** BEGIN TIMER SETTINGS FRAGMENT SECTION *****************************/

    /****************************** END TIMER SETTINGS FRAGMENT SECTION *******************************/

    /****************************** BEGIN DIALER FRAGMENT SECTION *************************************/

    /****************************** END DIALER FRAGMENT SECTION ***************************************/
}