package me.thanel.readtracker.api.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "GoodreadsResponse")
data class UserResponse(
    @Element(name = "Request") val request: Request,
    @Element(name = "user") val user: User
)
