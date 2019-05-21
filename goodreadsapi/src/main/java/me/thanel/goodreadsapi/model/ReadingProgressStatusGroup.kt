package me.thanel.goodreadsapi.model

data class ReadingProgressStatusGroup(
    val statuses: List<ReadingProgressStatus>,
    val books: List<Book>
)
