package me.thanel.readtracker.api.model

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "user")
data class User(
    @PropertyElement(name = "id") val id: Long,
    @PropertyElement(name = "name") val name: String,
    @PropertyElement(name = "link") val link: String,
    @PropertyElement(name = "image_url") val imageUrl: String,
    @PropertyElement(name = "small_image_url") val smallImageUrl: String,
    @PropertyElement(name = "about") val about: String,
    @PropertyElement(name = "age") val age: Int,
    @PropertyElement(name = "gender") val gender: String,
    @PropertyElement(name = "location") val location: String,
    @PropertyElement(name = "website") val website: String,
    @PropertyElement(name = "joined") val joined: String,
    @Element(name = "user_shelves") val shelves: List<UserShelf>,
    @Element(name = "updates") val updates: List<Update>
)
