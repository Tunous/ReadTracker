package me.thanel.readtracker.database

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReadProgressQueriesTest : DatabaseTest() {

    @Test
    fun `selectAll should return list of stored progresses`() {
        val expectedReadProgresses = SampleData.generateReadProgress()
        bookQueries.insert(SampleData.generateBooks())
        readProgressQueries.insert(expectedReadProgresses)

        val readProgresses = readProgressQueries.selectAll().executeAsList()

        assertThat(readProgresses, equalTo(expectedReadProgresses))
    }

    @Test
    fun `deleteAll should delete all stored progresses`() {
        bookQueries.insert(SampleData.generateBooks())
        readProgressQueries.insert(SampleData.generateReadProgress())

        readProgressQueries.deleteAll()

        assertThat(readProgressQueries.selectAll().executeAsList(), empty())
    }

    @Test
    fun `selectProgressForBook should return correct progress`() {
        bookQueries.insert(SampleData.bookBeingRead)
        val expectedReadProgress = SampleData.progressForBookBeingRead
        readProgressQueries.insert(expectedReadProgress)

        val readProgress = readProgressQueries.selectProgressForBook(SampleData.bookBeingRead.id)
            .executeAsOneOrNull()

        assertThat(readProgress, equalTo(expectedReadProgress))
    }

    @Test
    fun `selectProgressForBook should return no progress if book was not found`() {
        bookQueries.insert(SampleData.bookBeingRead)
        readProgressQueries.insert(SampleData.progressForBookBeingRead)

        val readProgress = readProgressQueries.selectProgressForBook(SampleData.bookToRead.id)
            .executeAsOneOrNull()

        assertThat(readProgress, nullValue())
    }

    @Test(expected = SQLiteConstraintException::class)
    fun `insertion should be not possible if no book with matching id exists`() {
        bookQueries.insert(SampleData.bookToRead)
        readProgressQueries.insert(SampleData.progressForBookBeingRead)
    }

    @Test
    fun `insertion should work if book exists`() {
        val expectedReadProgress = SampleData.progressForBookBeingRead
        bookQueries.insert(SampleData.bookBeingRead)
        readProgressQueries.insert(expectedReadProgress)

        assertThat(readProgressQueries.selectAll().executeAsOne(), equalTo(expectedReadProgress))
    }

    @Test
    fun `insertion should replace existing progress on id conflict`() {
        val originalReadProgress = SampleData.progressForBookBeingRead
        bookQueries.insert(SampleData.bookBeingRead)
        bookQueries.insert(SampleData.bookToRead)
        readProgressQueries.insert(originalReadProgress)

        val modifiedReadProgress = (originalReadProgress as ReadProgress.Impl)
            .copy(bookId = SampleData.bookToRead.id) as ReadProgress
        readProgressQueries.insert(modifiedReadProgress)

        assertThat(readProgressQueries.selectAll().executeAsOne(), equalTo(modifiedReadProgress))
    }

    @Test
    fun `insertion should replace existing progress on book id conflict`() {
        val originalReadProgress = SampleData.progressForBookBeingRead
        bookQueries.insert(SampleData.bookBeingRead)
        bookQueries.insert(SampleData.bookToRead)
        readProgressQueries.insert(originalReadProgress)

        val modifiedReadProgress = (originalReadProgress as ReadProgress.Impl)
            .copy(id = 7L) as ReadProgress
        readProgressQueries.insert(modifiedReadProgress)

        assertThat(readProgressQueries.selectAll().executeAsOne(), equalTo(modifiedReadProgress))
    }

    @Test
    fun `updateProgress should update progress for correct book`() {
        val originalReadProgress = SampleData.progressForBookBeingRead
        bookQueries.insert(SampleData.bookBeingRead)
        readProgressQueries.insert(originalReadProgress)

        val expectedReadProgress = (originalReadProgress as ReadProgress.Impl)
            .copy(page = originalReadProgress.page * 2) as ReadProgress
        readProgressQueries.updateProgress(expectedReadProgress.page, expectedReadProgress.bookId)

        val readProgress = readProgressQueries.selectAll().executeAsOne()
        assertThat(readProgress, equalTo(expectedReadProgress))
    }

    @Test
    fun `updateProgress should not modify unrelatedProgress`() {
        bookQueries.insert(SampleData.bookBeingRead)
        bookQueries.insert(SampleData.bookToRead)
        readProgressQueries.insert(SampleData.progressForBookBeingRead)
        readProgressQueries.insert(SampleData.progressForBookToRead)

        readProgressQueries.updateProgress(75, SampleData.bookBeingRead.id)

        val readProgress = readProgressQueries.selectProgressForBook(SampleData.bookToRead.id).executeAsOne()
        assertThat(readProgress, equalTo(SampleData.progressForBookToRead))
    }

    @Test
    fun `progress should be deleted when related book is deleted`() {
        bookQueries.insert(SampleData.bookBeingRead)
        readProgressQueries.insert(SampleData.progressForBookBeingRead)

        assertThat(readProgressQueries.selectAll().executeAsOneOrNull(), notNullValue())

        bookQueries.deleteBookWithId(SampleData.bookBeingRead.id)

        assertThat(readProgressQueries.selectAll().executeAsOneOrNull(), nullValue())
    }

    @Test
    fun `progress should not be deleted when unrelated book is deleted`() {
        bookQueries.insert(SampleData.bookBeingRead)
        bookQueries.insert(SampleData.bookToRead)
        readProgressQueries.insert(SampleData.progressForBookBeingRead)

        bookQueries.deleteBookWithId(SampleData.bookToRead.id)

        assertThat(readProgressQueries.selectAll().executeAsOne(), equalTo(SampleData.progressForBookBeingRead))
    }

    // TODO:
    //  deleteBookForReview
    //  selectWithBookInformation
}
