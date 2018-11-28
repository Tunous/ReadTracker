package me.thanel.goodreadsapi.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "user_status")
data class UserStatus(
    @PropertyElement(name = "id") val id: Long,
    @PropertyElement(name = "page") val page: Int,
    @PropertyElement(name = "percent") val percent: Int,
    @Element(name = "book") val book: Book
)