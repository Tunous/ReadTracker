package me.thanel.goodreadsapi

import kotlinx.coroutines.runBlocking
import me.thanel.goodreadsapi.internal.model.ShortDate
import me.thanel.goodreadsapi.internal.util.applyIf
import me.thanel.goodreadsapi.internal.util.urlDecode
import me.thanel.goodreadsapi.model.Book
import me.thanel.goodreadsapi.model.GoodreadsSecrets
import me.thanel.goodreadsapi.model.ReadingProgressStatus
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
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
    fun setup() {
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
    fun `getUserId should return id of authenticated user`() {
        stubResponse("api_auth_user.xml")

        val userId = runBlocking { api.getUserId() }

        assertThat(userId, equalTo(7L))
    }

    @Test
    fun `getBooksInShelf should return all books in currently-reading shelf`() {
        stubResponse("books_currently_reading.xml")

        val (statuses, books) = runBlocking { api.getBooksInShelf(7L, "currently-reading") }

        val expectedBooks = listOf(
            Book(
                id = 6L,
                title = "Harry Potter and the Goblet of Fire (Harry Potter, #4)",
                numPages = 734,
                imageUrl = "https://images.gr-assets.com/books/1554006152m/6.jpg",
                authors = "J.K. Rowling"
            ),
            Book(
                id = 5L,
                title = "Harry Potter and the Prisoner of Azkaban (Harry Potter, #3)",
                numPages = 435,
                imageUrl = "https://images.gr-assets.com/books/1499277281m/5.jpg",
                authors = "J.K. Rowling"
            ),
            Book(
                id = 15881L,
                title = "Harry Potter and the Chamber of Secrets (Harry Potter, #2)",
                numPages = 341,
                imageUrl = "https://images.gr-assets.com/books/1474169725m/15881.jpg",
                authors = "J.K. Rowling"
            )
        )
        assertThat(books, equalTo(expectedBooks))

        val expectedStatuses = listOf(
            ReadingProgressStatus(
                id = null,
                bookId = 6L,
                page = 0,
                reviewId = 2833631637L
            ),
            ReadingProgressStatus(
                id = null,
                bookId = 5L,
                page = 0,
                reviewId = 2833631179L
            ),
            ReadingProgressStatus(
                id = null,
                bookId = 15881L,
                page = 0,
                reviewId = 2833630698L
            )
        )
        assertThat(statuses, equalTo(expectedStatuses))
    }

    @Test
    fun `getBooksInShelf should return all books in to-read shelf`() {
        stubResponse("books_to_read.xml")

        val (statuses, books) = runBlocking { api.getBooksInShelf(7L, "to-read") }

        val expectedBooks = listOf(
            Book(
                id = 2L,
                title = "Harry Potter and the Order of the Phoenix (Harry Potter, #5)",
                numPages = 870,
                imageUrl = "https://images.gr-assets.com/books/1546910265m/2.jpg",
                authors = "J.K. Rowling"
            ),
            Book(
                id = 1L,
                title = "Harry Potter and the Half-Blood Prince (Harry Potter, #6)",
                numPages = 652,
                imageUrl = "https://images.gr-assets.com/books/1361039191m/1.jpg",
                authors = "J.K. Rowling"
            ),
            Book(
                id = 136251L,
                title = "Harry Potter and the Deathly Hallows (Harry Potter, #7)",
                numPages = 759,
                imageUrl = "https://images.gr-assets.com/books/1474171184m/136251.jpg",
                authors = "J.K. Rowling"
            )
        )
        assertThat(books, equalTo(expectedBooks))

        assertThat(statuses.size, equalTo(0))
    }

    @Test
    fun `getReadingProgressStatus should return currently read books with their progress`() {
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
        val b1 = readingProgress.books[1]
        assertThat(b1.id, equalTo(15881L))
        assertThat(b1.title, equalTo("Harry Potter and the Chamber of Secrets (Harry Potter, #2)"))
        assertThat(b1.numPages, equalTo(341))
        assertThat(b1.imageUrl, equalTo("https://images.gr-assets.com/books/1474169725m/15881.jpg"))
        assertThat(b1.authors, equalTo("J.K. Rowling, Mary GrandPré"))
    }

    @Test
    fun `updateProgressByPageNumber should make request with correct parameters`() {
        stubResponse(code = 200)

        runBlocking { api.updateProgressByPageNumber(5L, 128, "Test") }

        val request = server.takeRequest()
        assertThat(request.path, equalTo("/user_status.xml"))
        val parameters = request.extractFormUrlEncodedParameters()
        assertThat(parameters.size, equalTo(3))
        assertThat(parameters[0], equalTo("user_status[book_id]" to "5"))
        assertThat(parameters[1], equalTo("user_status[page]" to "128"))
        assertThat(parameters[2], equalTo("user_status[body]" to "Test"))
    }

    @Test
    fun `updateProgressByPageNumber should not set body parameter when it is blank`() {
        stubResponse()

        runBlocking { api.updateProgressByPageNumber(9L, 234, " ") }

        val request = server.takeRequest()
        val parameters = request.extractFormUrlEncodedParameters()
        assertThat(parameters.size, equalTo(2))
        assertThat(parameters[0], equalTo("user_status[book_id]" to "9"))
        assertThat(parameters[1], equalTo("user_status[page]" to "234"))
    }

    @Test
    fun `startReadingBook should make request with correct parameters`() {
        stubResponse()

        runBlocking { api.startReadingBook(9L) }

        val request = server.takeRequest()
        assertThat(request.path, equalTo("/shelf/add_to_shelf.xml"))
        val parameters = request.extractFormUrlEncodedParameters()
        assertThat(parameters.size, equalTo(2))
        assertThat(parameters[0], equalTo("book_id" to "9"))
        assertThat(parameters[1], equalTo("name" to "currently-reading"))
    }

    @Test
    fun `finishReading should make request with correct parameters`() {
        stubResponse()
        ShortDate.formatter = { "2019-06-09" }

        runBlocking { api.finishReading(1245L, 5, "Cool") }

        val request = server.takeRequest()
        assertThat(request.path, equalTo("/review/1245.xml"))
        val parameters = request.extractFormUrlEncodedParameters()
        assertThat(parameters.size, equalTo(5))
        assertThat(parameters[0], equalTo("review[review]" to "Cool"))
        assertThat(parameters[1], equalTo("review[rating]" to "5"))
        assertThat(parameters[2], equalTo("review[read_at]" to "2019-06-09"))
        assertThat(parameters[3], equalTo("finished" to "true"))
        assertThat(parameters[4], equalTo("shelf" to "read"))
    }

    @Test
    fun `getReviewIdForBook should make request with correct parameters`() {
        stubResponse("review_by_user_and_book.xml")

        val reviewId = runBlocking { api.getReviewIdForBook(1L, 2L) }

        assertThat(reviewId, equalTo(54648L))

        val request = server.takeRequest()
        val uri = request.requestUrl
        assertThat(uri.encodedPath(), equalTo("/review/show_by_user_and_book.xml"))
        assertThat(uri.queryParameter("user_id"), equalTo("1"))
        assertThat(uri.queryParameter("book_id"), equalTo("2"))
    }

    @Test
    fun `getReviewIdForBook should return review id`() {
        stubResponse("review_by_user_and_book.xml")

        val reviewId = runBlocking { api.getReviewIdForBook(1L, 2L) }

        assertThat(reviewId, equalTo(54648L))
    }

    private fun RecordedRequest.extractFormUrlEncodedParameters(): List<Pair<String, String>> {
        val body = body.readString(Charsets.UTF_8)
        val parameters = body.split('&')
        return parameters.map {
            val pair = it.split('=')
            pair[0].urlDecode() to pair[1].urlDecode()
        }
    }

    private fun stubResponse(filename: String? = null, code: Int = 200) {
        server.enqueue(
            MockResponse()
                .setResponseCode(code)
                .applyIf(filename != null) {
                    setBody(getXml("responses/$filename"))
                }
        )
    }

    private fun getXml(path: String): String {
        val uri = javaClass.classLoader!!.getResource(path)
        val file = File(uri.path)
        return file.readText()
    }
}
