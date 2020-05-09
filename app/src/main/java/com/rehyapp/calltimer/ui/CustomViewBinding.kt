package com.rehyapp.calltimer.ui

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

@BindingAdapter(value = ["setAdapter"])
fun RecyclerView.bindRecyclerViewAdapter(adapter: RecyclerView.Adapter<*>) {
    this.run {
        this.setHasFixedSize(true)
        this.adapter = adapter
    }
}

@BindingAdapter(value = ["app:hasPermission", "app:hasData"], requireAll = true)
fun evalShow(view: View, hasPermission: Boolean, hasData: Boolean) {
    view.visibility = if (hasPermission && hasData) View.VISIBLE else View.GONE
}

@BindingAdapter(value = ["app:hasPermissionHide", "app:hasDataHide"], requireAll = true)
fun evalGone(view: View, hasPermission: Boolean, hasData: Boolean) {
    view.visibility = if (!hasPermission || !hasData) View.VISIBLE else View.GONE
}