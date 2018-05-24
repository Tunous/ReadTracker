package me.thanel.readtracker.api.model

import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "author")
data class Author(
    @PropertyElement(name = "id") val id: Long,
    @PropertyElement(name = "name") val name: String
//    @PropertyElement(name = "role", required = false) val role: Int,
//    @PropertyElement(name = "image_url") val imageUrl: String,
//    @PropertyElement(name = "small_image_url") val smallImageUrl: String,
//    @PropertyElement(name = "link") val link: String,
//    @PropertyElement(name = "average_rating") val averageRating: Float,
//    @PropertyElement(name = "ratings_count") val ratingsCount: Long,
//    @PropertyElement(name = "text_reviews_count") val textReviewsCount: Long
)
