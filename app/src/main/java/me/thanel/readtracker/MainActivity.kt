package me.thanel.readtracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.chibatching.kotpref.bulk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.thanel.readtracker.ui.authorize.AuthorizeFragment
import me.thanel.readtracker.ui.authorize.AuthorizeViewModel
import me.thanel.readtracker.ui.readinglist.ReadingListFragment
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private lateinit var uiJob: Job

    override val coroutineContext: CoroutineContext
        get() = uiJob + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        uiJob = Job()

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

    override fun onDestroy() {
        super.onDestroy()
        uiJob.cancel()
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

        val viewModel = ViewModelProviders.of(this).get(AuthorizeViewModel::class.java)
        launch {
            viewModel.finishAuthorization()
            displayReadingListFragment()
        }

        return true
    }
}

