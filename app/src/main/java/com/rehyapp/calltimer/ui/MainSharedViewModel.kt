package com.rehyapp.calltimer.ui

import android.app.Application
import android.provider.CallLog
import androidx.lifecycle.AndroidViewModel
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

    /****************************** BEGIN ACTIVITY SECTION ************************************/
    private val _activityIsRecentsFragShowing = MutableLiveData<Boolean>()
    val activityIsRecentsFragShowing = _activityIsRecentsFragShowing

    fun activityIsRecentsFragShowing(isRecentsFragShowing: Boolean) {
        _activityIsRecentsFragShowing.value = isRecentsFragShowing
    }

    private val _activityIsDefaultDialer = MutableLiveData<Boolean>()
    val activityIsDefaultDialer = _activityIsDefaultDialer

    fun activitySetIsDefaultDialer(isDefaultDialer: Boolean) {
        _activityIsDefaultDialer.value = isDefaultDialer
    }
    /****************************** END ACTIVITY SECTION *************************************/

    /****************************** BEGIN RECENTS FRAGMENT SECTION ***************************/
    private val _recentsUnreadMissedCount = MutableLiveData<Int>()
    val recentsUnreadMissedCount = _recentsUnreadMissedCount

    fun getNewMissedCallCount() {
        viewModelScope.launch(Dispatchers.IO) { _recentsUnreadMissedCount.postValue(logManager.getNewMissedCallCount()) }
    }

    private val _recentsHasPermissions = MutableLiveData<Boolean>()
    val recentsHasPermissions = _recentsHasPermissions

    fun recentsUpdateHasPermissions(__hasPermissions: Boolean) {
        _recentsHasPermissions.postValue(__hasPermissions)
        if (__hasPermissions) {
            if (recentsFilteredMissed.value == true) {
                recentsShowMissedCallLogs()
            } else {
                recentsShowAllCallLogs()
            }
        }
    }

    private val _recentsHasLogsToShow = MutableLiveData<Boolean>()
    val recentsHasLogsToShow = _recentsHasLogsToShow

    private val _recentsNoPermissionText = MutableLiveData<String>()
    val recentsNoPermissionText = _recentsNoPermissionText

    private val _recentsNoPermissionButtonText = MutableLiveData<String>()
    val recentsNoPermissionButtonText = _recentsNoPermissionButtonText

    fun recentsSetNoPermissionTexts(noPermissionTextLong: String, noPermissionButtonText: String) {
        _recentsNoPermissionText.postValue(noPermissionTextLong)
        _recentsNoPermissionButtonText.postValue(noPermissionButtonText)
    }

    private val _recentsFilteredMissed = MutableLiveData<Boolean>()
    val recentsFilteredMissed = _recentsFilteredMissed

    fun markNewMissedCallsAsRead() {
        viewModelScope.launch(Dispatchers.IO) {
            logManager.markNewAsRead()
        }
    }

    private val _recentsCallLogData = MutableLiveData<MutableList<RecentsUIGroupingsObject>>()
    val recentsCallLogData = _recentsCallLogData

    fun recentsShowAllCallLogs() {
        recentsLoadCallLogsByType(-1)
        _recentsFilteredMissed.postValue(false)
    }

    fun recentsShowMissedCallLogs() {
        recentsLoadCallLogsByType(CallLog.Calls.MISSED_TYPE)
        _recentsFilteredMissed.postValue(true)
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

    /****************************** BEGIN CALL DETAILS FRAGMENT SECTION *******************************/
    private val _text = MutableLiveData<String>()
    val text: MutableLiveData<String> = _text

    fun callDetailsMarkReadyById(callIds: LongArray) {
        logManager.markAsReadById(callIds)
    }
    /****************************** END CALL DETAILS FRAGMENT SECTION *********************************/

    /****************************** BEGIN CONTACTS FRAGMENT SECTION ***********************************/

    /****************************** END CONTACTS FRAGMENT SECTION *************************************/

    /****************************** BEGIN TIMER USAGE FRAGMENT SECTION ********************************/

    /****************************** END TIMER USAGE FRAGMENT SECTION **********************************/

    /****************************** BEGIN TIMER SETTINGS FRAGMENT SECTION *****************************/

    /****************************** END TIMER SETTINGS FRAGMENT SECTION *******************************/

    /****************************** BEGIN DIALER FRAGMENT SECTION *************************************/

    /****************************** END DIALER FRAGMENT SECTION ***************************************/

    init {
        _recentsHasPermissions.value = false
        _recentsHasLogsToShow.value = false
        _activityIsRecentsFragShowing.value = false
    }

}