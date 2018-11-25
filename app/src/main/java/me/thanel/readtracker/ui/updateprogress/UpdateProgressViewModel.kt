package me.thanel.readtracker.ui.updateprogress

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thanel.readtracker.api.GoodreadsApi
import me.thanel.readtracker.api.model.Review

class UpdateProgressViewModel : ViewModel() {
    private var reviews: List<Review>? = null

    suspend fun listReviews(id: Int, shelf: String): List<Review>? {
        if (reviews != null) {
            Log.d(TAG, "Returning existing reviews $reviews")
            return reviews
        }

        Log.d(TAG, "Fetching reviews...")
        val response = withContext(Dispatchers.IO) {
            GoodreadsApi.service.getMemberBooksFromShelf(id, shelf).await()
        }
        reviews = response.reviews.reviews
        Log.d(TAG, "Reviews fetched: $reviews")
        return reviews
    }

    companion object {
        private val TAG = UpdateProgressViewModel::class.java.simpleName
    }
}
