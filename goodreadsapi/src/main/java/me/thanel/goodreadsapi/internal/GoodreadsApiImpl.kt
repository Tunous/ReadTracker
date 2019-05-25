package me.thanel.goodreadsapi.internal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thanel.goodreadsapi.GoodreadsApi
import me.thanel.goodreadsapi.GoodreadsSecrets
import me.thanel.goodreadsapi.internal.model.ShortDate
import me.thanel.goodreadsapi.internal.model.UserStatus
import me.thanel.goodreadsapi.internal.util.nullIfBlank
import me.thanel.goodreadsapi.model.Book
import me.thanel.goodreadsapi.model.ReadingProgressStatus
import me.thanel.goodreadsapi.model.ReadingProgressStatusGroup
import kotlin.math.roundToInt

internal class GoodreadsApiImpl(secrets: GoodreadsSecrets, baseUrl: String) : GoodreadsApi {

    private val service by lazy {
        GoodreadsServiceCreator(baseUrl, secrets).createService()
    }

    /// Requests

    override suspend fun getUserId() = withContext(Dispatchers.Default) {
        service.getUserIdAsync().await().user.id
    }

    override suspend fun getReadingProgressStatus(userId: Long): ReadingProgressStatusGroup =
        withContext(Dispatchers.Default) {
            val response = service.getUserAsync(userId).await()
            val internalStatuses = response.user.userStatuses
            val statuses = internalStatuses?.map { status ->
                // User might have saved their progress using percentages instead of pages.
                // In [getCurrentPage] we are converting these progresses to pages which is the
                // only way how this app stores info about every progress.
                val page = getCurrentPage(status)
                ReadingProgressStatus(status.id, status.book.id, page, status.reviewId)
            }
            val books = internalStatuses?.map {
                it.book.toPublicBook(true)
            }
            return@withContext ReadingProgressStatusGroup(
                statuses ?: emptyList(),
                books ?: emptyList()
            )
        }

    override suspend fun getBooksInShelf(userId: Long, shelf: String) =
        withContext(Dispatchers.Default) {
            val response = service.getBooksInShelfAsync(userId, shelf).await()
            val reviews = response.reviews?.map { review ->
                val shelves = review.shelves ?: emptyList()
                val isCurrentlyReading = shelves.any { it.name == "currently-reading" }
                review.book.toPublicBook(isCurrentlyReading)
            }
            reviews ?: emptyList()
        }

    /// Actions

    override suspend fun updateProgressByPageNumber(bookId: Long, page: Int, body: String?) {
        withContext(Dispatchers.Default) {
            service.updateUserStatusByPageNumberAsync(bookId, page, body.nullIfBlank()).await()
        }
    }

    override suspend fun startReadingBook(bookId: Long) {
        withContext(Dispatchers.Default) {
            service.addBookToShelfAsync("currently-reading", bookId).await()
        }
    }

    override suspend fun finishReading(reviewId: Long, rating: Int?, body: String?) {
        withContext(Dispatchers.Default) {
            service.editReviewAsync(
                reviewId = reviewId,
                reviewText = body.nullIfBlank(),
                rating = rating,
                dateRead = ShortDate.now(),
                shelf = "read",
                finished = true
            ).await()
        }
    }

    /// Private

    private fun getCurrentPage(userStatus: UserStatus): Int {
        val page = userStatus.page
        if (page != null && page > 0) return page
        val percent = userStatus.percent ?: 0
        return percentToPage(percent, userStatus.book.numPages)
    }

    private fun percentToPage(percent: Int, numPages: Int): Int {
        val floatPercent = percent / 100f
        return (floatPercent * numPages).roundToInt()
    }

    private fun me.thanel.goodreadsapi.internal.model.Book.toPublicBook(isCurrentlyReading: Boolean) =
        Book(
            id,
            title,
            numPages,
            imageUrl,
            authors.joinToString { it.name }.nullIfBlank(),
            isCurrentlyReading = isCurrentlyReading
        )
}