package com.rehyapp.calltimer.ui.recents

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.calllogging.LogObject
import com.rehyapp.calltimer.calllogging.LogsManager
import java.text.DateFormat
import java.util.*

@SuppressLint("MissingPermission")
class RecentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var logTypeImage: AppCompatImageView = itemView.findViewById(R.id.log_type_image)
    private var logNameNumber: MaterialTextView = itemView.findViewById(R.id.log_name_number)
    private var logDurationType: MaterialTextView = itemView.findViewById(R.id.log_duration_type)
    private var logDateTime: MaterialTextView = itemView.findViewById(R.id.log_date_time)
    private var logReturnCall: AppCompatImageView = itemView.findViewById(R.id.log_return_call)

    fun bind(log: LogObject) {

        val date = Date(log.date)
        val dateFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.ERA_FIELD, DateFormat.SHORT)

        logNameNumber.text = log.contactName
        logDurationType.text = log.coolDuration
        logDateTime.text = dateFormat.format(date)

        logReturnCall.setOnClickListener {
            it.context.startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", log.number, null)))
        }

        when(log.type) {
            LogsManager.INCOMING -> logTypeImage.setImageResource(R.drawable.ic_phone_incoming_answered)
            LogsManager.OUTGOING -> logTypeImage.setImageResource(R.drawable.ic_phone_outgoing)
            LogsManager.MISSED   -> logTypeImage.setImageResource(R.drawable.ic_phone_incoming_missed)
        }

    }

}