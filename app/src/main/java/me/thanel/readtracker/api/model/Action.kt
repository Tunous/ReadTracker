package me.thanel.readtracker.api.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "action")
data class Action(
    @Attribute(name = "type") val type: String,
    @PropertyElement(name = "rating") val rating: Int
)
