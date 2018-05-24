package me.thanel.readtracker.api.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml

@Xml
data class Reviews(
    @Attribute val start: Int,
    @Attribute val end: Int,
    @Attribute val total: Int,
    @Element val reviews: List<Review>?
)
