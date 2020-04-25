package com.rehyapp.calltimer.ui.callDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.rehyapp.calltimer.databinding.FragmentCallDetailsBinding

class CallDetailsFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "CallDetailsFragment"
    }

    private lateinit var callIds: LongArray
    private lateinit var viewModel: CallDetailsViewModel
    private lateinit var binding: FragmentCallDetailsBinding
    private val args: CallDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCallDetailsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(CallDetailsViewModel::class.java)
        callIds = args.callIds
        viewModel.text.observe(viewLifecycleOwner, Observer {
            binding.callDetailsText.text = it
        })
        var callId = ""
        for (element in callIds) {
            callId = callId.plus(element).plus(",")
        }
        viewModel.text.value =
            "This is Call Details Fragment, callId = ${callId.substring(0, callId.length - 1)}"
        return binding.root
    }
}
