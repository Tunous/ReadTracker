package me.thanel.readtracker.ui.util

import android.view.View
import androidx.core.view.isVisible

fun View.animateShow() {
    if (isVisible) return
    alpha = 0f
    isVisible = true
    animate().alpha(1f).setDuration(1000).start()
}
