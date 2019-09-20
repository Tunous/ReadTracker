package me.thanel.readtracker.sync

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import me.thanel.readtracker.di.ReadTracker
import me.thanel.readtracker.testbase.MockDependencyInjector
import me.thanel.readtracker.testbase.spyWithTestScope
import org.hamcrest.Matchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.isNull
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import java.io.IOException

@ExperimentalCoroutinesApi
class ProgressSynchronizationServiceTest {

    private lateinit var service: ProgressSynchronizationService
    private lateinit var mockInjector: MockDependencyInjector
    private val testScope = TestCoroutineScope()

    @Before
    fun setup() {
        mockInjector = MockDependencyInjector()
        ReadTracker.dependencyInjector = mockInjector

        service = spy(ProgressSynchronizationService())
        service.spyWithTestScope(testScope)

        doNothing().`when`(service).jobFinished(isNull(), anyBoolean())
    }

    @After
    fun cleanup() {
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun `service should invoke database synchronization process`() = testScope.runBlockingTest {
        service.onStartJob(null)

        verify(mockInjector.readingProgressRepositoryMock).synchronizeDatabase()
    }

    @Test
    fun `service should continue running after onStartJob finishes`() {
        val shouldRun = service.onStartJob(null)

        assertThat(shouldRun, equalTo(true))
    }

    @Test
    fun `service should finish without rescheduling on success`() = testScope.runBlockingTest {
        service.onStartJob(null)

        verify(service).jobFinished(null, false)
    }

    @Test
    fun `error while synchronizing should result in service rescheduling`() =
        testScope.runBlockingTest {
            `when`(mockInjector.readingProgressRepositoryMock.synchronizeDatabase())
                .thenAnswer {
                    throw IOException()
                }

            service.onStartJob(null)

            verify(service).jobFinished(null, true)
        }

    @Test
    fun `stopping service should reschedule`() {
        service.onStartJob(null)

        val shouldReschedule = service.onStopJob(null)

        assertThat(shouldReschedule, equalTo(true))
    }

    @Test
    fun `stopping service should cancel background tasks`() {
        service.onStartJob(null)
        verifyIsCancelled(false)

        service.onStopJob(null)
        verifyIsCancelled(true)
    }

    private fun verifyIsCancelled(cancelled: Boolean) {
        assertThat(service.coroutineContext[Job]?.isCancelled, equalTo(cancelled))
    }
}
