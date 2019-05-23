package me.thanel.readtracker.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thanel.goodreadsapi.GoodreadsApi
import me.thanel.goodreadsapi.model.Book
import me.thanel.readtracker.Database
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingProgressRepository @Inject constructor(
    private val api: GoodreadsApi,
    private val database: Database,
    private val userRepository: UserRepository
) {
    suspend fun updateProgressByPageNumber(
        bookId: Long,
        pageNumber: Int,
        reviewBody: String? = null
    ) = withContext(Dispatchers.Default) {
        database.readProgressQueries.updateProgressByPageNumber(pageNumber, bookId)
        api.updateProgressByPageNumber(bookId, pageNumber, reviewBody)
    }

    suspend fun updateProgressByPercent(
        bookId: Long,
        percent: Int,
        reviewBody: String? = null
    ) = withContext(Dispatchers.Default) {
        database.readProgressQueries.updateProgressByPercent(percent, bookId)
        api.updateProgressByPercent(bookId, percent, reviewBody)
    }

    suspend fun finishReading(
        reviewId: Long,
        rating: Int? = null,
        reviewBody: String? = null
    ) = withContext(Dispatchers.Default) {
        api.finishReading(reviewId, rating, reviewBody)
    }

    suspend fun synchronizeDatabase() = withContext(Dispatchers.Default) {
        val userId = userRepository.getUserId()
        val (statuses, books) = api.getReadingProgressStatus(userId)

        database.transaction {
            books.forEach { book ->
                insertBook(book, null)
            }

            statuses.forEach { status ->
                database.readProgressQueries.insert(
                    status.id,
                    status.bookId,
                    status.page,
                    status.percent,
                    status.reviewId
                )
            }
        }


        val currentlyReadingBooks = api.getBooksInShelf(userId, "currently-reading")
        database.transaction {
            currentlyReadingBooks.forEach { book ->
                insertBook(book, null)
            }
        }

        val booksToRead = api.getBooksInShelf(userId, "to-read")
        database.transaction {
            booksToRead.withIndex().forEach { (position, book) ->
                insertBook(book, position)
            }
        }
    }

    private fun insertBook(book: Book, position: Int?) {
        database.bookQueries.insert(
            book.id,
            book.title,
            book.numPages,
            book.imageUrl,
            book.authors,
            position = position
        )
    }
}
