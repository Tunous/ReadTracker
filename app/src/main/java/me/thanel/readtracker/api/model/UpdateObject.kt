package me.thanel.readtracker.api.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "object")
data class UpdateObject(
    @Element(name = "read_status") val readStatus: ReadStatus,
    @Element(name = "book") val book: Book
)
