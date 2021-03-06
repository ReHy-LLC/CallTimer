package com.rehyapp.calltimer.ui

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.rehyapp.calltimer.databinding.ActivityCallBinding
import com.rehyapp.calltimer.in_call_utils.Constants.asString
import com.rehyapp.calltimer.in_call_utils.OngoingCall
import com.rehyapp.calltimer.ui.recents.RecentsFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.functions.Predicate
import java.util.*
import java.util.concurrent.TimeUnit


class CallActivity : AppCompatActivity() {

    companion object {
        private const val LOG_TAG = "CallActivity"

        fun start(context: Context?, call: Call?) {
            val intent = Intent(context, CallActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .setData(call?.details?.handle)
            context?.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityCallBinding
    private var disposables: CompositeDisposable? = null
    private var number: String? = null
    private var ongoingCall: OngoingCall? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        findViewById<View>(android.R.id.content).transitionName = "shared_element_container"

        // Attach a callback used to receive the shared elements from Activity A to be
        // used by the container transform transition.
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())

        // Set this Activity’s enter and return transition to a MaterialContainerTransform
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 300L
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = 300L
        }

        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideBottomNavigationBar()

        ongoingCall = OngoingCall()
        disposables = CompositeDisposable()

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 -> {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                keyguardManager.requestDismissKeyguard(this, null)
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.O -> {
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                keyguardManager.requestDismissKeyguard(this, null)
            }
            else -> {
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
            }
        }

        number = Objects.requireNonNull(intent.data).schemeSpecificPart

        binding.callAnswer.setOnClickListener {
            ongoingCall!!.answer()
        }

        binding.callReject.setOnClickListener {
            ongoingCall!!.hangup()
        }

    }

    override fun onStart() {
        super.onStart()
        updateUi(-1)
        disposables!!.add(
            OngoingCall.state
                .subscribe { integer -> updateUi(integer) }
        )
        disposables!!.add(
            OngoingCall.state
                .filter(object : Predicate<Int?> {
                    @Throws(Exception::class)
                    override fun test(integer: Int): Boolean {
                        if (integer == Call.STATE_DISCONNECTED) {
                            return true
                        }
                        return false
                    }
                })
                .delay(1, TimeUnit.SECONDS)
                .firstElement()
                .subscribe { finish() }
        )
    }

    private fun updateUi(state: Int?): Consumer<in Int?>? {
        binding.callInfo.text = state?.let { asString(it) }.plus("\n").plus(number)
        if (state != Call.STATE_RINGING) {
            binding.callAnswer.visibility = View.GONE
        } else binding.callAnswer.visibility = View.VISIBLE
        if (state == Call.STATE_DIALING || state == Call.STATE_RINGING || state == Call.STATE_ACTIVE) {
            binding.callReject.visibility = View.VISIBLE
        } else binding.callReject.visibility = View.GONE
        return null
    }

    override fun onStop() {
        super.onStop()
        RecentsFragment
        disposables!!.clear()
    }

    private fun hideBottomNavigationBar() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> window.insetsController?.hide(
                WindowInsets.Type.navigationBars()
            )
            else -> {
                window.decorView.apply {
                    systemUiVisibility =
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
                }
            }
        }
    }

}
