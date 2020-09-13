package com.rehyapp.calltimer.ui.recents

import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.databinding.FragmentRecentsBinding
import com.rehyapp.calltimer.permissions.BusinessRequest
import com.rehyapp.calltimer.permissions.PermissionFragment
import com.rehyapp.calltimer.ui.MainSharedViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class RecentsFragment : PermissionFragment() {

    companion object {
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

        //check if permissions were granted
        checkPermissionsAreGranted(BusinessRequest.CALLS_CALL_LOGS_CONTACTS)

        //start observer
        activity?.contentResolver?.registerContentObserver(
            CallLog.Calls.CONTENT_URI,
            true,
            callLogChangeObserver
        )

        //return root view of binding
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        sharedViewModel.setActivityIsRecentsFragShowing(true)
    }

    override fun onPermissionsGranted(businessRequest: BusinessRequest) {
        //setup recent recycler
        setupRecycler()
        //notify view model of permission grant
        sharedViewModel.recentsUpdateHasPermissions(true)
        binding.linkRecents.setOnClickListener {
            val action = RecentsFragmentDirections.actionNavigationRecentsToNavigationDialer()
            requireView().findNavController().navigate(action)
        }
    }

    override fun onPermissionsDenied() {
        super.onPermissionsDenied()
        //notify view model of permission denial and set no permission text
        sharedViewModel.let {
            it.recentsUpdateHasPermissions(false)
            it.recentsSetNoPermissionTexts(
                getString(R.string.recent_no_permission_description_text),
                getString(R.string.grant)
            )
            binding.linkRecents.setOnClickListener {
                checkPermissionsAreGranted(BusinessRequest.CALLS_CALL_LOGS_CONTACTS)
            }
        }
    }

    override fun onDestroy() {
        activity?.contentResolver?.unregisterContentObserver(callLogChangeObserver)
        super.onDestroy()
    }

    private fun setupRecycler() {

        //handle swipe deletes for recycler items
        /*val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //delete as long as its not the header
                if (viewHolder.itemViewType != RecentsAdapter.ITEM_VIEW_TYPE_HEADER) {
                    //remove from data set
                    sharedViewModel.recentsCallLogData.value!!.removeAt(viewHolder.adapterPosition)
                    //notify the adapter that the item has been removed
                    recentsAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                    //cast out of box view holder to our custom recent view holder
                    val recentHolder: RecentsAdapter.RecentsViewHolder =
                        viewHolder as RecentsAdapter.RecentsViewHolder
                    //trigger async delete from call log database via view model function
                    sharedViewModel.recentsDeleteCallLogGrouping(recentHolder.getRecentGroupLog())
                }
            }
        }

        //create item touch helper using swipe handler and attach to recycler
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recentsRecycler)*/

        //observe log fetch from viewModel
        sharedViewModel.recentsCallLogData.observe(viewLifecycleOwner, {
            it.let(recentsAdapter::submitList)
        })

    }

    fun recyclerScrollTop() {
        binding.recentsRecycler.scrollToPosition(0)
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
