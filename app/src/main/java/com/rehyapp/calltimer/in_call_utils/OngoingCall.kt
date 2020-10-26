package com.rehyapp.calltimer.in_call_utils

import android.telecom.Call
import android.telecom.VideoProfile
import com.rehyapp.calltimer.BuildConfig
import io.reactivex.subjects.BehaviorSubject


class OngoingCall {

    private val callback: Any = object : Call.Callback() {
        override fun onStateChanged(call: Call, newState: Int) {
            super.onStateChanged(call, newState)
            state.onNext(newState)
        }
    }

    fun setCall(value: Call?) {
        if (call != null) {
            call!!.unregisterCallback(callback as Call.Callback)
        }
        if (value != null) {
            value.registerCallback(callback as Call.Callback)
            state.onNext(value.state)
        }
        call = value
    }

    fun answer() {
        if (BuildConfig.DEBUG && call == null) {
            error("Assertion failed")
        }
        call!!.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun hangup() {
        if (BuildConfig.DEBUG && call == null) {
            error("Assertion failed")
        }
        call!!.disconnect()
    }

    companion object {
        var state = BehaviorSubject.create<Int>()
        private var call: Call? = null
    }
}