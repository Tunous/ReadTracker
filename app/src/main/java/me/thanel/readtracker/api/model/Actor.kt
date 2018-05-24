package me.thanel.readtracker.api.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "actor")
data class Actor(
    @PropertyElement(name = "id") val id: Long,
    @PropertyElement(name = "name") val name: String,
    @PropertyElement(name = "image_url") val imageUrl: String,
    @PropertyElement(name = "link") val link: String
)
