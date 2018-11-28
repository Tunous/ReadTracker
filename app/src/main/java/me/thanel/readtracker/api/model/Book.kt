package me.thanel.readtracker.api.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Path
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml
data class Book(
    @PropertyElement val id: Long,
    @PropertyElement val title: String,
    @PropertyElement val isbn: String?,
    @PropertyElement(name = "image_url") val imageUrl: String,
    @Path("authors") @Element val authors: List<Author>,
    @PropertyElement(name = "num_pages") val numPages: Int,
    @PropertyElement(name = "average_rating") val averageRating: Double?,
    @PropertyElement(name = "ratings_count") val ratingsCount: Long?
)
