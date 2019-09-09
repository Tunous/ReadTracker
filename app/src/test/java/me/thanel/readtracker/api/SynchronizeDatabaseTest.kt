package me.thanel.readtracker.api

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import me.thanel.goodreadsapi.model.Book
import me.thanel.goodreadsapi.model.ReadingProgressStatus
import me.thanel.goodreadsapi.model.ReadingProgressStatusGroup
import me.thanel.readtracker.database.ReadProgress
import me.thanel.readtracker.testbase.BaseRepositoryTest
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`

@RunWith(AndroidJUnit4::class)
class SynchronizeDatabaseTest : BaseRepositoryTest() {

    private lateinit var readingProgressRepository: ReadingProgressRepository

    @Before
    fun setup() {
        val userRepository = UserRepository(goodreadsApi)
        readingProgressRepository =
            ReadingProgressRepository(goodreadsApi, database, userRepository)

        runBlocking {
            `when`(goodreadsApi.getUserId()).thenReturn(1L)
        }
    }

    @Test
    fun `should remove no longer existing books`() = runBlocking {
        stubBookWithProgress()
        stubReadingProgressStatus(emptyList(), emptyList())
        stubCurrentlyReadBooks(emptyList())
        stubBooksToRead(emptyList())

        readingProgressRepository.synchronizeDatabase()

        assertThat(getStatuses(), empty())
        assertThat(getBooks(), empty())
    }

    @Test
    fun `should store downloaded books with their progresses`() = runBlocking {
        val expectedStatuses = generateReadingStatuses()
        val expectedBooks = generateBooks()
        stubReadingProgressStatus(expectedStatuses, expectedBooks)
        stubCurrentlyReadBooks(emptyList())
        stubBooksToRead(emptyList())

        readingProgressRepository.synchronizeDatabase()

        assertThat(getStatuses(), equalTo(expectedStatuses.map(ReadingProgressStatus::toDbStatus)))
        assertThat(getBooks(), equalTo(expectedBooks.map { it.toDbBook() }))
    }

    @Test
    fun `should store currently read books without progress`() = runBlocking {
        val expectedBooks = generateBooks(isCurrentlyReading = false)
        stubReadingProgressStatus(emptyList(), emptyList())
        stubCurrentlyReadBooks(expectedBooks)
        stubBooksToRead(emptyList())

        readingProgressRepository.synchronizeDatabase()

        assertThat(getStatuses(), empty())
        assertThat(getBooks(), equalTo(expectedBooks.map { it.toDbBook() }))
    }

    @Test
    fun `should store books to read in order`() = runBlocking {
        val expectedBooks = generateBooks(isCurrentlyReading = false)
        stubReadingProgressStatus(emptyList(), emptyList())
        stubCurrentlyReadBooks(emptyList())
        stubBooksToRead(expectedBooks)

        readingProgressRepository.synchronizeDatabase()

        assertThat(getStatuses(), empty())
        val expectedBooksInOrder = expectedBooks.mapIndexed { index, book ->
            book.toDbBook(position = index)
        }
        assertThat(getBooks(), equalTo(expectedBooksInOrder))
    }

    private fun getStatuses() = database.readProgressQueries.selectAll().executeAsList()

    private fun getBooks() = database.bookQueries.selectAll().executeAsList()

    private fun generateBooks(isCurrentlyReading: Boolean = true): List<Book> {
        return (1..10).map {
            Book(
                id = it.toLong(),
                title = "Book #$it",
                numPages = it * 200,
                imageUrl = null,
                authors = null,
                isCurrentlyReading = isCurrentlyReading
            )
        }
    }

    private fun generateReadingStatuses(): List<ReadingProgressStatus> {
        return (1..10).map {
            ReadingProgressStatus(
                id = it.toLong(),
                bookId = it.toLong(),
                page = it * 100,
                reviewId = it.toLong()
            )
        }
    }

    private suspend fun stubReadingProgressStatus(
        statuses: List<ReadingProgressStatus>,
        books: List<Book>
    ) {
        `when`(goodreadsApi.getReadingProgressStatus(1L))
            .thenReturn(ReadingProgressStatusGroup(statuses, books))
    }

    private suspend fun stubCurrentlyReadBooks(books: List<Book>) {
        `when`(goodreadsApi.getBooksInShelf(1L, "currently-reading"))
            .thenReturn(books)
    }

    private suspend fun stubBooksToRead(books: List<Book>) {
        `when`(goodreadsApi.getBooksInShelf(1L, "to-read"))
            .thenReturn(books)
    }

    private fun stubBookWithProgress() {
        database.bookQueries.insert(1L, "Divergent", 100, null, null, null, true)
        database.readProgressQueries.insert(1L, 1L, 30, 1L)
    }
}

fun Book.toDbBook(position: Int? = null): me.thanel.readtracker.database.Book =
    me.thanel.readtracker.database.Book.Impl(
        id = id,
        title = title,
        numPages = numPages,
        imageUrl = imageUrl,
        authors = authors,
        position = position,
        isCurrentlyReading = isCurrentlyReading
    )

fun ReadingProgressStatus.toDbStatus(): ReadProgress = ReadProgress.Impl(
    id = id,
    bookId = bookId,
    page = page,
    reviewId = reviewId
)
