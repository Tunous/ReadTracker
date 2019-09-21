package me.thanel.readtracker.testbase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import me.thanel.readtracker.di.ReadTracker
import org.junit.Before

abstract class BaseWorkerTest {
    lateinit var context: Context
        private set
    protected lateinit var mockInjector: MockDependencyInjector
        private set

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockInjector = MockDependencyInjector()
        ReadTracker.dependencyInjector = mockInjector
    }

    protected inline fun <reified T : ListenableWorker> runWithWorker(
        inputData: Data = Data.EMPTY,
        noinline block: suspend CoroutineScope.(T) -> Unit
    ) = runBlocking {
        block(TestListenableWorkerBuilder<T>(context, inputData = inputData).build())
    }
}
