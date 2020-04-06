package com.rehyapp.calltimer.ui.recents

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.calllogging.LogObject


class RecentsAdapter(var logs: List<LogObject>) : RecyclerView.Adapter<RecentsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RecentsViewHolder (
        LayoutInflater.from(parent.context).inflate(R.layout.recents_item, parent, false))

    override fun getItemCount() = logs.size

    override fun onBindViewHolder(holder: RecentsViewHolder, position: Int) {
        holder.bind(logs[position])
    }


}