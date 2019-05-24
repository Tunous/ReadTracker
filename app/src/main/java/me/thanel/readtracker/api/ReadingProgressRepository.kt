package me.thanel.readtracker.api

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thanel.goodreadsapi.GoodreadsApiInterface
import me.thanel.goodreadsapi.model.Book
import me.thanel.readtracker.Database
import me.thanel.readtracker.model.BookWithProgress
import me.thanel.readtracker.util.executeAsListLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingProgressRepository @Inject constructor(
    private val api: GoodreadsApiInterface,
    private val database: Database,
    private val userRepository: UserRepository
) {
    suspend fun updateProgressByPageNumber(
        bookId: Long,
        pageNumber: Int,
        reviewBody: String? = null
    ) = withContext(Dispatchers.Default) {
        database.readProgressQueries.updateProgress(pageNumber, bookId)
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
                page = page,
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
        reviewId: Long,
        rating: Int? = null,
        reviewBody: String? = null
    ) = withContext(Dispatchers.Default) {
        api.finishReading(reviewId, rating, reviewBody)
        // TODO: Remove from local database
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
                    status.reviewId
                )
            }
        }

        val currentlyReadingBooks = api.getBooksInShelf(userId, "currently-reading")
        database.transaction {
            // TODO: This might remove too many books if request for books in currently-reading
            //  shelf has another page
            database.bookQueries.deleteBooksWithoutPosition()
            database.readProgressQueries.deleteProgressWithoutBook()
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
