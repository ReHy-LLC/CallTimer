package com.rehyapp.calltimer.ui.usage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.rehyapp.calltimer.R

class UsageFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "UsageFragment"
    }

    private lateinit var usageViewModel: UsageViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        usageViewModel =
                ViewModelProvider(this).get(UsageViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_usage, container, false)
        val textView: TextView = root.findViewById(R.id.text_usage)
        usageViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}
