package me.thanel.readtracker.ui.util

import android.text.InputFilter
import android.text.Spanned

class RangeInputFilter(private val range: IntRange) : InputFilter {

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val prefix = dest.substring(0, dstart)
        val replacement = source.substring(start, end)
        val suffix = dest.substring(dend, dest.length)
        val newText = prefix + replacement + suffix
        val number = newText.toIntOrNull() ?: return ""
        if (range.contains(number)) {
            return null
        }
        return ""
    }
}
