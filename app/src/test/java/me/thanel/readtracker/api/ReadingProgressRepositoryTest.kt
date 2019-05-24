package me.thanel.readtracker.api

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chibatching.kotpref.Kotpref
import com.squareup.sqldelight.android.AndroidSqliteDriver
import kotlinx.coroutines.runBlocking
import me.thanel.goodreadsapi.GoodreadsApiInterface
import me.thanel.goodreadsapi.model.ReadingProgressStatusGroup
import me.thanel.readtracker.Database
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ReadingProgressRepositoryTest {
    private lateinit var database: Database

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        Kotpref.init(context)

        val driver = AndroidSqliteDriver(Database.Schema, context)
        database = Database(driver)

        database.bookQueries.insert(1L, "Divergent", 100, null, null, null)
        database.readProgressQueries.insert(1L, 1L, 30, 90, 1L)
    }

    @Test
    fun updating_progress_by_page_number_should_update_local_database() = runBlocking {
        val goodreadsApi = mock(GoodreadsApiInterface::class.java)
        val userRepository = UserRepository(goodreadsApi)
        val readingProgressRepository =
            ReadingProgressRepository(goodreadsApi, database, userRepository)

        readingProgressRepository.updateProgressByPageNumber(1L, 200)

        val progress = database.readProgressQueries.selectAll().executeAsOne()
        assertThat(progress.bookId, equalTo(1L))
        assertThat(progress.page, equalTo(200))
    }

    @Test
    fun updating_progress_by_page_number_should_make_api_call() = runBlocking {
        val goodreadsApi = mock(GoodreadsApiInterface::class.java)
        val userRepository = UserRepository(goodreadsApi)
        val readingProgressRepository =
            ReadingProgressRepository(goodreadsApi, database, userRepository)

        readingProgressRepository.updateProgressByPageNumber(1L, 200)

        verify(goodreadsApi).updateProgressByPageNumber(1L, 200, null)
    }

    @Test
    fun synchronize_database_should_remove_no_longer_existing_books() = runBlocking {
        val goodreadsApi = mock(GoodreadsApiInterface::class.java)
        `when`(goodreadsApi.getUserId()).thenReturn(1L)
        `when`(goodreadsApi.getReadingProgressStatus(1L))
            .thenReturn(ReadingProgressStatusGroup(emptyList(), emptyList()))
        `when`(goodreadsApi.getBooksInShelf(1L, "currently-reading"))
            .thenReturn(emptyList())
        `when`(goodreadsApi.getBooksInShelf(1L, "to-read"))
            .thenReturn(emptyList())
        val userRepository = UserRepository(goodreadsApi)
        val repository = ReadingProgressRepository(goodreadsApi, database, userRepository)

        repository.synchronizeDatabase()

        val data = database.readProgressQueries.selectAll().executeAsList()
        assertThat(data, hasSize(0))
    }

    @Test
    fun finishing_reading_should_make_api_call() = runBlocking {
        val goodreadsApi = mock(GoodreadsApiInterface::class.java)
        val userRepository = UserRepository(goodreadsApi)
        val readingProgressRepository = ReadingProgressRepository(goodreadsApi, database, userRepository)

        readingProgressRepository.finishReading(1L, 3, "Nice book!")

        verify(goodreadsApi).finishReading(1L, 3, "Nice book!")
    }

    @Test
    fun finishing_reading_should_remove_book_from_local_database() = runBlocking {
        val goodreadsApi = mock(GoodreadsApiInterface::class.java)
        val userRepository = UserRepository(goodreadsApi)
        val readingProgressRepository = ReadingProgressRepository(goodreadsApi, database, userRepository)

        readingProgressRepository.finishReading(1L, 3, "Nice book!")

        val data = database.readProgressQueries.selectAll().executeAsList()
        assertThat(data, hasSize(0))
    }
}
