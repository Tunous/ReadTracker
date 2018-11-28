package me.thanel.readtracker.api.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "user_status")
data class UserStatus(
    @PropertyElement(name = "id") val id: Long,
    @PropertyElement(name = "page") val page: Int,
    @PropertyElement(name = "comments_count") val commentCount: Int,
    @PropertyElement(name = "body") val body: String?,
//    @PropertyElement(name = "created_at") val createdAt: Date?,
//    @PropertyElement(name = "updated_at") val updatedAt: Date?,
//    @PropertyElement(name = "last_comment_at") val lastCommentAt: Date?,
    @PropertyElement(name = "percent") val percent: Int,
    @Element(name = "book") val book: Book
)