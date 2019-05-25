package me.thanel.readtracker.api

import me.thanel.goodreadsapi.GoodreadsApi
import me.thanel.readtracker.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: GoodreadsApi
) {
    suspend fun getUserId() = Preferences.userId ?: fetchAndStoreUserId()

    private suspend fun fetchUserId() = api.getUserId()

    private suspend fun fetchAndStoreUserId() = fetchUserId().also {
        Preferences.userId = it
    }
}
