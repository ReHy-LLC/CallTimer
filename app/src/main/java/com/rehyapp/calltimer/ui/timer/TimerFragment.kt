package com.rehyapp.calltimer.ui.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.ui.usage.UsageViewModel

class TimerFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "TimeFragment"
    }

    private lateinit var timerViewModel: UsageViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        timerViewModel = ViewModelProvider(this).get(UsageViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_timer, container, false)
        val textView: TextView = root.findViewById(R.id.text_timer)
        timerViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}
