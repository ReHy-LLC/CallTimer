package com.rehyapp.calltimer.ui.timer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rehyapp.calltimer.databinding.FragmentTimerBinding
import com.rehyapp.calltimer.ui.dialer.DialerViewModel

class TimerFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "TimeFragment"
    }

    private lateinit var timerViewModel: DialerViewModel
    private lateinit var binding: FragmentTimerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        timerViewModel = ViewModelProvider(this).get(DialerViewModel::class.java)
        binding = FragmentTimerBinding.inflate(inflater, container, false)
        timerViewModel.text.observe(viewLifecycleOwner, Observer {
            binding.textTimer.text = it
        })
        return binding.root
    }
}
