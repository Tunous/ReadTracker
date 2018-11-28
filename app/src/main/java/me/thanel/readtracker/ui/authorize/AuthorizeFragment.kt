package me.thanel.readtracker.ui.authorize

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.authorize_fragment.*
import kotlinx.coroutines.launch
import me.thanel.readtracker.R
import me.thanel.readtracker.ui.base.BaseFragment

class AuthorizeFragment : BaseFragment(R.layout.authorize_fragment) {
    private lateinit var viewModel: AuthorizeViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val error = arguments?.getString(ARG_ERROR)
        errorTextView.text = error
        errorTextView.visibility = if (error != null) View.VISIBLE else View.GONE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AuthorizeViewModel::class.java)

        loginButton.setOnClickListener {
            beginAuthorization()
        }
    }

    private fun beginAuthorization() {
        launch {
            try {
                loginButton.isEnabled = false
                val authUri = viewModel.beginAuthorization()
                startActivity(Intent(Intent.ACTION_VIEW, authUri))
            } finally {
                loginButton.isEnabled = true
            }
        }
    }

    companion object {
        private const val ARG_ERROR = "error"

        fun newInstance(error: String?) = AuthorizeFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ERROR, error)
            }
        }
    }
}
