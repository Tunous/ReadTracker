package me.thanel.readtracker.ui.authorize

import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.authorize_fragment.*
import kotlinx.coroutines.launch
import me.thanel.readtracker.R
import me.thanel.readtracker.ui.base.BaseFragment
import me.thanel.readtracker.ui.util.viewModel
import me.thanel.readtracker.ui.util.withArguments

class AuthorizeFragment : BaseFragment(R.layout.authorize_fragment) {
    private val viewModel: AuthorizeViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showOrHideError(arguments?.getString(ARG_ERROR))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loginButton.setOnClickListener {
            beginAuthorization()
        }
    }

    private fun showOrHideError(error: String?) {
        errorTextView.text = error
        errorTextView.visibility = if (error != null) View.VISIBLE else View.GONE
    }

    private fun beginAuthorization() {
        loginButton.isEnabled = false
        launch {
            try {
                val authUri = viewModel.beginAuthorization()
                startActivity(Intent(Intent.ACTION_VIEW, authUri))
            } catch (e: Exception) {
                showOrHideError("Something went wrong: ${e.localizedMessage}")
            } finally {
                loginButton.isEnabled = true
            }
        }
    }

    companion object {
        private const val ARG_ERROR = "error"

        fun newInstance(error: String?) = AuthorizeFragment().withArguments {
            putString(ARG_ERROR, error)
        }
    }
}
