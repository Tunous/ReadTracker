package me.thanel.readtracker.model

import me.thanel.goodreadsapi.model.Book

data class BookWithProgress(
    val progressId: Long?,
    val page: Int,
    val reviewId: Long?,
    val book: Book
)
