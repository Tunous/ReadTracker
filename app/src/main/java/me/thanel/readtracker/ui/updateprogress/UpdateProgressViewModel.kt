package me.thanel.readtracker.ui.updateprogress

import androidx.lifecycle.ViewModel
import dagger.Lazy
import me.thanel.goodreadsapi.GoodreadsApi
import me.thanel.goodreadsapi.internal.util.nullIfBlank
import me.thanel.readtracker.Database
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
    internal lateinit var database: Database

    init {
        ReadTracker.dependencyInjector.inject(this)
    }

    suspend fun fetchReadProgress() {
        val userId = userRepository.getUserId()
        val response = api.getUser(userId)
        response.user.userStatuses?.let { statuses ->
            val books = statuses
                .map { it.book }
                .distinctBy { it.id }

            database.transaction {
                database.bookQueries.deleteAll()
                database.readProgressQueries.deleteAll()

                for (book in books) {
                    database.bookQueries.insert(
                        book.id,
                        book.title,
                        book.numPages,
                        book.imageUrl,
                        book.authors.joinToString { it.name }.nullIfBlank()
                    )
                }

                for (status in statuses) {
                    database.readProgressQueries.insert(
                        status.id,
                        status.book.id,
                        status.page,
                        status.percent,
                        status.reviewId
                    )
                }
            }
        }
    }

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
