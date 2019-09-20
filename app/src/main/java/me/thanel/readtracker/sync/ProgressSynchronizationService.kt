package me.thanel.readtracker.sync

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.thanel.readtracker.api.ReadingProgressRepository
import me.thanel.readtracker.di.ReadTracker
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class ProgressSynchronizationService : JobService(), CoroutineScope {

    private var backgroundJob = Job()

    @Inject
    lateinit var readingProgressRepository: ReadingProgressRepository

    override val coroutineContext: CoroutineContext
        get() = backgroundJob + Dispatchers.IO

    init {
        ReadTracker.dependencyInjector.inject(this)
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Timber.d("Synchronizing reading progress...")
        backgroundJob = Job()
        launch {
            try {
                readingProgressRepository.synchronizeDatabase()
                jobFinished(params, false)
                Timber.d("Reading progress synchronization finished with success")
            } catch (exception: IOException) {
                jobFinished(params, true)
                Timber.e(exception, "Reading progress synchronization failed")
            } catch (exception: CancellationException) {
                jobFinished(params, true)
            }
        }
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Timber.d("Reading progress synchronization service has stopped")
        backgroundJob.cancel()
        return true
    }

    companion object {
        private const val SYNCHRONIZE_JOB_ID = 1

        fun schedule(context: Context) {
            Timber.d("Scheduling reading progress update")
            val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            val serviceName =
                ComponentName(context.packageName, ProgressSynchronizationService::class.java.name)
            val jobInfo = JobInfo.Builder(SYNCHRONIZE_JOB_ID, serviceName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build()
            scheduler.schedule(jobInfo)
        }
    }
}
