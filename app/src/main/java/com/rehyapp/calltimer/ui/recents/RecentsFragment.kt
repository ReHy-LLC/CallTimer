package com.rehyapp.calltimer.ui.recents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.databinding.FragmentRecentsBinding
import com.rehyapp.calltimer.permissions.BusinessRequest
import com.rehyapp.calltimer.permissions.PermissionFragment
import com.rehyapp.calltimer.ui.SwipeToDeleteCallback
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecentsFragment : PermissionFragment() {

    companion object {
        private const val LOG_TAG = "RecentsFragment"
    }

    //koin dependency injected ViewModel
    private val recentsViewModel by viewModel<RecentsViewModel>()

    //init in onCreateView
    private lateinit var binding: FragmentRecentsBinding

    //create adapter
    private val recentsAdapter = RecentsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        //inflate layout and set binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_recents, container, false)

        //set data binding vars
        binding.viewModel = recentsViewModel
        binding.adapter = recentsAdapter
        binding.lifecycleOwner = this

        //check if permissions were granted
        checkPermissionsAreGranted(BusinessRequest.CALLS_CALL_LOGS_CONTACTS)

        //return root view of binding
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (recentsViewModel.hasPermissions.value == true) {
            if (binding.recentsRecyclerSwitch.checkedButtonId == R.id.btnMissed) {
                recentsViewModel.showMissedLogs()
            } else {
                recentsViewModel.showAllLogs()
            }
        }
    }

    override fun onPermissionsGranted(businessRequest: BusinessRequest) {
        //setup recent recycler
        setupRecycler()
        //notify view model of permission grant
        recentsViewModel.updateHasPermissions(true)
        binding.linkRecents.setOnClickListener {
            val action = RecentsFragmentDirections.actionNavigationRecentsToNavigationDialer()
            requireView().findNavController().navigate(action)
        }
    }

    override fun onPermissionsDenied() {
        super.onPermissionsDenied()
        //notify view model of permission denial and set no permission text
        recentsViewModel.let {
            it.updateHasPermissions(false)
            it.setTextNoPermissions(
                getString(R.string.recent_no_permission_description_text),
                getString(R.string.grant)
            )
            binding.linkRecents.setOnClickListener {
                checkPermissionsAreGranted(BusinessRequest.CALLS_CALL_LOGS_CONTACTS)
            }
        }
    }

    private fun setupRecycler() {

        //handle switch changes for filtered recycler
        binding.recentsRecyclerSwitch.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked && checkedId == R.id.btnAll) {
                //show all if set to all via switch
                recentsViewModel.showAllLogs()
            } else if (!isChecked && checkedId == R.id.btnAll) {
                //if nothing is checked now but all was checked then check missed
                group.check(R.id.btnMissed)
            } else if (isChecked && checkedId == R.id.btnMissed) {
                //if missed is checked then filter recycler to missed logs only
                recentsViewModel.showMissedLogs()
            } else if (!isChecked && checkedId == R.id.btnMissed) {
                //if nothing is checked but missed was checked then check all
                group.check(R.id.btnAll)
            }
        }

        //handle recycler link clicks and show bottom sheet dialog
        binding.recentsRecyclerLink.setOnClickListener {
            val sheet = RecentsBottomSheet()
            sheet.isCancelable = false
            sheet.show(parentFragmentManager, sheet.tag)
        }

        //handle swipe deletes for recycler items
        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //delete as long as its not the header
                if (viewHolder.itemViewType != RecentsAdapter.ITEM_VIEW_TYPE_HEADER) {
                    //remove from data set
                    recentsViewModel.logData.value!!.removeAt(viewHolder.adapterPosition)
                    //notify the adapter that the item has been removed
                    recentsAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                    //cast out of box view holder to our custom recent view holder
                    val recentHolder: RecentsAdapter.RecentsViewHolder =
                        viewHolder as RecentsAdapter.RecentsViewHolder
                    //trigger async delete from call log database via view model function
                    recentsViewModel.deleteLogFromRecentsObject(recentHolder.getRecentGroupLog())
                }
            }
        }

        //add recycler scroll listener to determine if scrolled past first row and then needs to show divider above the recycler
        /*binding.recentsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //get the recycler layout manager to get visible item position
                val manager = recyclerView.layoutManager as LinearLayoutManager
                //if scrolled past 1st item then show the divider, if not then hide the divider
                if (manager.findFirstVisibleItemPosition() > 0) {
                    binding.recentRecyclerSwitchDivider.visibility = View.VISIBLE
                } else {
                    binding.recentRecyclerSwitchDivider.visibility = View.GONE
                }
            }
        })*/

        //create item touch helper using swipe handler and attach to recycler
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recentsRecycler)

        //create recycler divider decorator
        /*val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)

        //set divider drawable as divider decorator drawable
        dividerItemDecoration.setDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.divider
            )!!
        )

        //add divider to recycler
        binding.recentsRecycler.addItemDecoration(dividerItemDecoration)*/

        //observe log fetch from viewModel
        recentsViewModel.logData.observe(viewLifecycleOwner, Observer {
            it.let(recentsAdapter::submitList)
        })

    }

}
