package me.thanel.readtracker.ui.review

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import dagger.Lazy
import kotlinx.android.synthetic.main.dialog_review.*
import kotlinx.android.synthetic.main.dialog_review.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.thanel.readtracker.R
import me.thanel.readtracker.api.ReadingProgressRepository
import me.thanel.readtracker.di.ReadTracker
import me.thanel.readtracker.ui.util.requireArguments
import me.thanel.readtracker.ui.util.withArguments
import javax.inject.Inject

class ReviewDialog : DialogFragment() {

    @Inject
    internal lateinit var readingProgressRepository: Lazy<ReadingProgressRepository>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReadTracker.dependencyInjector.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val positiveButtonText =
            if (hasFinishedReading()) R.string.action_submit_review
            else R.string.action_update_progress

        return AlertDialog.Builder(requireContext())
            .setView(createDialogView())
            .setPositiveButton(positiveButtonText) { _, _ -> submitReview() }
            .setNegativeButton(R.string.action_cancel, null)
            .create()
    }

    @SuppressLint("InflateParams")
    private fun createDialogView(): View {
        return requireActivity().layoutInflater.inflate(R.layout.dialog_review, null).apply {
            this@apply.ratingBar.visibility = if (hasFinishedReading()) View.VISIBLE else View.GONE
        }
    }

    private fun hasFinishedReading() = requireArguments().containsKey(ARG_REVIEW_ID)

    private fun getReviewBody() = dialog.reviewBodyTextInputLayout.editText?.text?.toString()

    private fun getRating() = dialog.ratingBar.rating.toInt()

    private fun submitReview() {
        if (hasFinishedReading()) {
            submitFinishedReview()
        } else {
            submitInProgressReview()
        }
    }

    private fun submitInProgressReview() {
        val arguments = requireArguments()
        val bookId = arguments.getLong(ARG_BOOK_ID)
        val progress = arguments.getInt(ARG_PROGRESS)
        val reviewBody = getReviewBody()

        GlobalScope.launch {
            val repository = readingProgressRepository.get()
            repository.updateProgressByPageNumber(bookId, progress, reviewBody)
        }
    }

    private fun submitFinishedReview() {
        val reviewId = requireArguments().getLong(ARG_REVIEW_ID)
        val rating = getRating()
        val reviewBody = getReviewBody()

        GlobalScope.launch {
            readingProgressRepository.get().finishReading(reviewId, rating, reviewBody)
        }
    }

    companion object {
        private const val ARG_REVIEW_ID = "reviewId"
        private const val ARG_BOOK_ID = "bookId"
        private const val ARG_PROGRESS = "progress"

        fun createForInProgress(
            bookId: Long,
            progress: Int
        ): ReviewDialog =
            ReviewDialog().withArguments {
                putLong(ARG_BOOK_ID, bookId)
                putInt(ARG_PROGRESS, progress)
            }

        fun createForFinished(reviewId: Long): ReviewDialog = ReviewDialog().withArguments {
            putLong(ARG_REVIEW_ID, reviewId)
        }
    }
}
