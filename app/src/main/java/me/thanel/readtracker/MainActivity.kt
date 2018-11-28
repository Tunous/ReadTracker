package me.thanel.readtracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.chibatching.kotpref.bulk
import me.thanel.readtracker.ui.authorize.AuthorizeFragment
import me.thanel.readtracker.ui.updateprogress.UpdateProgressFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            if (Preferences.isAuthorized) {
                displayProgressFragment()
            } else {
                displayAuthorizeFragment()
            }
        }

        if (intent?.action == Intent.ACTION_VIEW) {
            if (handleAuthorizedIntent()) {
                displayProgressFragment()
            } else {
                displayAuthorizeFragment("Something went wrong")
            }
        }
    }

    private fun displayAuthorizeFragment(error: String? = null) {
        displayFragment(AuthorizeFragment.newInstance(error))
    }

    private fun displayProgressFragment() {
        displayFragment(UpdateProgressFragment.newInstance())
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
            }
            return false
        }

        Preferences.isAuthorized = true
        return true
    }
}

