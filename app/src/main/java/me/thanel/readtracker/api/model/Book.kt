package me.thanel.readtracker.api.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Path
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml
data class Book(
    @PropertyElement val id: Long,
    @PropertyElement val title: String,
    @PropertyElement val isbn: String,
    @PropertyElement(name = "image_url") val imageUrl: String,
    @Path("authors") @Element val authors: List<Author>
)
