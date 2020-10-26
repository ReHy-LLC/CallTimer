package com.rehyapp.calltimer.ui.recents

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.eazypermissions.common.model.PermissionResult
import com.eazypermissions.dsl.extension.requestPermissions
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.databinding.FragmentRecentsBinding
import com.rehyapp.calltimer.ui.MainSharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class RecentsFragment : Fragment() {

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 4
        private const val LOG_TAG = "RecentsFragment"
    }

    //koin dependency injected ViewModel
    private val sharedViewModel by sharedViewModel<MainSharedViewModel>()

    //init in onCreateView
    private lateinit var binding: FragmentRecentsBinding

    //create adapter
    private val recentsAdapter = RecentsAdapter()

    //create observer
    private val callLogChangeObserver = CallLogChangeObserver(Handler(Looper.getMainLooper()))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //inflate layout and set binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recents, container, false)

        //set data binding vars
        binding.viewModel = sharedViewModel
        binding.adapter = recentsAdapter
        binding.lifecycleOwner = activity

        //return root view of binding
        return binding.root

    }

    override fun onResume() {

        super.onResume()

        sharedViewModel.activityIsRecentsFragShowing(true)

        Log.e(LOG_TAG, "OnResume!")

        checkPermissionsAreGranted()

    }


    override fun onDestroy() {

        activity?.contentResolver?.unregisterContentObserver(callLogChangeObserver)

        super.onDestroy()

    }

    private fun checkPermissionsAreGranted() {

        if (sharedViewModel.activityIsDefaultDialer.value == true) {

            val hasReadCallLogs = context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_CALL_LOG
                )
            } == PackageManager.PERMISSION_GRANTED

            val hasReadContacts = context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.READ_CONTACTS
                )
            } == PackageManager.PERMISSION_GRANTED

            val hasCallPhone = context?.let {
                ContextCompat.checkSelfPermission(
                    it,
                    Manifest.permission.CALL_PHONE
                )
            } == PackageManager.PERMISSION_GRANTED

            if (!hasReadCallLogs || !hasReadContacts || !hasCallPhone) {

                requestPermissions(
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.CALL_PHONE
                ) {
                    requestCode = REQUEST_CODE_PERMISSIONS
                    resultCallback = {
                        handlePermissionResult(this)
                    }
                }

            } else {

                handlePermissionGrant()

            }

        }

    }

    private fun handlePermissionResult(permissionResult: PermissionResult) {

        when (permissionResult) {
            is PermissionResult.PermissionGranted -> {
                Log.e(LOG_TAG, "Permissions Granted!")
                handlePermissionGrant()
            }
            is PermissionResult.PermissionDenied -> {
                Log.e(LOG_TAG, "Permissions Denied!")
                handlePermissionDenial()
            }
            is PermissionResult.PermissionDeniedPermanently -> {
                Log.e(LOG_TAG, "Permissions Denied Permanently!")
                handlePermissionDeniedPermanently()
            }
            is PermissionResult.ShowRational -> {
                Log.e(LOG_TAG, "Show Rationale!")
                handlePermissionRational()
            }
        }
    }

    private fun handlePermissionGrant() {

        //notify view model of permission grant
        sharedViewModel.recentsUpdateHasPermissions(true)

        binding.linkRecents.setOnClickListener {
            val action = RecentsFragmentDirections.actionNavigationRecentsToNavigationDialer()
            requireView().findNavController().navigate(action)
        }

        //start observer
        activity?.contentResolver?.registerContentObserver(
            CallLog.Calls.CONTENT_URI,
            true,
            callLogChangeObserver
        )

        //setup recycler
        setupRecycler()

    }

    private fun handlePermissionDenial() {

        //notify view model of permission denial and set no permission text
        sharedViewModel.let {

            it.recentsUpdateHasPermissions(false)

            it.recentsSetNoPermissionTexts(
                getString(R.string.recent_no_permission_description_text),
                getString(R.string.grant)
            )

            binding.linkRecents.setOnClickListener {
                checkPermissionsAreGranted()
            }

            binding.imageRecents.visibility = View.VISIBLE

        }

    }

    private fun handlePermissionDeniedPermanently() {

        //notify view model of permission denial and set no permission text
        sharedViewModel.let {

            it.recentsUpdateHasPermissions(false)

            it.recentsSetNoPermissionTexts(
                getString(R.string.recent_no_permission_description_text),
                getString(R.string.grant)
            )

            binding.linkRecents.setOnClickListener {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", activity?.packageName, null)
                )
                startActivity(intent)
            }

            binding.imageRecents.visibility = View.VISIBLE

        }

    }

    private fun handlePermissionRational() {

        if (!this.isRemoving) {

            activity.let {

                val builder = AlertDialog.Builder(it)

                builder.apply {
                    setTitle(R.string.permission_title_calls_call_logs_contacts)
                    setMessage(context.getString(R.string.permission_rationale_calls_call_logs_contacts))
                    setCancelable(false)
                    setPositiveButton(R.string.ok) { _, _ ->
                        checkPermissionsAreGranted()
                    }

                    setNegativeButton(R.string.cancel) { dialog, _ ->
                        if (!this@RecentsFragment.isRemoving) {
                            dialog.dismiss()
                            handlePermissionDenial()
                        }

                    }

                }.create()

            }.show()
        }

    }

    private fun setupRecycler() {

        //observe log fetch from viewModel
        sharedViewModel.recentsCallLogData.observe(
            viewLifecycleOwner,
            { it.let(recentsAdapter::submitList) })

    }


    inner class CallLogChangeObserver(handler: Handler) : ContentObserver(handler) {

        override fun onChange(selfChange: Boolean) {

            if (sharedViewModel.recentsFilteredMissed.value == true) {

                sharedViewModel.recentsShowMissedCallLogs()

            } else {

                sharedViewModel.recentsShowAllCallLogs()

            }

        }

    }

}
