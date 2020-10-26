package com.rehyapp.calltimer.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.databinding.FragmentContactsBinding
import com.rehyapp.calltimer.ui.MainSharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ContactsFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "ContactsFragment"
    }

    //koin dependency injected ViewModel
    private val sharedViewModel by sharedViewModel<MainSharedViewModel>()

    private lateinit var binding: FragmentContactsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts, container, false)
        binding.viewModel = sharedViewModel
        binding.lifecycleOwner = activity
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        sharedViewModel.activityIsRecentsFragShowing(false)
    }
}
