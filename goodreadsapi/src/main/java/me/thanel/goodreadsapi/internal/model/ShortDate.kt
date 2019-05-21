package me.thanel.goodreadsapi.internal.model

import android.text.format.DateFormat
import java.util.Date

internal class ShortDate(date: Date) {
    private val formattedDate by lazy {
        DateFormat.format("yyyy-MM-dd", date).toString()
    }

    override fun toString(): String {
        return formattedDate
    }

    companion object {
        fun now() = ShortDate(Date())
    }
}
