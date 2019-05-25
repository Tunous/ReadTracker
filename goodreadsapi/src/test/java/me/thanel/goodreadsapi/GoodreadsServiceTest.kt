package me.thanel.goodreadsapi

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File

class GoodreadsServiceTest {
    private lateinit var server: MockWebServer
    private lateinit var api: GoodreadsApi

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val secrets = GoodreadsSecrets("", "", "", "")
        api = GoodreadsApi.create(secrets, server.url("").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `should return id of authenticated user`() {
        stubResponse("api_auth_user.xml")

        val userId = runBlocking { api.getUserId() }

        assertThat(userId, equalTo(7L))
    }

    @Test
    fun `should return all books in currently-reading shelf`() {
        stubResponse("books_currently_reading.xml")

        val books = runBlocking { api.getBooksInShelf(7L, "currently-reading") }

        assertThat(books.size, equalTo(3))
        val b0 = books[0]
        assertThat(b0.id, equalTo(6L))
        assertThat(b0.title, equalTo("Harry Potter and the Goblet of Fire (Harry Potter, #4)"))
        assertThat(b0.numPages, equalTo(734))
        assertThat(b0.imageUrl, equalTo("https://images.gr-assets.com/books/1554006152m/6.jpg"))
        assertThat(b0.authors, equalTo("J.K. Rowling"))
        assertThat(b0.isCurrentlyReading, equalTo(true))
        val b1 = books[1]
        assertThat(b1.id, equalTo(5L))
        assertThat(b1.title, equalTo("Harry Potter and the Prisoner of Azkaban (Harry Potter, #3)"))
        assertThat(b1.numPages, equalTo(435))
        assertThat(b1.imageUrl, equalTo("https://images.gr-assets.com/books/1499277281m/5.jpg"))
        assertThat(b1.authors, equalTo("J.K. Rowling"))
        assertThat(b1.isCurrentlyReading, equalTo(true))
        val b2 = books[2]
        assertThat(b2.id, equalTo(15881L))
        assertThat(b2.title, equalTo("Harry Potter and the Chamber of Secrets (Harry Potter, #2)"))
        assertThat(b2.numPages, equalTo(341))
        assertThat(b2.imageUrl, equalTo("https://images.gr-assets.com/books/1474169725m/15881.jpg"))
        assertThat(b2.authors, equalTo("J.K. Rowling"))
        assertThat(b2.isCurrentlyReading, equalTo(true))
    }

    @Test
    fun `should return all books in to-read shelf`() {
        stubResponse("books_to_read.xml")

        val books = runBlocking { api.getBooksInShelf(7L, "to-read") }

        assertThat(books.size, equalTo(3))
        val b0 = books[0]
        assertThat(b0.id, equalTo(2L))
        assertThat(b0.title, equalTo("Harry Potter and the Order of the Phoenix (Harry Potter, #5)"))
        assertThat(b0.numPages, equalTo(870))
        assertThat(b0.imageUrl, equalTo("https://images.gr-assets.com/books/1546910265m/2.jpg"))
        assertThat(b0.authors, equalTo("J.K. Rowling"))
        assertThat(b0.isCurrentlyReading, equalTo(false))
        val b1 = books[1]
        assertThat(b1.id, equalTo(1L))
        assertThat(b1.title, equalTo("Harry Potter and the Half-Blood Prince (Harry Potter, #6)"))
        assertThat(b1.numPages, equalTo(652))
        assertThat(b1.imageUrl, equalTo("https://images.gr-assets.com/books/1361039191m/1.jpg"))
        assertThat(b1.authors, equalTo("J.K. Rowling"))
        assertThat(b1.isCurrentlyReading, equalTo(false))
        val b2 = books[2]
        assertThat(b2.id, equalTo(136251L))
        assertThat(b2.title, equalTo("Harry Potter and the Deathly Hallows (Harry Potter, #7)"))
        assertThat(b2.numPages, equalTo(759))
        assertThat(b2.imageUrl, equalTo("https://images.gr-assets.com/books/1474171184m/136251.jpg"))
        assertThat(b2.authors, equalTo("J.K. Rowling"))
        assertThat(b2.isCurrentlyReading, equalTo(false))
    }

    @Test
    fun `should return currently read books with their progress`() {
        stubResponse("user_show.xml")

        val readingProgress = runBlocking { api.getReadingProgressStatus(7L) }

        assertThat(readingProgress.statuses.size, equalTo(2))
        val s0 = readingProgress.statuses[0]
        assertThat(s0.id, equalTo(1234L))
        assertThat(s0.page, equalTo(78))
        assertThat(s0.bookId, equalTo(5L))
        assertThat(s0.reviewId, equalTo(5678L))
        val s1 = readingProgress.statuses[1]
        assertThat(s1.id, equalTo(1235L))
        assertThat(s1.page, equalTo(307))
        assertThat(s1.bookId, equalTo(15881L))
        assertThat(s1.reviewId, equalTo(5679L))

        assertThat(readingProgress.books.size, equalTo(2))
        val b0 = readingProgress.books[0]
        assertThat(b0.id, equalTo(5L))
        assertThat(b0.title, equalTo("Harry Potter and the Prisoner of Azkaban (Harry Potter, #3)"))
        assertThat(b0.numPages, equalTo(435))
        assertThat(b0.imageUrl, equalTo("https://images.gr-assets.com/books/1499277281m/5.jpg"))
        assertThat(b0.authors, equalTo("J.K. Rowling, Mary GrandPré"))
        assertThat(b0.isCurrentlyReading, equalTo(true))
        val b1 = readingProgress.books[1]
        assertThat(b1.id, equalTo(15881L))
        assertThat(b1.title, equalTo("Harry Potter and the Chamber of Secrets (Harry Potter, #2)"))
        assertThat(b1.numPages, equalTo(341))
        assertThat(b1.imageUrl, equalTo("https://images.gr-assets.com/books/1474169725m/15881.jpg"))
        assertThat(b1.authors, equalTo("J.K. Rowling, Mary GrandPré"))
        assertThat(b1.isCurrentlyReading, equalTo(true))
    }

    private fun stubResponse(filename: String, code: Int = 200) {
        server.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setBody(getXml("responses/$filename"))
        )
    }

    private fun getXml(path: String): String {
        val uri = javaClass.classLoader!!.getResource(path)
        val file = File(uri.path)
        return file.readText()
    }
}
