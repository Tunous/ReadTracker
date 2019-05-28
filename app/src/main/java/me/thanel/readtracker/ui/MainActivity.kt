package me.thanel.readtracker.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.chibatching.kotpref.bulk
import kotlinx.coroutines.launch
import me.thanel.readtracker.Preferences
import me.thanel.readtracker.R
import me.thanel.readtracker.ui.authorize.AuthorizeFragment
import me.thanel.readtracker.ui.authorize.AuthorizeViewModel
import me.thanel.readtracker.ui.base.BaseActivity
import me.thanel.readtracker.ui.readinglist.ReadingListFragment
import me.thanel.readtracker.ui.util.viewModel

class MainActivity : BaseActivity(R.layout.activity_main) {

    private val authorizeViewModel: AuthorizeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            if (Preferences.isAuthorized) {
                displayReadingListFragment()
            } else {
                displayAuthorizeFragment()
            }
        }

        if (intent?.action == Intent.ACTION_VIEW) {
            if (!handleAuthorizedIntent()) {
                displayAuthorizeFragment("Something went wrong")
            }
        }
    }

    private fun displayAuthorizeFragment(error: String? = null) {
        displayFragment(AuthorizeFragment.newInstance(error))
    }

    private fun displayReadingListFragment() {
        displayFragment(ReadingListFragment.newInstance())
    }

    private fun displayFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun handleAuthorizedIntent(): Boolean {
        val uri = intent.data ?: return false
        if (uri.scheme != "me.thanel.readtracker") return false
        if (uri.host != "oauth") return false

        val authorized = uri.getQueryParameter("authorize") == "1"
        val newToken = uri.getQueryParameter("oauth_token")

        if (!authorized || Preferences.token != newToken) {
            Preferences.bulk {
                token = null
                tokenSecret = null
                isAuthorized = false
                userId = null
            }
            return false
        }

        launch {
            authorizeViewModel.finishAuthorization()
            displayReadingListFragment()
        }

        return true
    }
}

