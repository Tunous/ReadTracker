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
import kotlin.math.roundToInt

class UpdateProgressFragment : BaseFragment(R.layout.update_progress_fragment) {
    private lateinit var viewModel: UpdateProgressViewModel

    private var numPages = 0
    private var bookId = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && progressInput.text.toIntOrElse { 0 } > progressSeekBar.max) {
                progressInput.setText(progressSeekBar.max.toString())
            }
        }

        setupSeekBarProgressListener()

        userStatusCommentTextInputEditText.afterTextChanged {
            val inputLength = it?.length ?: 0
            updateProgressButton.isEnabled =
                    inputLength <= userStatusCommentTextInputLayout.counterMaxLength
        }

        updateProgressButton.setOnClickListener {
            launch {
                // TODO: Use correct progress type
                val body = userStatusCommentTextInputEditText.text?.toString()
                viewModel.updatePercentProgress(bookId, progressSeekBar.progress, body)
                Toast.makeText(requireContext(), "Updated progress", Toast.LENGTH_SHORT).show()
                userStatusCommentTextInputEditText.text?.clear()
            }
        }

        progressTypeSegmentedGroup.setOnCheckedChangeListener { _, checkedId ->
            setupProgressTypeButton(checkedId == R.id.pageProgressTypeButton)
        }

        increaseProgressButton.setOnClickListener {
            progressSeekBar.progress += 1
        }
        decreaseProgressButton.setOnClickListener {
            progressSeekBar.progress -= 1
        }
    }

    private fun setupSeekBarProgressListener() {
        var skipChange = false
        progressSeekBar.onProgressChanged {
            if (!skipChange) {
                progressInput.setText(it.toString())
            }
            val isMax = it == progressSeekBar.max
            val isMin = it == 0
            updateProgressButton.setText(if (isMax) R.string.action_finished else R.string.action_update_progress)
            increaseProgressButton.isEnabled = !isMax
            decreaseProgressButton.isEnabled = !isMin
        }

        progressInput.afterTextChanged {
            skipChange = true
            progressSeekBar.progress = it.toIntOrElse { 0 }
            skipChange = false
        }
    }

    private fun setupProgressTypeButton(usePages: Boolean) {
        val progress = progressSeekBar.progress / progressSeekBar.max.toFloat()
        if (usePages) {
            progressTypeSegmentedGroup.check(R.id.pageProgressTypeButton)
            progressSeekBar.max = numPages
        } else {
            progressTypeSegmentedGroup.check(R.id.percentProgressTypeButton)
            progressSeekBar.max = 100
        }

        val newProgress = (progress * progressSeekBar.max).roundToInt()
        progressInput.setText(newProgress.toString())
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
            pagesTextView.text = getString(R.string.info_pages, book.numPages)

            bookId = book.id
            numPages = book.numPages

            if (userStatus.page > 0) {
                setupProgressTypeButton(true)
                progressSeekBar.progress = userStatus.page
            } else {
                setupProgressTypeButton(false)
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
