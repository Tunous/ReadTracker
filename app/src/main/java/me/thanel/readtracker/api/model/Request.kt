package me.thanel.readtracker.api.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "Request")
data class Request(
    @PropertyElement(name = "authentication") val authentication: Boolean,
    @PropertyElement(name = "key") val key: String,
    @PropertyElement(name = "method") val method: String
)
