package me.thanel.readtracker.ui.updateprogress

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_book_card.*
import kotlinx.android.synthetic.main.update_progress_fragment.*
import kotlinx.coroutines.launch
import me.thanel.readtracker.R
import me.thanel.readtracker.ui.base.BaseFragment
import me.thanel.readtracker.ui.util.connectTo
import me.thanel.readtracker.ui.util.toIntOrElse

class UpdateProgressFragment : BaseFragment(R.layout.update_progress_fragment) {
    private lateinit var viewModel: UpdateProgressViewModel

    private var numPages = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressSeekBar.connectTo(progressInput)
        progressInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && progressInput.text.toIntOrElse { 0 } > progressSeekBar.max) {
                progressInput.setText(progressSeekBar.max.toString())
            }
        }
        progressTypeButton.setOnClickListener {
            val progress = progressSeekBar.progress / progressSeekBar.max.toFloat()
            if (progressTypeButton.text.startsWith("%")) {
                progressTypeButton.text = getString(R.string.hint_pages_done, numPages)
                progressSeekBar.max = numPages
            } else {
                progressTypeButton.setText(R.string.hint_percentage_done)
                progressSeekBar.max = 100
            }
            progressInput.setText((progress * progressSeekBar.max).toInt().toString())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(UpdateProgressViewModel::class.java)
        launch {
            val reviews = viewModel.listReviews(77741768, "currently-reading")
            val review = reviews?.firstOrNull() ?: return@launch
            bookTitleView.text = review.book.title
            bookAuthorView.text =
                    getString(R.string.info_authors, review.book.authors.joinToString { it.name })
            ratingView.text = review.book.averageRating.toString()
            totalRatingsView.text = getString(R.string.info_num_ratings, review.book.ratingsCount)

            numPages = review.book.numPages

            Picasso.get()
                .load(review.book.imageUrl)
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
