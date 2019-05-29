package me.thanel.readtracker.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasSize
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookQueriesTest : DatabaseTest() {

    private val bookQueries get() = database.bookQueries

    @Test
    fun `insertion sets all properties`() {
        insertTestBook()

        val book = bookQueries.selectAll().executeAsOne()
        assertThat(book.id, equalTo(6L))
        assertThat(book.title, equalTo("Harry Potter and the Goblet of Fire (Harry Potter, #4)"))
        assertThat(book.numPages, equalTo(734))
        assertThat(book.imageUrl, equalTo("https://images.gr-assets.com/books/1554006152m/6.jpg"))
        assertThat(book.authors, equalTo("J.K. Rowling"))
        assertThat(book.position, equalTo(7))
        assertThat(book.isCurrentlyReading, equalTo(true))
    }

    @Test
    fun `insertion replaces old data on id conflict`() {
        insertTestBook()

        bookQueries.insert(
            id = 6L,
            title = "Harry Potter and the Goblet of Fire",
            numPages = 734,
            imageUrl = null,
            authors = "J.K. Rowling",
            position = 9,
            isCurrentlyReading = false
        )

        val book = bookQueries.selectAll().executeAsOne()
        assertThat(book.id, equalTo(6L))
        assertThat(book.title, equalTo("Harry Potter and the Goblet of Fire"))
        assertThat(book.numPages, equalTo(734))
        assertThat(book.imageUrl, nullValue())
        assertThat(book.authors, equalTo("J.K. Rowling"))
        assertThat(book.position, equalTo(9))
        assertThat(book.isCurrentlyReading, equalTo(false))
    }

    @Test
    fun `insertion replaces old data on position conflict`() {
        insertTestBook()

        bookQueries.insert(
            id = 12L,
            title = "Harry Potter, #4",
            numPages = 734,
            imageUrl = null,
            authors = "Rowling",
            position = 7,
            isCurrentlyReading = true
        )

        val book = bookQueries.selectAll().executeAsOne()
        assertThat(book.id, equalTo(12L))
        assertThat(book.title, equalTo("Harry Potter, #4"))
        assertThat(book.numPages, equalTo(734))
        assertThat(book.imageUrl, nullValue())
        assertThat(book.authors, equalTo("Rowling"))
        assertThat(book.position, equalTo(7))
        assertThat(book.isCurrentlyReading, equalTo(true))
    }

    @Test
    fun `selectAll returns all books`() {
        val count = 5
        insertMultipleBooks(count)

        val books = bookQueries.selectAll().executeAsList()

        assertThat(books, hasSize(count))
        for (id in 0 until count) {
            assertThat(books[id].id, equalTo(id.toLong()))
            assertThat(books[id].title, equalTo("Book #$id"))
        }
    }

    @Test
    fun `selectBooksToRead returns book that aren't currently being read`() {
        bookQueries.insert(
            id = 1L,
            title = "Not reading",
            numPages = 1355,
            imageUrl = null,
            authors = null,
            position = null,
            isCurrentlyReading = false
        )
        bookQueries.insert(
            id = 2L,
            title = "Reading",
            numPages = 57,
            imageUrl = null,
            authors = null,
            position = null,
            isCurrentlyReading = true
        )
        bookQueries.insert(
            id = 3L,
            title = "Reading nr 2",
            numPages = 8312,
            imageUrl = null,
            authors = null,
            position = null,
            isCurrentlyReading = false
        )

        val books = bookQueries.selectBooksToRead().executeAsList()

        assertThat(books, hasSize(2))
        assertThat(books[0].id, equalTo(1L))
        assertThat(books[1].id, equalTo(3L))
    }

    @Test
    fun `selectBooksToRead returns books ordered by position`() {
        bookQueries.insert(
            id = 1L,
            title = "Second",
            numPages = 1355,
            imageUrl = null,
            authors = null,
            position = 2,
            isCurrentlyReading = false
        )
        bookQueries.insert(
            id = 2L,
            title = "First",
            numPages = 57,
            imageUrl = null,
            authors = null,
            position = 1,
            isCurrentlyReading = false
        )
        bookQueries.insert(
            id = 3L,
            title = "Third",
            numPages = 8312,
            imageUrl = null,
            authors = null,
            position = 3,
            isCurrentlyReading = false
        )

        val books = bookQueries.selectBooksToRead().executeAsList()

        val bookIds = books.map { it.id }
        assertThat(bookIds, equalTo(listOf(2L, 1L, 3L)))
    }

    @Test
    fun `deleteAll deletes all books`() {
        insertMultipleBooks(10)

        bookQueries.deleteAll()

        val books = bookQueries.selectAll().executeAsList()
        assertThat(books, empty())
    }

    @Test
    fun `deleteBooksWithoutPosition deletes book without position`() {
        bookQueries.insert(
            id = 1,
            title = "Book #1",
            numPages = 10,
            imageUrl = null,
            authors = null,
            position = 4,
            isCurrentlyReading = false
        )
        bookQueries.insert(
            id = 2,
            title = "Book #2",
            numPages = 10,
            imageUrl = null,
            authors = null,
            position = null,
            isCurrentlyReading = false
        )
        bookQueries.insert(
            id = 3,
            title = "Book #3",
            numPages = 10,
            imageUrl = null,
            authors = null,
            position = null,
            isCurrentlyReading = false
        )

        bookQueries.deleteBooksWithoutPosition()

        val books = bookQueries.selectAll().executeAsList()
        val bookIds = books.map { it.id }
        assertThat(bookIds, equalTo(listOf(1L)))
    }

    // TODO: bookQueries.deleteBooksWithProgress()

    private fun insertMultipleBooks(count: Int) {
        for (id in 0 until count) {
            bookQueries.insert(
                id = id.toLong(),
                title = "Book #$id",
                numPages = id * 10,
                imageUrl = null,
                authors = null,
                position = null,
                isCurrentlyReading = false
            )
        }
    }

    private fun insertTestBook() {
        bookQueries.insert(
            id = 6L,
            title = "Harry Potter and the Goblet of Fire (Harry Potter, #4)",
            numPages = 734,
            imageUrl = "https://images.gr-assets.com/books/1554006152m/6.jpg",
            authors = "J.K. Rowling",
            position = 7,
            isCurrentlyReading = true
        )
    }
}
