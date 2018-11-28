package me.thanel.readtracker.api.model

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "update")
data class Update(
    @Attribute(name = "type") val type: String//,
//    @PropertyElement(name = "action_text") val actionText: String?,
//    @PropertyElement(name = "link") val link: String,
//    @PropertyElement(name = "image_url") val imageUrl: String,
//    @Element(name = "actor") val actor: Actor,
//    @PropertyElement(name = "updated_at") val updatedAt: Date,
//    @Element(name = "action") val action: Action,
//    @Element(name = "object") val updateObject: UpdateObject
)
