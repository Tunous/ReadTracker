package me.thanel.readtracker.ui.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.content.res.use
import androidx.core.graphics.ColorUtils
import kotlin.math.roundToInt

@ColorInt
@SuppressLint("Recycle")
fun Context.getColorFromAttr(
    @AttrRes attrResId: Int,
    @FloatRange(from = 0.0, to = 1.0) alpha: Float = 1f
): Int {
    obtainStyledAttributes(intArrayOf(attrResId)).use {
        val color = it.getColor(0, Color.TRANSPARENT)
        if (alpha != 1f) {
            return ColorUtils.setAlphaComponent(color, (alpha * 255).roundToInt())
        }
        return color
    }
}
