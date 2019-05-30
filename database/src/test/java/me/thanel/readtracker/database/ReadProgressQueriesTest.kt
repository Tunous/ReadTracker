package me.thanel.readtracker.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReadProgressQueriesTest : DatabaseTest() {

    private val readProgressQueries get() = database.readProgressQueries

    @Test
    fun `selectAll returns all progresses`() {
        readProgressQueries.insert(
            id = 5,
            bookId = 7,
            page = 3,
            reviewId = 247
        )
        readProgressQueries.insert(
            id = 765,
            bookId = 23,
            page = 5,
            reviewId = 7
        )
        readProgressQueries.insert(
            id = 12,
            bookId = 75,
            page = 85,
            reviewId = 91
        )

        val progress = readProgressQueries.selectAll().executeAsList()

        val ids = progress.map { it.id }
        assertThat(progress, hasSize(3))
        assertThat(ids, equalTo(listOf(5L, 12L, 765L)))
    }

    @Test
    fun `deleteAll deletes everything`() {
        insertMultipleProgress(10)

        readProgressQueries.deleteAll()

        val progress = readProgressQueries.selectAll().executeAsList()
        assertThat(progress, empty())
    }

    @Test
    fun `hasProgress returns correct value based on book id`() {
        readProgressQueries.insert(
            id = 1,
            bookId = 2,
            page = 30,
            reviewId = 278
        )

        val existingProgress = readProgressQueries.selectProgressForBook(2L).executeAsOneOrNull()
        val nonExistingProgress = readProgressQueries.selectProgressForBook(7L).executeAsOneOrNull()

        assertThat(existingProgress, notNullValue())
        assertThat(existingProgress?.id, equalTo(1L))
        assertThat(nonExistingProgress, nullValue())
    }

    @Test
    fun `insertion sets all properties`() {
        readProgressQueries.insert(
            id = 8,
            bookId = 6,
            page = 96,
            reviewId = 1
        )

        val progress = readProgressQueries.selectAll().executeAsOne()

        assertThat(progress.id, equalTo(8L))
        assertThat(progress.bookId, equalTo(6L))
        assertThat(progress.page, equalTo(96))
        assertThat(progress.reviewId, equalTo(1L))
    }

    @Test
    fun `insertion replaces existing progress on id conflict`() {
        readProgressQueries.insert(
            id = 1,
            bookId = 2,
            page = 200,
            reviewId = 12
        )

        readProgressQueries.insert(
            id = 1,
            bookId = 3,
            page = 200,
            reviewId = 18
        )

        val progress = readProgressQueries.selectAll().executeAsOne()
        assertThat(progress.id, equalTo(1L))
        assertThat(progress.bookId, equalTo(3L))
        assertThat(progress.page, equalTo(200))
        assertThat(progress.reviewId, equalTo(18L))
    }

    @Test
    fun `insertion replaces existing progress on book id conflict`() {
        readProgressQueries.insert(
            id = 1,
            bookId = 2,
            page = 200,
            reviewId = 12
        )

        readProgressQueries.insert(
            id = 3,
            bookId = 2,
            page = 700,
            reviewId = 7
        )

        val progress = readProgressQueries.selectAll().executeAsOne()
        assertThat(progress.id, equalTo(3L))
        assertThat(progress.bookId, equalTo(2L))
        assertThat(progress.page, equalTo(700))
        assertThat(progress.reviewId, equalTo(7L))
    }

    @Test
    fun `updateProgress updates progress for correct book`() {
        readProgressQueries.insert(
            id = 3,
            bookId = 12,
            page = 100,
            reviewId = 5
        )
        readProgressQueries.insert(
            id = 12,
            bookId = 3,
            page = 120,
            reviewId = 7
        )

        readProgressQueries.updateProgress(200, 12)

        val progress = readProgressQueries.selectAll().executeAsList()
        val updatedProgress = progress[0]
        assertThat(updatedProgress.id, equalTo(3L))
        assertThat(updatedProgress.bookId, equalTo(12L))
        assertThat(updatedProgress.page, equalTo(200))
        val otherProgress = progress[1]
        assertThat(otherProgress.id, equalTo(12L))
        assertThat(otherProgress.bookId, equalTo(3L))
        assertThat(otherProgress.page, equalTo(120))
    }

    // TODO:
    //  deleteBookForReview
    //  deleteProgressWithoutBook
    //  selectWithBookInformation

    private fun insertMultipleProgress(count: Int) {
        for (id in 0 until count) {
            readProgressQueries.insert(
                id = id.toLong(),
                bookId = id * 5L,
                page = id * 10,
                reviewId = id * 20L
            )
        }
    }
}
