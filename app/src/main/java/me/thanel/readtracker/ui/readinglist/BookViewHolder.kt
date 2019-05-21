package me.thanel.readtracker.ui.readinglist

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_currently_reading_book.view.*
import me.thanel.readtracker.R
import me.thanel.readtracker.SelectWithBookInformation
import me.thanel.readtracker.model.ProgressType
import me.thanel.readtracker.model.progressType
import kotlin.math.roundToInt

class BookViewHolder(
    itemView: View,
    onUpdateProgress: (SelectWithBookInformation, Int) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val progressIndicatorView = itemView.bookProgressIndicatorView
    private val updateProgressButton = itemView.updateBookProgressButton
    private val bookTitleView = itemView.bookTitleView
    private val bookAuthorView = itemView.bookAuthorView
    private val bookCoverImageView = itemView.bookCoverImageView

    private val progressItem: SelectWithBookInformation
        get() = itemView.tag as SelectWithBookInformation

    init {
        itemView.bookProgressView.onProgressChangeListener = this::handleProgressChanged
        updateProgressButton.setOnClickListener {
            val progress = itemView.bookProgressView.currentValue
            onUpdateProgress(progressItem, progress)
        }
    }

    fun bind(item: SelectWithBookInformation) {
        itemView.tag = item

        bindBookInformation(item)
        bindImage(item)
        bindProgressView(item)

        updateProgressButton.visibility = View.GONE
        updateProgressButton.alpha = 0f
    }

    private fun bindBookInformation(item: SelectWithBookInformation) {
        bookTitleView.text = item.bookTitle
        bookAuthorView.text = itemView.context.getString(R.string.info_authors, item.bookAuthors)
    }

    private fun bindImage(item: SelectWithBookInformation) {
        if (item.bookImageUrl != null) {
            Picasso.get()
                .load(item.bookImageUrl!!)
                .placeholder(ColorDrawable(Color.BLACK))
                .into(bookCoverImageView)
        } else {
            bookCoverImageView.setImageDrawable(ColorDrawable(Color.BLACK))
        }
    }

    private fun bindProgressView(item: SelectWithBookInformation) {
        with(itemView.bookProgressView) {
            when (item.progressType) {
                ProgressType.Page -> {
                    maxValue = item.numPages
                    currentValue = item.page ?: 0
                }
                ProgressType.Percent -> {
                    maxValue = 100
                    currentValue = item.percent ?: 0
                }
            }
            bindProgress(item, currentValue)
        }
    }

    private fun bindProgress(progressItem: SelectWithBookInformation, progress: Int) {
        val numPages = progressItem.numPages
        val page: Int
        val percent: Int
        when (progressItem.progressType) {
            ProgressType.Page -> {
                page = progress
                percent = pageToPercent(page, numPages)
            }
            ProgressType.Percent -> {
                percent = progress
                page = percentToPage(percent, numPages)
            }
        }
        progressIndicatorView.text =
            itemView.context.getString(R.string.info_book_progress, page, numPages, percent)

        animateShowSaveButton()
    }

    private fun pageToPercent(page: Int, numPages: Int): Int {
        val floatPercent = page / numPages.toFloat()
        return (floatPercent * 100).roundToInt()
    }

    private fun percentToPage(percent: Int, numPages: Int): Int {
        val floatPercent = percent / 100f
        return (floatPercent * numPages).roundToInt()
    }

    private fun handleProgressChanged(progress: Int) {
        bindProgress(progressItem, progress)
    }

    private fun animateShowSaveButton() = with(updateProgressButton) {
        if (visibility != View.GONE) return
        alpha = 0f
        visibility = View.VISIBLE
        animate().alpha(1f).setDuration(1000).start()
    }
}
