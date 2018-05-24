package me.thanel.readtracker

import android.text.InputFilter
import android.text.Spanned

class RangeInputFilter(private val min: Int, private val max: Int) : InputFilter {
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val replacement = source.substring(start, end)
        val newVal = dest.substring(0, dstart) + replacement + dest.substring(dend, dest.length)
        val input = newVal.toIntOrNull() ?: return ""
        if (input in min..max) return null
        return ""
    }
}
