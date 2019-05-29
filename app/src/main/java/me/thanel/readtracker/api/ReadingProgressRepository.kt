package me.thanel.readtracker.api

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thanel.goodreadsapi.GoodreadsApi
import me.thanel.goodreadsapi.model.Book
import me.thanel.readtracker.database.Database
import me.thanel.readtracker.database.executeAsListLiveData
import me.thanel.readtracker.model.BookWithProgress
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
        val hasProgress = database.readProgressQueries.hasProgress(bookId).executeAsOneOrNull() == 1L
        if (hasProgress) {
            database.readProgressQueries.updateProgress(pageNumber, bookId)
        } else {
            // TODO: Save new progress in database (what to do with reviewId)
            api.startReadingBook(bookId)
        }
        api.updateProgressByPageNumber(bookId, pageNumber, reviewBody)
    }

    fun getBooksToReadAsLiveData(): LiveData<List<BookWithProgress>> {
        return database.bookQueries.selectBooksToRead { id, title, numPages, imageUrl, authors ->
            BookWithProgress(
                progressId = null,
                page = 0,
                reviewId = null,
                book = Book(
                    id = id,
                    numPages = numPages,
                    title = title,
                    imageUrl = imageUrl,
                    authors = authors,
                    isCurrentlyReading = false
                )
            )
        }.executeAsListLiveData()
    }

    fun getBooksWithProgressAsLiveData(): LiveData<List<BookWithProgress>> {
        return database.readProgressQueries.selectWithBookInformation { id, page, reviewId, bookId, numPages, bookTitle, bookImageUrl, bookAuthors ->
            BookWithProgress(
                progressId = id,
                page = page ?: 0,
                reviewId = reviewId,
                book = Book(
                    id = bookId,
                    numPages = numPages,
                    title = bookTitle,
                    imageUrl = bookImageUrl,
                    authors = bookAuthors,
                    isCurrentlyReading = true
                )
            )
        }.executeAsListLiveData()
    }

    suspend fun finishReading(
        reviewId: Long,
        rating: Int? = null,
        reviewBody: String? = null
    ) = withContext(Dispatchers.Default) {
        api.finishReading(reviewId, rating, reviewBody)
        database.readProgressQueries.deleteBookForReview(reviewId)
    }

    suspend fun synchronizeDatabase() = withContext(Dispatchers.Default) {
        val userId = userRepository.getUserId()
        val (statuses, books) = api.getReadingProgressStatus(userId)

        database.transaction {
            // In local database we might have potentially no longer valid data, here we are
            // removing as much as possible. It is safe to do that as the performed api call
            // should have returned more up-to date data.
            database.readProgressQueries.deleteAll()
            database.bookQueries.deleteBooksWithProgress()

            books.forEach { book ->
                insertBook(book, null)
            }

            statuses.forEach { status ->
                database.readProgressQueries.insert(
                    status.id,
                    status.bookId,
                    status.page,
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
            position,
            book.isCurrentlyReading
        )
    }
}
