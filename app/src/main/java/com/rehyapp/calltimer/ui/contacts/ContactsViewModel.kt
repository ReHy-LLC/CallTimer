package com.rehyapp.calltimer.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContactsViewModel : ViewModel() {

    companion object {
        private const val LOG_TAG = "ContactsViewModel"
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is Contacts Fragment"
    }
    val text: LiveData<String> = _text
}