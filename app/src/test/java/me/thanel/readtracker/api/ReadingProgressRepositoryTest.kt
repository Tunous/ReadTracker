package me.thanel.readtracker.api

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import me.thanel.goodreadsapi.model.ReadingProgressStatusGroup
import me.thanel.readtracker.testbase.BaseRepositoryTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class ReadingProgressRepositoryTest : BaseRepositoryTest() {

    private lateinit var readingProgressRepository: ReadingProgressRepository

    @Before
    fun setup() {
        val userRepository = UserRepository(goodreadsApi)
        readingProgressRepository =
            ReadingProgressRepository(goodreadsApi, database, userRepository)
    }

    @Test
    fun `updating progress by page number should update local database`() = runBlocking {
        stubBookWithProgress()

        readingProgressRepository.updateProgressByPageNumber(1L, 200)

        val progress = database.readProgressQueries.selectAll().executeAsOne()
        assertThat(progress.bookId, equalTo(1L))
        assertThat(progress.page, equalTo(200))
    }

    @Test
    fun `updating progress by page number should make api call`() = runBlocking {
        stubBookWithProgress()

        readingProgressRepository.updateProgressByPageNumber(1L, 200)

        verify(goodreadsApi).updateProgressByPageNumber(1L, 200, null)
    }

    @Test
    fun `synchronize database should remove no longer existing books`() = runBlocking {
        stubBookWithProgress()
        `when`(goodreadsApi.getUserId()).thenReturn(1L)
        `when`(goodreadsApi.getReadingProgressStatus(1L))
            .thenReturn(ReadingProgressStatusGroup(emptyList(), emptyList()))
        `when`(goodreadsApi.getBooksInShelf(1L, "currently-reading"))
            .thenReturn(emptyList())
        `when`(goodreadsApi.getBooksInShelf(1L, "to-read"))
            .thenReturn(emptyList())

        readingProgressRepository.synchronizeDatabase()

        val data = database.readProgressQueries.selectAll().executeAsList()
        assertThat(data, hasSize(0))
    }

    @Test
    fun `finishing reading should make api call`() = runBlocking {
        stubBookWithProgress()

        readingProgressRepository.finishReading(1L, 2L, 3, "Nice book!")

        verify(goodreadsApi).finishReading(2L, 3, "Nice book!")
    }

    @Test
    fun `finishing reading should remove book from local database`() = runBlocking {
        stubBookWithProgress()

        readingProgressRepository.finishReading(1L, 2L, 3, "Nice book!")

        val books = database.bookQueries.selectAll().executeAsList()
        assertThat(books, hasSize(0))
    }

    @Test
    fun `finishing reading should fetch review id if it was not provided`() = runBlocking {
        stubBookWithProgress()
        `when`(goodreadsApi.getUserId()).thenReturn(1L)
        `when`(goodreadsApi.getReviewIdForBook(1L, 1L)).thenReturn(12L)

        readingProgressRepository.finishReading(1L, null, 3, "Nice book!")

        verify(goodreadsApi).finishReading(12L, 3, "Nice book!")
    }

    // TODO: Test situation where getReviewIdForBook returns null

    @Test
    fun `initial progress update on book moves it to currently reading shelf`() = runBlocking {
        database.bookQueries.insert(1L, "Divergent", 100, null, null, null, false)

        readingProgressRepository.updateProgressByPageNumber(1L, 10)

        verify(goodreadsApi).startReadingBook(1L)
    }

    @Test
    fun `progress update does not move book to currently reading shelf if it had previous progress`() =
        runBlocking {
            stubBookWithProgress()

            readingProgressRepository.updateProgressByPageNumber(1L, 10)

            verify(goodreadsApi, never()).startReadingBook(1L)
        }

    private fun stubBookWithProgress() {
        database.bookQueries.insert(1L, "Divergent", 100, null, null, null, true)
        database.readProgressQueries.insert(1L, 1L, 30, 1L)
    }
}
