package me.thanel.readtracker.ui.updateprogress

import androidx.lifecycle.ViewModel
import dagger.Lazy
import me.thanel.goodreadsapi.GoodreadsApi
import me.thanel.readtracker.Database
import me.thanel.readtracker.ReadProgressQueries
import me.thanel.readtracker.api.ReadingProgressRepository
import me.thanel.readtracker.api.UserRepository
import me.thanel.readtracker.di.ReadTracker
import javax.inject.Inject

class UpdateProgressViewModel : ViewModel() {

    @Inject
    internal lateinit var lazyApi: Lazy<GoodreadsApi>
    private val api get() = lazyApi.get()

    @Inject
    internal lateinit var userRepository: UserRepository

    @Inject
    lateinit var readingProgressRepository: ReadingProgressRepository

    @Inject
    internal lateinit var readProgressQueries: ReadProgressQueries

    @Inject
    internal lateinit var database: Database

    init {
        ReadTracker.dependencyInjector.inject(this)
    }

    suspend fun synchronizeDatabase() {
        readingProgressRepository.synchronizeDatabase()
    }

    fun getReadingStatusLiveData() =
        readProgressQueries.selectWithBookInformation().executeAsListLiveData()

    suspend fun updateProgressByPercent(bookId: Long, progress: Int, body: String?) {
        api.updateProgressByPercent(bookId, progress, body)
    }

    suspend fun updateProgressByPageNumber(bookId: Long, progress: Int, body: String?) {
        api.updateProgressByPageNumber(bookId, progress, body)
    }

    suspend fun finishReading(reviewId: Long, rating: Int, body: String?) {
        api.finishReading(reviewId, rating, body)
    }
}
