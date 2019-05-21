package me.thanel.goodreadsapi.internal.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "author")
internal data class Author(
    @PropertyElement(name = "id") val id: Long,
    @PropertyElement(name = "name") val name: String
)
