package me.thanel.readtracker.ui.util

import android.text.Editable

fun Editable?.toIntOrElse(elseBlock: () -> Int): Int {
    return this?.toString()?.toIntOrNull() ?: elseBlock()
}