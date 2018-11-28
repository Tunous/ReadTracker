package me.thanel.readtracker.api.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "user_shelf")
data class UserShelf(
    @PropertyElement(name = "id") val id: Long,
    @PropertyElement(name = "name") val name: String?
)
