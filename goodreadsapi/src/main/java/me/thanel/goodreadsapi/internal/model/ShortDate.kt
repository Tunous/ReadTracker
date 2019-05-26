package me.thanel.goodreadsapi.internal.model

import android.text.format.DateFormat
import java.util.Date

internal class ShortDate(date: Date) {
    private val formattedDate by lazy {
        formatter(date)
    }

    override fun toString() = formattedDate

    companion object {
        internal var formatter: (Date) -> String = {
            DateFormat.format("yyyy-MM-dd", it).toString()
        }

        fun now() = ShortDate(Date())
    }
}
