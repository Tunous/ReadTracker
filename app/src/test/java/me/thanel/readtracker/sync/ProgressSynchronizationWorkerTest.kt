package me.thanel.readtracker.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker.Result
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import me.thanel.readtracker.di.ReadTracker
import me.thanel.readtracker.testbase.MockDependencyInjector
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ProgressSynchronizationWorkerTest {

    private lateinit var context: Context
    private lateinit var mockInjector: MockDependencyInjector

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockInjector = MockDependencyInjector()
        ReadTracker.dependencyInjector = mockInjector
    }

    private fun runWithWorker(block: suspend CoroutineScope.(ProgressSynchronizationWorker) -> Unit) =
        runBlocking {
            block(TestListenableWorkerBuilder<ProgressSynchronizationWorker>(context).build())
        }

    @Test
    fun `worker should invoke database synchronization process`() = runWithWorker {
        it.doWork()

        verify(mockInjector.readingProgressRepositoryMock).synchronizeDatabase()
    }

    @Test
    fun `synchronization success should finish worker with success`() = runWithWorker {
        val result = it.doWork()

        assertThat(result, equalTo(Result.success()))
    }

    @Test
    fun `error while synchronizing should finish worker with retry`() = runWithWorker {
        `when`(mockInjector.readingProgressRepositoryMock.synchronizeDatabase())
            .thenAnswer {
                throw IOException()
            }

        val result = it.doWork()

        assertThat(result, equalTo(Result.retry()))
    }
}
