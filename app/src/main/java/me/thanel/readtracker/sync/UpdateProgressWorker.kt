package me.thanel.readtracker.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import me.thanel.readtracker.api.ReadingProgressRepository
import me.thanel.readtracker.di.ReadTracker
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class UpdateProgressWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    @Inject
    lateinit var readingProgressRepository: ReadingProgressRepository

    init {
        ReadTracker.dependencyInjector.inject(this)
    }

    override suspend fun doWork(): Result {
        val bookId = inputData.getLong(INPUT_BOOK_ID, 0)
        val pageNumber = inputData.getInt(INPUT_PAGE_NUMBER, 0)
        val reviewBody = inputData.getString(INPUT_REVIEW_BODY)

        Timber.d("Updating progress for book with id=$bookId...")
        try {
            readingProgressRepository.updateProgressByPageNumber(bookId, pageNumber, reviewBody)
            Timber.d("Progress for book with id=$bookId has been updated")
        } catch (exception: IOException) {
            Timber.e(exception, "Progress update for book with id=$bookId has failed")
            return Result.retry()
        }

        return Result.success()
    }

    companion object {
        internal const val INPUT_BOOK_ID = "bookId"
        internal const val INPUT_PAGE_NUMBER = "pageNumber"
        internal const val INPUT_REVIEW_BODY = "reviewBody"

        fun enqueue(context: Context, bookId: Long, pageNumber: Int, reviewBody: String?) {
            Timber.d("Enqueuing progress update for book with id=$bookId")
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val inputData = workDataOf(
                INPUT_BOOK_ID to bookId,
                INPUT_PAGE_NUMBER to pageNumber,
                INPUT_REVIEW_BODY to reviewBody
            )
            val request = OneTimeWorkRequestBuilder<UpdateProgressWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}
