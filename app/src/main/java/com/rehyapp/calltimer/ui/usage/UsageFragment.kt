package com.rehyapp.calltimer.ui.usage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rehyapp.calltimer.databinding.FragmentUsageBinding

class UsageFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "UsageFragment"
    }

    private lateinit var usageViewModel: UsageViewModel
    private lateinit var binding: FragmentUsageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        usageViewModel = ViewModelProvider(this).get(UsageViewModel::class.java)
        binding = FragmentUsageBinding.inflate(inflater, container, false)
        usageViewModel.text.observe(viewLifecycleOwner, Observer {
            binding.textUsage.text = it
        })
        return binding.root
    }
}
