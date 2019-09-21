package me.thanel.readtracker.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import me.thanel.readtracker.api.ReadingProgressRepository
import me.thanel.readtracker.di.ReadTracker
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class ProgressSynchronizationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @Inject
    lateinit var readingProgressRepository: ReadingProgressRepository

    init {
        ReadTracker.dependencyInjector.inject(this)
    }

    override suspend fun doWork(): Result {
        Timber.d("Synchronizing database...")
        try {
            readingProgressRepository.synchronizeDatabase()
            Timber.d("Database synchronization finished with success")
        } catch (exception: IOException) {
            Timber.e(exception, "Database synchronization failed")
            return Result.retry()
        }

        return Result.success()
    }
}
