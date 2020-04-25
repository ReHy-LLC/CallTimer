package com.rehyapp.calltimer.ui.recents

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.calllogging.RecentsUIGroupingsObject

@SuppressLint("MissingPermission")
class RecentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var logTypeImage: AppCompatImageView = itemView.findViewById(R.id.log_type_image)
    private var logTopText: MaterialTextView = itemView.findViewById(R.id.log_top_text)
    private var logBottomText: MaterialTextView = itemView.findViewById(R.id.log_bottom_text)
    private var logDayDateTime: MaterialTextView = itemView.findViewById(R.id.log_day_date_time)
    private var logInfoIcon: AppCompatImageView = itemView.findViewById(R.id.log_info_icon)

    fun bind(log: RecentsUIGroupingsObject) {

        logTopText.text = log.groupTopText
        logBottomText.text = log.groupBottomText
        logDayDateTime.text = log.groupTimeDayDate

        logInfoIcon.setOnClickListener {
            val action =
                RecentsFragmentDirections.actionNavigationRecentsToCallDetailsFragment(log.groupCallIds.toLongArray())
            it.findNavController().navigate(action)
            Toast.makeText(
                logTypeImage.context,
                "Will open call details screen!",
                Toast.LENGTH_SHORT
            ).show()
        }

        logTypeImage.setImageResource(log.groupIconDrawableId)

        if (log.groupTopTextRed == true) {
            logTopText.setTextColor(logTypeImage.context.getColor(android.R.color.holo_red_dark))
        } else {
            logTopText.setTextColor(logTypeImage.context.getColor(android.R.color.black))
        }

    }
}

class RecentsAdapter(private val logs: MutableList<RecentsUIGroupingsObject>) :
    RecyclerView.Adapter<RecentsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RecentsViewHolder (
        LayoutInflater.from(parent.context).inflate(R.layout.recents_item, parent, false))

    override fun getItemCount() = logs.size

    override fun onBindViewHolder(holder: RecentsViewHolder, position: Int) {
        holder.bind(logs[position])
    }

}