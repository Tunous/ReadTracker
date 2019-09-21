package me.thanel.readtracker.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import me.thanel.readtracker.sync.UpdateProgressWorker.Companion.INPUT_BOOK_ID
import me.thanel.readtracker.sync.UpdateProgressWorker.Companion.INPUT_PAGE_NUMBER
import me.thanel.readtracker.sync.UpdateProgressWorker.Companion.INPUT_REVIEW_BODY
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor() {

    private val connectedToNetworkConstraint: Constraints
        get() = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    fun synchronizeDatabase(context: Context) {
        Timber.d("Scheduling database synchronization")
        val request = OneTimeWorkRequestBuilder<ProgressSynchronizationWorker>()
            .setConstraints(connectedToNetworkConstraint)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("progress.synchronization", ExistingWorkPolicy.KEEP, request)
    }

    fun updateProgress(
        context: Context,
        bookId: Long,
        pageNumber: Int,
        reviewBody: String?
    ) {
        Timber.d("Scheduling progress update for book with id=$bookId")
        val inputData = workDataOf(
            INPUT_BOOK_ID to bookId,
            INPUT_PAGE_NUMBER to pageNumber,
            INPUT_REVIEW_BODY to reviewBody
        )
        val request = OneTimeWorkRequestBuilder<UpdateProgressWorker>()
            .setConstraints(connectedToNetworkConstraint)
            .setInputData(inputData)
            .build()
        WorkManager.getInstance(context).enqueue(request)
    }
}
