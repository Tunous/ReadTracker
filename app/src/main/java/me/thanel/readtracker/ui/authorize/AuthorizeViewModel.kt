package me.thanel.readtracker.ui.authorize

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.chibatching.kotpref.bulk
import me.thanel.goodreadsapi.GoodreadsApi
import me.thanel.readtracker.BuildConfig
import me.thanel.readtracker.Preferences

class AuthorizeViewModel : ViewModel() {
    suspend fun beginAuthorization(): Uri? {
        val response = GoodreadsApi.authorize(
            BuildConfig.GOODREADS_CONSUMER_KEY,
            BuildConfig.GOODREADS_CONSUMER_SECRET
        )

        Preferences.bulk {
            token = response.token
            tokenSecret = response.tokenSecret
            isAuthorized = false
        }

        return Uri.parse(response.authUrl)
    }
}
