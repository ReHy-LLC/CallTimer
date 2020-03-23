package com.rehyapp.calltimer.ui.usage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UsageViewModel : ViewModel() {

    companion object {
        private const val LOG_TAG = "UsageViewModel"
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is Usage Fragment"
    }
    val text: LiveData<String> = _text
}