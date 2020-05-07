package com.rehyapp.calltimer.ui.dialer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DialerViewModel : ViewModel() {

    companion object {
        private const val LOG_TAG = "DialerFragment"
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is Dialer Fragment"
    }
    val text: LiveData<String> = _text
}