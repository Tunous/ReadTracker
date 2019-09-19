package me.thanel.goodreadsapi.model

data class Book(
    val id: Long,
    val title: String,
    val numPages: Int,
    val imageUrl: String?,
    val authors: String?
)
