package com.rehyapp.calltimer.ui

import android.graphics.Typeface
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator


@BindingAdapter(value = ["setAdapter"])
fun RecyclerView.bindRecyclerViewAdapter(adapter: RecyclerView.Adapter<*>) {
    this.run {
        this.setHasFixedSize(true)
        this.adapter = adapter
    }
}

@BindingAdapter(value = ["app:hasPermission", "app:hasData"], requireAll = true)
fun evalShow(view: View, hasPermission: Boolean = true, hasData: Boolean = true) {
    view.visibility = if (hasPermission && hasData) View.VISIBLE else View.GONE
}

@BindingAdapter(value = ["app:hasPermissionHide", "app:hasDataHide"], requireAll = true)
fun evalGone(view: View, hasPermission: Boolean = false, hasData: Boolean = false) {
    view.visibility = if (!hasPermission || !hasData) View.VISIBLE else View.GONE
}

@BindingAdapter(value = ["app:topTextForAvatar", "app:imageUri"], requireAll = true)
fun loadImage(view: ImageView, topTextForAvatar: String, imageUri: String?) {
    if (imageUri != null && !imageUri.contentEquals("NULL")) {
        view.setImageURI(Uri.parse(imageUri))
    } else {
        //view.setImageDrawable(AvatarGenerator.avatarImage(view.context.applicationContext, 500, AvatarConstants.CIRCLE, topTextForAvatar))
        val generator = ColorGenerator.MATERIAL
        view.setImageDrawable(
            TextDrawable.builder().beginConfig().fontSize(100).useFont(Typeface.SANS_SERIF)
                .endConfig()
                .buildRound(topTextForAvatar[0].toString(), generator.getColor(topTextForAvatar))
        )
    }
}