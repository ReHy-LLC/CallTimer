package com.rehyapp.calltimer.ui.recents

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.calllogging.LogObject
import java.text.DateFormat
import java.util.*

@SuppressLint("MissingPermission")
class RecentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var logCallId: MaterialTextView = itemView.findViewById(R.id.log_call_id)
    private var logTypeImage: AppCompatImageView = itemView.findViewById(R.id.log_type_image)
    private var logNameNumber: MaterialTextView = itemView.findViewById(R.id.log_name_number)
    private var logDurationType: MaterialTextView = itemView.findViewById(R.id.log_duration_type)
    private var logDateTime: MaterialTextView = itemView.findViewById(R.id.log_date_time)
    private var logReturnCall: AppCompatImageView = itemView.findViewById(R.id.log_return_call)

    fun bind(log: LogObject) {

        val date = Date(log.date)
        val dateFormat: DateFormat =
            DateFormat.getDateTimeInstance(DateFormat.ERA_FIELD, DateFormat.SHORT)

        logCallId.text = log.callId.toString()
        logNameNumber.text = log.number
        logDurationType.text = log.duration.toString()
        logDateTime.text = dateFormat.format(date)

        logReturnCall.setOnClickListener {
            it.context.startActivity(
                Intent(
                    Intent.ACTION_CALL,
                    Uri.fromParts("tel", log.number, null)
                )
            )
        }

        when (log.type) {
            CallLog.Calls.INCOMING_TYPE -> logTypeImage.setImageResource(R.drawable.ic_phone_incoming_answered)
            CallLog.Calls.OUTGOING_TYPE -> logTypeImage.setImageResource(R.drawable.ic_phone_outgoing)
            CallLog.Calls.MISSED_TYPE -> logTypeImage.setImageResource(R.drawable.ic_phone_incoming_missed)
        }

    }
}

class RecentsAdapter(var logs: List<LogObject>) : RecyclerView.Adapter<RecentsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RecentsViewHolder (
        LayoutInflater.from(parent.context).inflate(R.layout.recents_item, parent, false))

    override fun getItemCount() = logs.size

    override fun onBindViewHolder(holder: RecentsViewHolder, position: Int) {
        holder.bind(logs[position])
    }

}