package com.rehyapp.calltimer.ui.recents

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rehyapp.calltimer.calllogging.RecentsUIGroupingsObject
import com.rehyapp.calltimer.databinding.HeaderRowBinding
import com.rehyapp.calltimer.databinding.RecentsItemBinding

@SuppressLint("MissingPermission")
class RecentsAdapter : ListAdapter<RecentsUIGroupingsObject, RecyclerView.ViewHolder>(Companion) {

    init {
        setHasStableIds(true)
    }

    class RecentsViewHolder(private val recentBinding: RecentsItemBinding) :
        RecyclerView.ViewHolder(recentBinding.root) {

        fun bindLog(log: RecentsUIGroupingsObject) {
            recentBinding.log = log
            recentBinding.executePendingBindings()
            itemView.setOnClickListener {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:".plus(log.groupNumber)))
                it.context.startActivity(intent)
            }
            recentBinding.logInfoIcon.setOnClickListener {
                val action =
                    RecentsFragmentDirections.actionNavigationRecentsToCallDetailsFragment(log.groupCallIds.toLongArray())
                it.findNavController().navigate(action)
            }
        }

        fun getRecentGroupLog(): RecentsUIGroupingsObject {
            return recentBinding.log!!
        }

        companion object {
            fun from(parent: ViewGroup): RecentsViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RecentsItemBinding.inflate(layoutInflater)
                return RecentsViewHolder(binding)
            }
        }
    }

    class HeaderViewHolder(private val headerBinding: HeaderRowBinding) :
        RecyclerView.ViewHolder(headerBinding.root) {

        fun bindHeader(log: RecentsUIGroupingsObject) {
            headerBinding.log = log
            headerBinding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = HeaderRowBinding.inflate(layoutInflater)
                return HeaderViewHolder(binding)
            }
        }
    }

    companion object : DiffUtil.ItemCallback<RecentsUIGroupingsObject>() {
        const val ITEM_VIEW_TYPE_HEADER = 0
        const val ITEM_VIEW_TYPE_ITEM = 1
        override fun areItemsTheSame(
            oldItem: RecentsUIGroupingsObject,
            newItem: RecentsUIGroupingsObject
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: RecentsUIGroupingsObject,
            newItem: RecentsUIGroupingsObject
        ): Boolean {
            return oldItem.groupCallIds.toString() == newItem.groupCallIds.toString()
        }

    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)

        return if (item.groupIsHeader == true) {
            ITEM_VIEW_TYPE_HEADER
        } else {
            ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun getItemCount(): Int = currentList.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> RecentsViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentLog = getItem(position)
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_HEADER -> {
                val headerHolder: HeaderViewHolder = holder as HeaderViewHolder
                headerHolder.bindHeader(currentLog)
            }
            ITEM_VIEW_TYPE_ITEM -> {
                val recentsHolder: RecentsViewHolder = holder as RecentsViewHolder
                recentsHolder.bindLog(currentLog)
            }
        }
    }
}