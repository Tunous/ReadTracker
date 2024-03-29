package me.thanel.goodreadsapi.internal.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "GoodreadsResponse")
internal data class ReviewResponse(
    @Element(name = "Request") val request: Request,
    @Element val review: Review?
)
