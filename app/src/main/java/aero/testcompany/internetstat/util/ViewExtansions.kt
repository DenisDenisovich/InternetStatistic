package aero.testcompany.internetstat.util

import android.content.Context
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.IntegerRes
import androidx.core.content.ContextCompat

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun Context.color(resource: Int) = ContextCompat.getColor(this, resource)