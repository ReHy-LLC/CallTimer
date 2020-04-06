package com.rehyapp.calltimer

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.rehyapp.calltimer.databinding.ActivityCallBinding
import com.rehyapp.calltimer.in_call_utils.Constants.asString
import com.rehyapp.calltimer.in_call_utils.OngoingCall
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
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideBottomNavigationBar()

        ongoingCall = OngoingCall()
        disposables = CompositeDisposable()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            this.window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
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
        assert(updateUi(-1) != null)
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
        binding.callInfo.text = "${state?.let { asString(it) }}\n$number"
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
        disposables!!.clear()
    }

    private fun hideBottomNavigationBar() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

}
