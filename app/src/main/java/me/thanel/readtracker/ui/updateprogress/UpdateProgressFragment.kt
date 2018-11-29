package me.thanel.readtracker.ui.updateprogress

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_book_card.*
import kotlinx.android.synthetic.main.update_progress_fragment.*
import kotlinx.coroutines.launch
import me.thanel.readtracker.R
import me.thanel.readtracker.ui.base.BaseFragment
import me.thanel.readtracker.ui.util.afterTextChanged
import me.thanel.readtracker.ui.util.onProgressChanged
import me.thanel.readtracker.ui.util.toIntOrElse

class UpdateProgressFragment : BaseFragment(R.layout.update_progress_fragment) {
    private lateinit var viewModel: UpdateProgressViewModel

    private var numPages = 0
    private var usePages = false
    private var bookId = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && progressInput.text.toIntOrElse { 0 } > progressSeekBar.max) {
                progressInput.setText(progressSeekBar.max.toString())
            }
        }

        setupSeekBarProgressListener()

        progressTypeButton.setOnClickListener {
            usePages = !usePages
            updateProgress()
        }

        userStatusCommentTextInputEditText.afterTextChanged {
            val inputLength = it?.length ?: 0
            updateProgressButton.isEnabled =
                    inputLength <= userStatusCommentTextInputLayout.counterMaxLength
        }

        updateProgressButton.setOnClickListener {
            launch {
                val body = userStatusCommentTextInputEditText.text?.toString()
                viewModel.updatePercentProgress(bookId, progressSeekBar.progress, body)
                Toast.makeText(requireContext(), "Updated progress", Toast.LENGTH_SHORT).show()
                userStatusCommentTextInputEditText.text?.clear()
            }
        }
    }

    private fun setupSeekBarProgressListener() {
        var skipChange = false
        progressSeekBar.onProgressChanged {
            if (!skipChange) {
                progressInput.setText(it.toString())
            }
            updateProgressButton.setText(if (it == progressSeekBar.max) R.string.action_finished else R.string.action_update_progress)
        }

        progressInput.afterTextChanged {
            skipChange = true
            progressSeekBar.progress = it.toIntOrElse { 0 }
            skipChange = false
        }
    }

    private fun updateProgress() {
        val progress = progressSeekBar.progress / progressSeekBar.max.toFloat()
        if (usePages) {
            progressTypeButton.text = getString(R.string.hint_pages_done, numPages)
            progressSeekBar.max = numPages
        } else {
            progressTypeButton.setText(R.string.hint_percentage_done)
            progressSeekBar.max = 100
        }
        progressInput.setText((progress * progressSeekBar.max).toInt().toString())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(UpdateProgressViewModel::class.java)
        launch {
            val userId = viewModel.getUserId()
            val userStatuses = viewModel.getUserStatuses(userId)
            val userStatus = userStatuses?.firstOrNull() ?: return@launch
            val book = userStatus.book
            bookTitleView.text = book.title
            bookAuthorView.text =
                    getString(R.string.info_authors, book.authors.joinToString { it.name })

            bookId = book.id
            numPages = book.numPages

            if (userStatus.page > 0) {
                usePages = true
                updateProgress()
                progressSeekBar.progress = userStatus.page
            } else {
                usePages = false
                updateProgress()
                progressSeekBar.progress = userStatus.percent
            }

            Picasso.get()
                .load(book.imageUrl)
                .placeholder(ColorDrawable(Color.BLACK))
                .into(imageView)

            bookInformationGroup.visibility = View.VISIBLE
            bookInformationProgressBar.visibility = View.GONE
        }
    }

    companion object {
        fun newInstance() = UpdateProgressFragment()
    }
}
