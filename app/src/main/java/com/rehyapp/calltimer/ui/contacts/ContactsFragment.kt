package com.rehyapp.calltimer.ui.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rehyapp.calltimer.databinding.FragmentContactsBinding

class ContactsFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "ContactsFragment"
    }

    private lateinit var contactsViewModel: ContactsViewModel
    private lateinit var binding: FragmentContactsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactsBinding.inflate(inflater, container, false)
        contactsViewModel = ViewModelProvider(this).get(ContactsViewModel::class.java)
        contactsViewModel.text.observe(viewLifecycleOwner, Observer {
            binding.textContacts.text = it
        })
        return binding.root
    }
}
