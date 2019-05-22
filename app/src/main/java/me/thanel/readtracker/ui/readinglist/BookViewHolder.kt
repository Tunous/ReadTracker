package me.thanel.readtracker.ui.readinglist

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_currently_reading_book.view.*
import me.thanel.goodreadsapi.internal.util.nullIfBlank
import me.thanel.readtracker.R
import me.thanel.readtracker.SelectWithBookInformation
import me.thanel.readtracker.model.actualPage
import me.thanel.readtracker.ui.util.RangeInputFilter
import kotlin.math.roundToInt

class BookViewHolder(
    itemView: View,
    onUpdateProgress: (SelectWithBookInformation, Int) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val pagesTextView = itemView.pagesTextView
    private val updateProgressButton = itemView.updateProgressButton
    private val bookTitleView = itemView.bookTitleView
    private val bookAuthorView = itemView.bookAuthorView
    private val bookCoverImageView = itemView.bookCoverImageView
    private val readPercentageEditTextView = itemView.readPercentageEditTextView
    private val readPercentageTextView = itemView.readPercentageTextView
    private val bookProgressView = itemView.bookProgressView

    private val progressItem: SelectWithBookInformation
        get() = itemView.tag as SelectWithBookInformation

    init {
        itemView.bookProgressView.onProgressChangeListener = this::handleProgressChanged
        updateProgressButton.setOnClickListener {
            val progress = itemView.bookProgressView.currentValue
            onUpdateProgress(progressItem, progress)
        }
        readPercentageEditTextView.afterUserTextChanged = {
            val progress = it?.toIntOrNull() ?: 0
            bookProgressView.currentValue = percentToPage(progress, progressItem.numPages)
        }
        readPercentageEditTextView.filters = arrayOf(RangeInputFilter(0..100))
        readPercentageTextView.setOnClickListener {
            readPercentageEditTextView.requestFocus()
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
        with(bookProgressView) {
            maxValue = item.numPages
            currentValue = item.actualPage
            bindProgress(item, currentValue)
        }
    }

    private fun bindProgress(progressItem: SelectWithBookInformation, page: Int) {
        val numPages = progressItem.numPages
        val percent = pageToPercent(page, numPages)

        pagesTextView.text =
            itemView.context.getString(R.string.info_book_pages_progress, page, numPages)

        val percentText = percent.toString()
        val currentText = readPercentageEditTextView.text?.toString()?.nullIfBlank() ?: "0"
        if (percentText != currentText) {
            readPercentageEditTextView.setText(percentText)
        }
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
        animateShowSaveButton()
    }

    private fun animateShowSaveButton() = with(updateProgressButton) {
        if (visibility != View.GONE) return
        alpha = 0f
        visibility = View.VISIBLE
        animate().alpha(1f).setDuration(1000).start()
    }
}
