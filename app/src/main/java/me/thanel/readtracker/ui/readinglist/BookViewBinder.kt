package me.thanel.readtracker.ui.readinglist

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.inputmethod.EditorInfo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_currently_reading_book.*
import me.thanel.goodreadsapi.internal.util.nullIfBlank
import me.thanel.readtracker.R
import me.thanel.readtracker.model.BookWithProgress
import me.thanel.readtracker.ui.util.RangeInputFilter
import me.thanel.readtracker.ui.util.getColorFromAttr
import me.thanel.readtracker.ui.util.showKeyboard
import me.thanel.recyclerviewutils.viewholder.ContainerViewHolder
import me.thanel.recyclerviewutils.viewholder.SimpleItemViewBinder
import me.thanel.swipeprogressview.setProgressColor
import kotlin.math.roundToInt

class BookViewBinder(
    private val onUpdateProgress: (BookWithProgress, Int) -> Unit
) : SimpleItemViewBinder<BookWithProgress>(R.layout.item_currently_reading_book) {

    private val ContainerViewHolder.bookWithProgress: BookWithProgress
        get() = itemView.getTag(R.id.bound_item) as BookWithProgress

    override fun onCreateViewHolder(itemView: View) = super.onCreateViewHolder(itemView)
        .also(::initViewHolder)

    private fun initViewHolder(holder: ContainerViewHolder) {
        val color = holder.context.getColorFromAttr(R.attr.colorControlActivated, alpha = 0.2f)
        holder.bookProgressView.setProgressColor(color)
        holder.bookProgressView.setOnProgressChangeListener {
            holder.handleProgressChanged(it)
        }
        holder.updateProgressButton.setOnClickListener {
            holder.notifyUpdateProgress()
        }
        holder.readPercentageEditTextView.onAfterUserTextChangeListener = {
            val progress = it?.toIntOrNull() ?: 0
            holder.bookProgressView.progress =
                percentToPage(progress, holder.bookWithProgress.book.numPages)
        }
        holder.readPercentageEditTextView.filters = arrayOf(RangeInputFilter(0..100))
        holder.readPercentageEditTextView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                holder.notifyUpdateProgress()
                return@setOnEditorActionListener true
            }
            false
        }
        holder.bookContainer.setOnClickListener {
            holder.readPercentageEditTextView.requestFocus()
            holder.readPercentageEditTextView.showKeyboard()
        }
    }

    private fun ContainerViewHolder.notifyUpdateProgress() {
        val progress = bookProgressView.progress
        onUpdateProgress(bookWithProgress, progress)
    }

    override fun onBindViewHolder(holder: ContainerViewHolder, item: BookWithProgress) {
        super.onBindViewHolder(holder, item)

        holder.bindBookInformation(item)
        holder.bindImage(item)
        holder.bindProgressView(item)

        holder.updateProgressButton.visibility = View.GONE
        holder.updateProgressButton.alpha = 0f
    }

    private fun ContainerViewHolder.bindBookInformation(item: BookWithProgress) {
        bookTitleView.text = item.book.title
        bookAuthorView.text = itemView.context.getString(R.string.info_authors, item.book.authors)
    }

    private fun ContainerViewHolder.bindImage(item: BookWithProgress) {
        if (item.book.imageUrl != null) {
            Picasso.get()
                .load(item.book.imageUrl!!)
                .placeholder(ColorDrawable(Color.BLACK))
                .into(bookCoverImageView)
        } else {
            bookCoverImageView.setImageDrawable(ColorDrawable(Color.BLACK))
        }
    }

    private fun ContainerViewHolder.bindProgressView(item: BookWithProgress) {
        with(bookProgressView) {
            maxProgress =
                if (item.book.numPages > 0) item.book.numPages else 100 // TODO: Handle this in a better way?
            progress = item.page
            bindProgress(item, progress)
        }
    }

    private fun ContainerViewHolder.bindProgress(progressItem: BookWithProgress, page: Int) {
        val numPages = progressItem.book.numPages

        val percent: Int
        if (numPages == 0) {
            // TODO: Add test that covers this case (Book to reproduce is: The Winds of Winter)
            percent = 0
            pagesTextView.text = "Unknown number of pages"
        } else {
            percent = pageToPercent(page, numPages)
            pagesTextView.text =
                itemView.context.getString(R.string.info_book_pages_progress, page, numPages)
        }

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

    private fun ContainerViewHolder.handleProgressChanged(progress: Int) {
        bindProgress(bookWithProgress, progress)
        animateShowSaveButton()
    }

    private fun ContainerViewHolder.animateShowSaveButton() = with(updateProgressButton) {
        if (visibility != View.GONE) return
        alpha = 0f
        visibility = View.VISIBLE
        animate().alpha(1f).setDuration(1000).start()
    }
}
