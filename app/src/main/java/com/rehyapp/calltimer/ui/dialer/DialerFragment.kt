package com.rehyapp.calltimer.ui.dialer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.databinding.FragmentDialerBinding
import com.rehyapp.calltimer.ui.MainSharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DialerFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "DialerFragment"
    }

    //koin dependency injected ViewModel
    private val sharedViewModel by sharedViewModel<MainSharedViewModel>()

    private lateinit var binding: FragmentDialerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dialer, container, false)
        binding.viewModel = sharedViewModel
        binding.lifecycleOwner = activity
        return binding.root
    }
}
