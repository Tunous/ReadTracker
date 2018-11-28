package me.thanel.readtracker.ui.updateprogress

import android.util.Log
import androidx.lifecycle.ViewModel
import me.thanel.goodreadsapi.GoodreadsApi
import me.thanel.goodreadsapi.model.UserStatus

class UpdateProgressViewModel : ViewModel() {
    private var userStatuses: List<UserStatus>? = null

    suspend fun getUserStatuses(userId: Long): List<UserStatus>? {
        if (userStatuses != null) {
            Log.d(TAG, "Returning existing userStatuses $userStatuses")
            return userStatuses
        }

        Log.d(TAG, "Fetching userStatuses...")
        val response =  GoodreadsApi.getUser(userId)
        userStatuses = response.user.userStatuses
        Log.d(TAG, "UserStatuses fetched: $userStatuses")
        return userStatuses
    }

    companion object {
        private val TAG = UpdateProgressViewModel::class.java.simpleName
    }
}
