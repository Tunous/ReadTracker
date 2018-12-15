package me.thanel.goodreadsapi

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import kotlinx.coroutines.runBlocking
import me.thanel.goodreadsapi.internal.GoodreadsService
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import java.io.File

class GoodreadsServiceTest {
    private lateinit var service: GoodreadsService
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        service = Retrofit.Builder()
            .baseUrl(server.url(""))
            .addConverterFactory(TikXmlConverterFactory.create(TikXml.Builder()
                .exceptionOnUnreadXml(false)
                .build()))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(GoodreadsService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun getUserId() {
        server.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(getXml("xml/userId.xml")))

        val response = runBlocking { service.getUserId().await() }

        assertEquals("/api/auth_user", server.takeRequest().path)
        assertEquals(1234, response.user.id)
        assertEquals("User", response.user.name)
        Assert.assertNull(response.user.userStatuses)
    }

    @Test
    fun getUser() {
        server.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(getXml("xml/user_1234.xml")))

        val response = runBlocking { service.getUser(1234).await() }

        assertEquals("/user/show/1234.xml", server.takeRequest().path)
        assertEquals(1234, response.user.id)
        assertEquals("User", response.user.name)
        assertNotNull(response.user.userStatuses)
        assertEquals(1, response.user.userStatuses!!.size)

        val userStatus = response.user.userStatuses!![0]
        assertEquals(56, userStatus.id)
        assertEquals(305, userStatus.page)
        assertEquals(35, userStatus.percent)

        val book = userStatus.book
        assertEquals(12, book.id)
        assertEquals("Book", book.title)
        assertEquals(856, book.numPages)
        assertEquals("https://example.com/book.jpg", book.imageUrl)
        assertEquals(1, book.authors.size)

        val author = book.authors[0]
        assertEquals(99, author.id)
        assertEquals("Author", author.name)
    }

    private fun getXml(path: String): String {
        val uri = javaClass.classLoader!!.getResource(path)
        val file = File(uri.path)
        return file.readText()
    }
}
