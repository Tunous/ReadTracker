package me.thanel.readtracker.ui.authorize

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.chibatching.kotpref.blockingBulk
import me.thanel.goodreadsapi.GoodreadsApi
import me.thanel.readtracker.BuildConfig
import me.thanel.readtracker.Preferences

class AuthorizeViewModel : ViewModel() {
    suspend fun beginAuthorization(): Uri? {
        val response = GoodreadsApi.authorize(
            BuildConfig.GOODREADS_CONSUMER_KEY,
            BuildConfig.GOODREADS_CONSUMER_SECRET
        )

        Preferences.blockingBulk {
            token = response.token
            tokenSecret = response.tokenSecret
            isAuthorized = false
        }

        return Uri.parse(response.authUrl)
    }

    suspend fun finishAuthorization() {
        val response = GoodreadsApi.getAccessToken(
            BuildConfig.GOODREADS_CONSUMER_KEY,
            BuildConfig.GOODREADS_CONSUMER_SECRET,
            Preferences.token!!,
            Preferences.tokenSecret!!
        )

        Preferences.blockingBulk {
            token = response.token
            tokenSecret = response.tokenSecret
            isAuthorized = true
            userId = null
        }
    }
}
