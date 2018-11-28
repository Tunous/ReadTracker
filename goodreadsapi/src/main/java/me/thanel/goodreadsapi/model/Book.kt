package me.thanel.goodreadsapi.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Path
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml
data class Book(
    @PropertyElement(name = "id") val id: Long,
    @PropertyElement(name = "title") val title: String,
    @PropertyElement(name = "num_pages") val numPages: Int,
    @PropertyElement(name = "image_url") val imageUrl: String,
    @Path("authors") @Element val authors: List<Author>
)
