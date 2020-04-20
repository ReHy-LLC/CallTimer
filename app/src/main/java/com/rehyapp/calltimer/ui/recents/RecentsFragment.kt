package com.rehyapp.calltimer.ui.recents

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dgreenhalgh.android.simpleitemdecoration.linear.EndOffsetItemDecoration
import com.dgreenhalgh.android.simpleitemdecoration.linear.StartOffsetItemDecoration
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.calllogging.LogManager
import com.rehyapp.calltimer.calllogging.RecentsUIGroupingsObject
import com.rehyapp.calltimer.databinding.FragmentRecentsBinding

class RecentsFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "RecentsFragment"
        private const val REQUEST_CODE_SET_DEFAULT_DIALER = 36
    }

    private lateinit var recentsViewModel: RecentsViewModel
    private lateinit var binding: FragmentRecentsBinding
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: RecentsAdapter
    private lateinit var logsManager: LogManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        recentsViewModel = ViewModelProvider(this).get(RecentsViewModel::class.java)
        binding = FragmentRecentsBinding.inflate(inflater, container, false)

        logsManager = LogManager(context!!)
        layoutManager = LinearLayoutManager(context)
        binding.recentsRecycler.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.divider)!!)

        binding.recentsRecycler.apply {
            addItemDecoration(dividerItemDecoration)
            addItemDecoration(StartOffsetItemDecoration(20))
            addItemDecoration(EndOffsetItemDecoration(200))
        }

        recentsViewModel.noPermissionRecentsText.observe(viewLifecycleOwner, Observer {
            binding.textRecents.text = it
        })

        recentsViewModel.noPermissionRecentsLink.observe(viewLifecycleOwner, Observer {
            binding.linkRecents.text = it
        })

        val hasLogPermission = ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_CALL_LOG)
        if (hasLogPermission == PackageManager.PERMISSION_GRANTED) {
            recentsViewModel.logList.observe(viewLifecycleOwner, Observer {
                showCallLog(it)
            })
        } else {
            hideRecyclerNoPermission()
        }

        return binding.root
    }

    private fun hideRecyclerNoPermission() {
        binding.recentsNoPermissionView.visibility = View.VISIBLE
        binding.recentsView.visibility = View.GONE
        recentsViewModel.setTextNoPermissions(
            getString(R.string.recent_no_permission_description_text),
            getString(R.string.enable)
        )
        binding.linkRecents.setOnClickListener {
            requestLogPermission()
        }
    }

    private fun hideRecyclerNoLogs() {
        binding.recentsNoPermissionView.visibility = View.VISIBLE
        binding.recentsView.visibility = View.GONE
        recentsViewModel.setTextNoPermissions(
            getString(R.string.text_no_call_log),
            getString(R.string.dialer)
        )
        binding.linkRecents.setOnClickListener {
            //TODO: Open dialer from this link
            Toast.makeText(context, "Will open dialer screen.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun requestLogPermission() {
        ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.READ_CALL_LOG), REQUEST_CODE_SET_DEFAULT_DIALER)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> onResume()
                PackageManager.PERMISSION_DENIED -> Toast.makeText(context,
                    getString(R.string.call_log_denied_toast), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showCallLog(logList: MutableList<RecentsUIGroupingsObject>) {
        val canShow = logsManager.canShowCallLogList("")
        if (canShow) {
            adapter = RecentsAdapter(logList)
            binding.recentsRecycler.adapter = adapter
            binding.recentsNoPermissionView.visibility = View.GONE
            binding.recentsView.visibility = View.VISIBLE
        } else {
            hideRecyclerNoLogs()
        }
    }

}
