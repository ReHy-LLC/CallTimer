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
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.databinding.BottomSheetRecentsBinding
import com.rehyapp.calltimer.databinding.FragmentRecentsBinding
import com.rehyapp.calltimer.ui.SwipeToDeleteCallback
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecentsFragment : Fragment() {

    companion object {
        private const val LOG_TAG = "RecentsFragment"
        private const val REQUEST_CODE_SET_DEFAULT_DIALER = 36
        private const val ITEM_VIEW_TYPE_HEADER = 0
        private const val ITEM_VIEW_TYPE_ITEM = 1
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

        //create recycler divider decorator
        val dividerItemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)

        //set divider drawable as divider decorator drawable
        dividerItemDecoration.setDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.divider
            )!!
        )

        //add divider to recycler
        binding.recentsRecycler.addItemDecoration(dividerItemDecoration)

        //observe log fetch from viewModel
        recentsViewModel.logData.observe(viewLifecycleOwner, Observer {
            it.let(recentsAdapter::submitList)
        })

        showRecycler()

        binding.recentsRecyclerSwitch.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked && checkedId == R.id.btnAll) {
                recentsViewModel.showAllLogs()
            } else if (!isChecked && checkedId == R.id.btnAll) {
                group.check(R.id.btnMissed)
            } else if (isChecked && checkedId == R.id.btnMissed) {
                recentsViewModel.showMissedLogs()
            } else if (!isChecked && checkedId == R.id.btnMissed) {
                group.check(R.id.btnAll)
            }
        }

        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (viewHolder.itemViewType != ITEM_VIEW_TYPE_HEADER) {
                    recentsViewModel.logData.value!!.removeAt(viewHolder.adapterPosition)
                    recentsAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                    val recentHolder: RecentsAdapter.RecentsViewHolder =
                        viewHolder as RecentsAdapter.RecentsViewHolder
                    recentsViewModel.deleteLogFromRecentsObject(recentHolder.getRecentGroupLog())
                }
            }
        }

        binding.recentsRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val manager = recyclerView.layoutManager as LinearLayoutManager
                if (manager.findFirstVisibleItemPosition() > 0) {
                    binding.recentRecyclerSwitchDivider.visibility = View.VISIBLE
                } else {
                    binding.recentRecyclerSwitchDivider.visibility = View.GONE
                }
            }
        })

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recentsRecycler)

        binding.recentsRecyclerLink.setOnClickListener { showBottomSheet(container) }

        //return root view of binding
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val hasLogPermission =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG)
        if (hasLogPermission != PackageManager.PERMISSION_GRANTED) {
            hideRecyclerNoPermission()
        } else {
            showRecycler()
            when (binding.recentsRecyclerSwitch.checkedButtonId) {
                R.id.btnAll -> recentsViewModel.showAllLogs()
                R.id.btnMissed -> recentsViewModel.showMissedLogs()
            }
        }
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

    private fun showRecycler() {
        binding.recentsNoPermissionView.visibility = View.GONE
        binding.recentsView.visibility = View.VISIBLE
    }

    private fun showBottomSheet(viewGroup: ViewGroup?) {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_recents, null)
        val bottomSheetBinding: BottomSheetRecentsBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.bottom_sheet_recents, viewGroup, false)
        bottomSheetBinding.viewModel = recentsViewModel

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun requestLogPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.READ_CALL_LOG),
            REQUEST_CODE_SET_DEFAULT_DIALER
        )
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

}
