package me.thanel.readtracker.api

import me.thanel.goodreadsapi.GoodreadsApi
import me.thanel.readtracker.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: GoodreadsApi
) {
    suspend fun getUserId(): Long {
        return Preferences.userId ?: fetchAndStoreUserId()
    }

    private suspend fun fetchUserId(): Long {
        val response = api.getUserId()
        return response.user.id
    }

    private suspend fun fetchAndStoreUserId(): Long {
        val userId = fetchUserId()
        Preferences.userId = userId
        return userId
    }
}
