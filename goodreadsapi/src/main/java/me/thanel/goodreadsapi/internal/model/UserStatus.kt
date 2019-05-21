package me.thanel.goodreadsapi.internal.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "user_status")
internal data class UserStatus(
    @PropertyElement(name = "id") val id: Long,
    @PropertyElement(name = "page") val page: Int?,
    @PropertyElement(name = "percent") val percent: Int?,
    @PropertyElement(name = "review_id") val reviewId: Long,
    @Element(name = "book") val book: Book
)
