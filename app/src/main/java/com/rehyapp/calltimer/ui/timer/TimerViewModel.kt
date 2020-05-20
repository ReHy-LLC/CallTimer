package com.rehyapp.calltimer.ui.timer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {

    companion object {
        private const val LOG_TAG = "TimerViewModel"
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is timer Fragment"
    }
    val text: LiveData<String> = _text
}