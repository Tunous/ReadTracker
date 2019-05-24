package me.thanel.readtracker.api

import me.thanel.goodreadsapi.GoodreadsApiInterface
import me.thanel.readtracker.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: GoodreadsApiInterface
) {
    suspend fun getUserId() = Preferences.userId ?: fetchAndStoreUserId()

    private suspend fun fetchUserId() = api.getUserId()

    private suspend fun fetchAndStoreUserId() = fetchUserId().also {
        Preferences.userId = it
    }
}
