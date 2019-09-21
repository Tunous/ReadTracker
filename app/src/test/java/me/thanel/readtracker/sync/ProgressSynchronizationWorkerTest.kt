package me.thanel.readtracker.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker.Result
import me.thanel.readtracker.testbase.BaseWorkerTest
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ProgressSynchronizationWorkerTest : BaseWorkerTest() {

    @Test
    fun `worker should invoke database synchronization process`() = runWithWorker<ProgressSynchronizationWorker> {
        it.doWork()

        verify(mockInjector.readingProgressRepositoryMock).synchronizeDatabase()
    }

    @Test
    fun `synchronization success should finish worker with success`() = runWithWorker<ProgressSynchronizationWorker> {
        val result = it.doWork()

        assertThat(result, equalTo(Result.success()))
    }

    @Test
    fun `error while synchronizing should finish worker with retry`() = runWithWorker<ProgressSynchronizationWorker> {
        `when`(mockInjector.readingProgressRepositoryMock.synchronizeDatabase())
            .thenAnswer {
                throw IOException()
            }

        val result = it.doWork()

        assertThat(result, equalTo(Result.retry()))
    }
}
