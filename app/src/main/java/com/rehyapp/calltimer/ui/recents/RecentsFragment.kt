package com.rehyapp.calltimer.ui.recents

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.google.android.material.textview.MaterialTextView
import com.rehyapp.calltimer.R
import com.wickerlabs.logmanager.LogsManager
import kotlinx.android.synthetic.main.fragment_recents.*


class RecentsFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "RecentsFragment"
        private const val REQUEST_CODE_SET_DEFAULT_DIALER = 36
    }

    private lateinit var recentsViewModel: RecentsViewModel
    private lateinit var noPermissionView: ConstraintLayout
    private lateinit var hasPermissionView: ConstraintLayout
    private lateinit var linkTextView: MaterialTextView
    private lateinit var recycler: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var adapter: RecentsAdapter
    private lateinit var logsManager: LogsManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        recentsViewModel = ViewModelProvider(this).get(RecentsViewModel::class.java)

        val rootView = inflater.inflate(R.layout.fragment_recents, container, false)

        noPermissionView = rootView.findViewById(R.id.recents_no_permission_view)
        hasPermissionView = rootView.findViewById(R.id.recents_view)
        linkTextView = rootView.findViewById(R.id.link_recents)
        recycler = rootView.findViewById(R.id.recents_recycler)
        logsManager = LogsManager(context)
        layoutManager = LinearLayoutManager(context)
        recycler.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.divider)!!)
        recycler.addItemDecoration(dividerItemDecoration)
        recycler.addItemDecoration(StartOffsetItemDecoration(20))
        recycler.addItemDecoration(EndOffsetItemDecoration(200))

        recentsViewModel.noPermissionRecentsText.observe(viewLifecycleOwner, Observer {
            text_recents.text = it
        })

        recentsViewModel.noPermissionRecentsLink.observe(viewLifecycleOwner, Observer {
            linkTextView.text = it
        })

        val hasLogPermission = ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_CALL_LOG)
        if (hasLogPermission != PackageManager.PERMISSION_GRANTED) {
            noPermissionView.visibility = View.VISIBLE
            hasPermissionView.visibility = View.GONE
            recentsViewModel.setTextNoPermissions(getString(R.string.recent_no_permission_description_text), getString(R.string.enable))
            linkTextView.setOnClickListener {
                requestLogPermission()
            }
        } else {
            showCallLog()
        }

        return rootView
    }

    private fun requestLogPermission() {
        ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.READ_CALL_LOG), REQUEST_CODE_SET_DEFAULT_DIALER)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            when (grantResults[0]) {
                PackageManager.PERMISSION_GRANTED -> showCallLog()
                PackageManager.PERMISSION_DENIED -> Toast.makeText(context,
                    getString(R.string.call_log_denied_toast), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showCallLog() {
        recycler.invalidate()
        adapter = RecentsAdapter(logsManager.getLogs(LogsManager.ALL_CALLS).asReversed())
        recycler.swapAdapter(adapter,false)
        noPermissionView.visibility = View.GONE
        hasPermissionView.visibility = View.VISIBLE
    }
}
