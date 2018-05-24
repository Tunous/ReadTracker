package me.thanel.readtracker.api.transformer

import com.tickaroo.tikxml.TypeConverter
import java.text.DateFormat
import java.util.*

class DateFormatTypeConverter(private val dateFormat: DateFormat) : TypeConverter<Date> {
    override fun write(value: Date): String = dateFormat.format(value)

    override fun read(value: String): Date = dateFormat.parse(value)
}
