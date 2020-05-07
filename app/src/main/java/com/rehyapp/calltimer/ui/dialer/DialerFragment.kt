package com.rehyapp.calltimer.ui.dialer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rehyapp.calltimer.databinding.FragmentDialerBinding

class DialerFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "DialerFragment"
    }

    private lateinit var dialerViewModel: DialerViewModel
    private lateinit var binding: FragmentDialerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialerViewModel = ViewModelProvider(this).get(DialerViewModel::class.java)
        binding = FragmentDialerBinding.inflate(inflater, container, false)
        dialerViewModel.text.observe(viewLifecycleOwner, Observer {
            binding.textUsage.text = it
        })
        return binding.root
    }
}
