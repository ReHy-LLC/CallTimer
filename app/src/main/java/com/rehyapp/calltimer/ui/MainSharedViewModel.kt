package com.rehyapp.calltimer.ui

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

class MainSharedViewModel(private val logManager: LogManager, application: Application) :
    AndroidViewModel(application) {

    companion object {
        private const val LOG_TAG = "MainSharedViewModel"
    }

    private val context = getApplication<Application>().applicationContext

    private val _activityIsRecentsFragShowing = MutableLiveData<Boolean>()
    val activityIsRecentsFragShowing: MutableLiveData<Boolean> = _activityIsRecentsFragShowing

    private val _activityToolbarTitle = MutableLiveData<String>()
    val activityToolbarTitle: MutableLiveData<String> = _activityToolbarTitle

    fun setActivityToolbarTitle(toolbarTitle: String) {
        _activityToolbarTitle.postValue(toolbarTitle)
        var isRecentsFragShowing = false
        if (toolbarTitle == context.getString(R.string.title_recents)) {
            isRecentsFragShowing = true
        }
        _activityIsRecentsFragShowing.value = isRecentsFragShowing
    }

    /****************************** BEGIN RECENTS FRAGMENT SECTION ************************************/
    private val _recentsHasPermissions = MutableLiveData<Boolean>()
    val recentsHasPermissions: MutableLiveData<Boolean> = _recentsHasPermissions

    private val _recentsHasLogsToShow = MutableLiveData<Boolean>()
    val recentsHasLogsToShow: MutableLiveData<Boolean> = _recentsHasLogsToShow

    private val _recentsNoPermissionText = MutableLiveData<String>()
    val recentsNoPermissionText: MutableLiveData<String> = _recentsNoPermissionText

    private val _recentsNoPermissionButtonText = MutableLiveData<String>()
    val recentsNoPermissionButtonText: MutableLiveData<String> = _recentsNoPermissionButtonText

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
    }

    fun recentsShowAllCallLogs() {
        recentsLoadCallLogsByType(-1)
    }

    fun recentsShowMissedCallLogs() {
        recentsLoadCallLogsByType(CallLog.Calls.MISSED_TYPE)
    }

    fun recentsUpdateHasPermissions(__hasPermissions: Boolean) {
        _recentsHasPermissions.postValue(__hasPermissions)
        if (__hasPermissions) {
            recentsShowAllCallLogs()
        }
    }

    fun recentsDeleteCallLogGrouping(recentsUIGroupingsObject: RecentsUIGroupingsObject) {
        viewModelScope.launch(Dispatchers.IO) {
            logManager.deleteLogFromRecentsObject(recentsUIGroupingsObject)
        }
    }

    private fun recentsLoadCallLogsByType(callType: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            when (callType) {
                CallLog.Calls.MISSED_TYPE -> _recentsCallLogData.postValue(
                    logManager.convertToRecentsUIGroupings(
                        logManager.getCallsLogsByType(callType)
                    )
                )
                else -> _recentsCallLogData.postValue(
                    logManager.convertToRecentsUIGroupings(
                        logManager.getCallLogsAll()
                    )
                )
            }
            val canShow = logManager.canShowCallLogList("")
            _recentsHasLogsToShow.postValue(canShow)
            if (!canShow) {
                recentsSetNoPermissionTexts(
                    context.getString(R.string.text_no_call_log),
                    context.getString(R.string.call)
                )
            }
        }
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