package me.thanel.goodreadsapi.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Path
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "user")
data class User(
    @Attribute(name = "id") val id: Long,
    @PropertyElement(name = "name") val name: String,
    @Path("user_statuses") @Element val userStatuses: List<UserStatus>?
)