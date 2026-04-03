package com.example.miniprj2.util


import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView

object ViewStyler {

    fun styleBadge(view: TextView, colorHex: String) {
        val shape = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(parseColor(colorHex))
        }
        view.background = shape
    }

    private fun parseColor(colorHex: String): Int {
        return try {
            Color.parseColor(colorHex)
        } catch (_: IllegalArgumentException) {
            Color.parseColor("#F97316")
        }
    }
}