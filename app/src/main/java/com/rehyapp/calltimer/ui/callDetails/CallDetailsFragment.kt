package com.rehyapp.calltimer.ui.callDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rehyapp.calltimer.databinding.FragmentCallDetailsBinding
import com.rehyapp.calltimer.ui.MainSharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CallDetailsFragment : BottomSheetDialogFragment() {

    companion object {
        private const val LOG_TAG = "CallDetailsFragment"
    }

    //safe arg passed by clicking grouped call log on recents fragment
    private lateinit var callIds: LongArray

    //koin dependency injected ViewModel
    private val sharedViewModel by sharedViewModel<MainSharedViewModel>()

    private lateinit var binding: FragmentCallDetailsBinding
    private val args: CallDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCallDetailsBinding.inflate(inflater, container, false)
        callIds = args.callIds
        sharedViewModel.activityIsRecentsFragShowing(false)
        sharedViewModel.text.observe(viewLifecycleOwner, {
            binding.callDetailsText.text = it
        })
        var callId = ""
        for (element in callIds) {
            callId = callId.plus(element).plus(",")
        }
        sharedViewModel.text.value =
            "This is Call Details Fragment, callId = ${callId.substring(0, callId.length - 1)}"
        return binding.root
    }
}
