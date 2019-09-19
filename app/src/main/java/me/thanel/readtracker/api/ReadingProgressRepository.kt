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
    ) = withContext(Dispatchers.IO) {
        if (hasProgressForBook(bookId)) {
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
                    authors = authors
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
                    authors = bookAuthors
                )
            )
        }.executeAsListLiveData()
    }

    suspend fun finishReading(
        bookId: Long,
        reviewId: Long?,
        rating: Int? = null,
        reviewText: String? = null
    ) = withContext(Dispatchers.Default) {
        if (reviewId != null) {
            finishReadingInternal(bookId, reviewId, rating, reviewText)
            return@withContext
        }

        val userId = userRepository.getUserId()
        val newReviewId = api.getReviewIdForBook(userId, bookId)
        if (newReviewId != null) {
            finishReadingInternal(bookId, newReviewId, rating, reviewText)
        } else {
            TODO("Handle error when trying to fetch review id")
        }
    }

    private suspend fun finishReadingInternal(
        bookId: Long,
        reviewId: Long,
        rating: Int? = null,
        reviewBody: String? = null
    ) {
        api.finishReading(reviewId, rating, reviewBody)
        database.bookQueries.deleteBookWithId(bookId)
    }

    suspend fun synchronizeDatabase() = withContext(Dispatchers.IO) {
        val userId = userRepository.getUserId()
        val (statuses, books) = api.getReadingProgressStatus(userId)

        database.transaction {
            // In local database we might have potentially no longer valid data. Here we are
            // removing all books with progress which should be contained in the result of
            // this API call.
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

        val (currentlyReadingStatuses, currentlyReadingBooks) = api.getBooksInShelf(userId, "currently-reading")
        database.transaction {
            currentlyReadingBooks.forEach { book ->
                insertBook(book, null)
            }

            currentlyReadingStatuses.forEach { status ->
                database.readProgressQueries.insert(
                    id = status.id,
                    bookId = status.bookId,
                    page = status.page,
                    reviewId = status.reviewId
                )
            }
        }

        val (_, booksToRead) = api.getBooksInShelf(userId, "to-read")
        database.transaction {
            booksToRead.forEachIndexed { position, book ->
                insertBook(book, position)
            }
        }
    }

    private fun hasProgressForBook(bookId: Long) =
        database.readProgressQueries.selectProgressForBook(bookId).executeAsOneOrNull() != null

    private fun insertBook(book: Book, position: Int?) {
        database.bookQueries.insert(
            book.id,
            book.title,
            book.numPages,
            book.imageUrl,
            book.authors,
            position
        )
    }
}
