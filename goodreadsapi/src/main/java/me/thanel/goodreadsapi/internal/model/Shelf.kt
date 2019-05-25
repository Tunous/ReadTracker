package me.thanel.goodreadsapi.internal.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "shelf")
internal data class Shelf(
    @Attribute(name = "name") val name: String
)
