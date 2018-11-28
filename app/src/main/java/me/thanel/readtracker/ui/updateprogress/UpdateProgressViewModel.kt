package me.thanel.readtracker.ui.updateprogress

import android.util.Log
import androidx.lifecycle.ViewModel
import me.thanel.goodreadsapi.GoodreadsApi
import me.thanel.goodreadsapi.model.UserStatus
import me.thanel.readtracker.BuildConfig
import me.thanel.readtracker.Preferences

class UpdateProgressViewModel : ViewModel() {
    private var userStatuses: List<UserStatus>? = null
    private val api by lazy {
        GoodreadsApi(
            BuildConfig.GOODREADS_CONSUMER_KEY,
            BuildConfig.GOODREADS_CONSUMER_SECRET,
            Preferences.token!!,
            Preferences.tokenSecret!!
        )
    }

    suspend fun getUserId(): Long {
        val response = api.getUserId()
        val userId = response.user.id
        return userId
    }

    suspend fun getUserStatuses(userId: Long): List<UserStatus>? {
        if (userStatuses != null) {
            Log.d(TAG, "Returning existing userStatuses $userStatuses")
            return userStatuses
        }

        Log.d(TAG, "Fetching userStatuses...")
        val response = api.getUser(userId)
        userStatuses = response.user.userStatuses
        Log.d(TAG, "UserStatuses fetched: $userStatuses")
        return userStatuses
    }

    suspend fun updatePercentProgress(bookId: Long, progress: Int) {
        api.updateUserStatus(bookId, progress).await()
    }

    companion object {
        private val TAG = UpdateProgressViewModel::class.java.simpleName
    }
}
