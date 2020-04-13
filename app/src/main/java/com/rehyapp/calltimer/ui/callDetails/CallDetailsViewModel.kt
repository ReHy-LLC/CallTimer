package com.rehyapp.calltimer.ui.callDetails

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CallDetailsViewModel : ViewModel() {

    companion object {
        private const val LOG_TAG = "CallDetailsViewModel"
    }

    val text = MutableLiveData<String>()

    fun text(_text: String) {
        text.value = _text
    }

}