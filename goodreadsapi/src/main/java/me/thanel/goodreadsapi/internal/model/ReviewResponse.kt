package me.thanel.goodreadsapi.internal.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Path
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "GoodreadsResponse")
internal data class ReviewResponse(
    @Element(name = "Request") val request: Request,
    @Path("reviews") @Element val reviews: List<Review>?
)

@Xml(name = "review")
internal data class Review(
    @PropertyElement(name = "id") val id: Long,
    @Element(name = "book") val book: Book
)
