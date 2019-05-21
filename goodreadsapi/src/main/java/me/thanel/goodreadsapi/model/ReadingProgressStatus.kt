package me.thanel.goodreadsapi.model

data class ReadingProgressStatus(
    val id: Long,
    val bookId: Long,
    val page: Int?,
    val percent: Int?,
    val reviewId: Long
)
