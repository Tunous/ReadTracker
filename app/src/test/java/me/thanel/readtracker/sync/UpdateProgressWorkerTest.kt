package me.thanel.readtracker.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker.Result
import androidx.work.workDataOf
import me.thanel.readtracker.testbase.BaseWorkerTest
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.nullable
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class UpdateProgressWorkerTest : BaseWorkerTest() {

    @Test
    fun `worker should update book progress with correct parameters`() {
        val bookId = 5L
        val pageNumber = 1243
        val reviewBody = "5 stars"
        val inputData = workDataOf(
            UpdateProgressWorker.INPUT_BOOK_ID to bookId,
            UpdateProgressWorker.INPUT_PAGE_NUMBER to pageNumber,
            UpdateProgressWorker.INPUT_REVIEW_BODY to reviewBody
        )
        runWithWorker<UpdateProgressWorker>(inputData = inputData) {
            it.doWork()

            verify(mockInjector.readingProgressRepositoryMock).updateProgressByPageNumber(
                bookId,
                pageNumber,
                reviewBody
            )
        }
    }

    @Test
    fun `operation success should finish worker with success`() =
        runWithWorker<UpdateProgressWorker> {
            val result = it.doWork()

            assertThat(result, equalTo(Result.success()))
        }

    @Test
    fun `error while updating progress should finish worker with retry`() =
        runWithWorker<UpdateProgressWorker> {
            `when`(
                mockInjector.readingProgressRepositoryMock.updateProgressByPageNumber(
                    anyLong(),
                    anyInt(),
                    nullable(String::class.java)
                )
            ).thenAnswer {
                throw IOException()
            }

            val result = it.doWork()

            assertThat(result, equalTo(Result.retry()))
        }
}
