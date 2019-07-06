package me.thanel.readtracker.model

import androidx.recyclerview.widget.DiffUtil
import me.thanel.goodreadsapi.model.Book

data class BookWithProgress(
    val progressId: Long?,
    val page: Int,
    val reviewId: Long?,
    val book: Book
) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BookWithProgress>() {
            override fun areItemsTheSame(
                oldItem: BookWithProgress,
                newItem: BookWithProgress
            ) = oldItem.book.id == newItem.book.id

            override fun areContentsTheSame(
                oldItem: BookWithProgress,
                newItem: BookWithProgress
            ) = oldItem == newItem

            override fun getChangePayload(
                oldItem: BookWithProgress,
                newItem: BookWithProgress
            ): Any? {
                val payloads = mutableListOf<BookWithProgressChange>()
                if (oldItem.book.title != newItem.book.title || oldItem.book.authors != newItem.book.authors) {
                    payloads.add(BookWithProgressChange.BookInformation)
                }
                if (oldItem.book.imageUrl != newItem.book.imageUrl) {
                    payloads.add(BookWithProgressChange.CoverImage)
                }
                if (oldItem.page != newItem.page || oldItem.book.numPages != newItem.book.numPages) {
                    payloads.add(BookWithProgressChange.ProgressInformation)
                }
                return payloads
            }
        }
    }
}
