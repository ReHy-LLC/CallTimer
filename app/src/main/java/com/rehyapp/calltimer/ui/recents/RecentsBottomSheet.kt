package com.rehyapp.calltimer.ui.recents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.rehyapp.calltimer.R
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class RecentsBottomSheet : BottomSheetDialogFragment() {

    //koin dependency injected ViewModel
    private val recentsViewModel by sharedViewModel<RecentsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(R.layout.bottom_sheet_recents, container, false)
        val btnClearAll = rootView.findViewById<MaterialButton>(R.id.bottom_sheet_recents_clear_all)
        val btnCancel = rootView.findViewById<MaterialButton>(R.id.bottom_sheet_recents_cancel)

        btnClearAll.setOnClickListener {
            recentsViewModel.recentsClearAll()
            this.dismiss()
        }

        btnCancel.setOnClickListener {
            this.dismiss()
        }

        return rootView
    }

}