package me.thanel.readtracker.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasSize
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookQueriesTest : DatabaseTest() {

    @Test
    fun `insertion should set all properties`() {
        bookQueries.insert(SampleData.bookToRead)

        assertThat(bookQueries.selectAll().executeAsOne(), equalTo(SampleData.bookToRead))
    }

    @Test
    fun `insertion should replace old data on id conflict`() {
        val originalBook = SampleData.bookToRead
        bookQueries.insert(originalBook)

        val modifiedBook = (originalBook as Book.Impl)
            .copy(
                title = "Modified title",
                numPages = 34,
                imageUrl = "http://example.com/image.png",
                authors = "Unknown",
                position = 12,
                isCurrentlyReading = true
            ) as Book
        bookQueries.insert(modifiedBook)

        assertThat(bookQueries.selectAll().executeAsOne(), equalTo(modifiedBook))
    }

    @Test
    fun `insertion should replace old data on position conflict`() {
        val originalBook = SampleData.bookToRead
        bookQueries.insert(originalBook)

        val modifiedBook = (originalBook as Book.Impl)
            .copy(
                id = 789L,
                title = "Modified title",
                numPages = 34,
                imageUrl = "http://example.com/image.png",
                authors = "Unknown",
                isCurrentlyReading = true
            ) as Book
        bookQueries.insert(modifiedBook)

        assertThat(bookQueries.selectAll().executeAsOne(), equalTo(modifiedBook))
    }

    @Test
    fun `selectAll should return all books`() {
        val books = SampleData.generateBooks()
        bookQueries.insert(books)

        assertThat(bookQueries.selectAll().executeAsList(), equalTo(books))
    }

    @Test
    fun `selectBooksToRead should return book that aren't currently being read`() {
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
    fun `selectBooksToRead should return books ordered by position`() {
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
    fun `deleteAll should delete all books`() {
        bookQueries.insert(SampleData.generateBooks())

        bookQueries.deleteAll()

        assertThat(bookQueries.selectAll().executeAsList(), empty())
    }

    @Test
    fun `deleteBooksWithoutPosition deletes book without position`() {
        val booksWithPosition = SampleData.generateBooks(count = 5)
        val booksWithoutPosition = SampleData.generateBooks(startNumber = 6, count = 5)
            .map { (it as Book.Impl).copy(position = null) }
        bookQueries.insert(booksWithPosition)
        bookQueries.insert(booksWithoutPosition)

        bookQueries.deleteBooksWithoutPosition()

        assertThat(bookQueries.selectAll().executeAsList(), equalTo(booksWithPosition))
    }

    // TODO: bookQueries.deleteBooksWithProgress()
}
