package com.rehyapp.calltimer.ui

import android.graphics.Typeface
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amulyakhare.textdrawable.TextDrawable
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.rehyapp.calltimer.R
import java.util.regex.Pattern


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

@BindingAdapter(value = ["app:imageText", "app:imageUri"], requireAll = true)
fun loadImage(view: ImageView, imageText: String, imageUri: String) {
    if (!imageUri.contentEquals("NULL") && !imageUri.contentEquals("")) {
        view.setImageURI(Uri.parse(imageUri))
        view.scaleType = ImageView.ScaleType.FIT_CENTER
    } else if (!Pattern.matches("[a-zA-Z]+", imageText[0].toString())) {
        view.setImageDrawable(ContextCompat.getDrawable(view.context, R.drawable.ic_account))
        view.scaleType = ImageView.ScaleType.CENTER_INSIDE
        view.setBackgroundColor(ColorGenerator.MATERIAL.getColor(imageText))
    } else {
        val generator = ColorGenerator.MATERIAL
        view.setImageDrawable(
            TextDrawable.builder().beginConfig().fontSize(100).useFont(Typeface.SANS_SERIF)
                .endConfig()
                .buildRound(imageText[0].toString(), generator.getColor(imageText))
        )
    }
}

@BindingAdapter(value = ["app:textStyle"])
fun setTextStyle(v: TextView, style: Int) {
    v.setTypeface(null, style)
}