package com.rehyapp.calltimer.ui.recents

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.BlockedNumberContract.BlockedNumbers
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rehyapp.calltimer.R
import com.rehyapp.calltimer.call_logging.LogFormatter
import com.rehyapp.calltimer.call_logging.LogManager
import com.rehyapp.calltimer.call_logging.RecentsUIGroupingsObject
import com.rehyapp.calltimer.databinding.HeaderRowBinding
import com.rehyapp.calltimer.databinding.RecentsItemBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@SuppressLint("MissingPermission")
class RecentsAdapter : ListAdapter<RecentsUIGroupingsObject, RecyclerView.ViewHolder>(Companion) {

    init {
        setHasStableIds(true)
    }

    class RecentsViewHolder(private val recentBinding: RecentsItemBinding) :
        RecyclerView.ViewHolder(recentBinding.root) {

        fun bindLog(log: RecentsUIGroupingsObject) {
            recentBinding.log = log
            recentBinding.formatter = LogFormatter(itemView.context.applicationContext)
            recentBinding.executePendingBindings()
            itemView.setOnClickListener {
                val action =
                    RecentsFragmentDirections.actionNavigationRecentsToCallDetailsFragment(log.groupCallIds.toLongArray())
                it.findNavController().navigate(action)
            }
            val contextMenuClickListener = MenuItem.OnMenuItemClickListener {
                when (it.itemId) {
                    R.id.recents_item_menu_copy -> {
                        val clipboardManager =
                            itemView.context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(log.groupNumber, log.groupNumber)
                        clipboardManager.setPrimaryClip(clip)
                        true
                    }
                    R.id.recents_item_menu_block -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            val values = ContentValues()
                            values.put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, log.groupNumber)
                            itemView.context.contentResolver.insert(
                                BlockedNumbers.CONTENT_URI,
                                values
                            )
                        }
                        true
                    }
                    R.id.recents_item_menu_unblock -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            val values = ContentValues()
                            values.put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, log.groupNumber)
                            val uri = itemView.context.contentResolver.insert(
                                BlockedNumbers.CONTENT_URI,
                                values
                            )!!
                            itemView.context.contentResolver.delete(uri, null, null)
                        }
                        true
                    }
                    R.id.recents_item_menu_delete -> {
                        GlobalScope.launch {
                            LogManager(itemView.context).deleteLogFromRecentsObject(log)
                        }
                        true
                    }
                    else -> false
                }
            }
            itemView.setOnCreateContextMenuListener { menu, v, _ ->
                menu.setHeaderTitle(
                    LogFormatter(itemView.context.applicationContext).formatPhoneNumber(
                        log.groupNumber
                    )
                )
                menu.add(
                    0,
                    R.id.recents_item_menu_copy,
                    1,
                    v.context.getString(R.string.copy_number)
                ).setOnMenuItemClickListener(contextMenuClickListener)
                var isBlocked = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val selection =
                        BlockedNumbers.COLUMN_ORIGINAL_NUMBER + " = '" + log.groupNumber + "'"
                    v.context.contentResolver.query(
                        BlockedNumbers.CONTENT_URI, arrayOf(
                            BlockedNumbers.COLUMN_ID,
                            BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
                            BlockedNumbers.COLUMN_E164_NUMBER
                        ), selection, null, null
                    )!!.apply {
                        isBlocked = count > 1
                        close()
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (isBlocked) {
                        menu.add(
                            0,
                            R.id.recents_item_menu_unblock,
                            2,
                            v.context.getString(R.string.unblock)
                        ).setOnMenuItemClickListener(contextMenuClickListener)
                    } else {
                        menu.add(
                            0,
                            R.id.recents_item_menu_block,
                            2,
                            v.context.getString(R.string.block)
                        ).setOnMenuItemClickListener(contextMenuClickListener)
                    }
                }
                menu.add(0, R.id.recents_item_menu_delete, 2, v.context.getString(R.string.delete))
                    .setOnMenuItemClickListener(contextMenuClickListener)
            }
            recentBinding.logActionIcon.setOnClickListener {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:".plus(log.groupNumber)))
                it.context.startActivity(intent)
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
            return oldItem.isHeader == newItem.isHeader
        }

        override fun areContentsTheSame(
            oldItem: RecentsUIGroupingsObject,
            newItem: RecentsUIGroupingsObject
        ): Boolean {
            return oldItem.groupUniqueId == newItem.groupUniqueId && oldItem.isNew == newItem.isNew
        }

    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)

        return if (item.isHeader) {
            ITEM_VIEW_TYPE_HEADER
        } else {
            ITEM_VIEW_TYPE_ITEM
        }
    }

    override fun getItemCount(): Int = currentList.size

    override fun getItemId(position: Int): Long = getItem(position).groupUniqueId

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