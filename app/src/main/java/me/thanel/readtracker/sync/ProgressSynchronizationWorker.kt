package me.thanel.readtracker.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    override suspend fun doWork() = withContext(Dispatchers.IO) {
        Timber.d("Synchronizing reading progress...")
        try {
            readingProgressRepository.synchronizeDatabase()
            Timber.d("Reading progress synchronization finished with success")
        } catch (exception: IOException) {
            Timber.e(exception, "Reading progress synchronization failed")
            return@withContext Result.retry()
        }

        return@withContext Result.success()
    }

    companion object {
        fun enqueue(context: Context) {
            Timber.d("Enqueuing reading progress update")
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<ProgressSynchronizationWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork("progress.synchronization", ExistingWorkPolicy.KEEP, request)
        }
    }
}
