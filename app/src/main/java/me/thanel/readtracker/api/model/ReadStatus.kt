package me.thanel.readtracker.api.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "read_status")
data class ReadStatus(
    @PropertyElement(name = "id") val id: Long,
    @PropertyElement(name = "status") val status: String,
    @Element(name = "review") val review: Review
)
