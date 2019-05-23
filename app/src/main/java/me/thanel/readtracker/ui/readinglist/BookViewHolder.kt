package me.thanel.readtracker.ui.readinglist

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.inputmethod.EditorInfo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_currently_reading_book.*
import me.thanel.goodreadsapi.internal.util.nullIfBlank
import me.thanel.readtracker.Book
import me.thanel.readtracker.R
import me.thanel.readtracker.SelectWithBookInformation
import me.thanel.readtracker.model.actualPage
import me.thanel.readtracker.ui.util.RangeInputFilter
import me.thanel.recyclerviewutils.viewholder.BaseItemViewBinder
import me.thanel.recyclerviewutils.viewholder.ContainerViewHolder
import kotlin.math.roundToInt

class ToReadBookViewBinder : BaseItemViewBinder<Book, ContainerViewHolder>(R.layout.item_currently_reading_book) {
    override fun onCreateViewHolder(itemView: View): ContainerViewHolder {
        return ContainerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContainerViewHolder, item: Book) {
        super.onBindViewHolder(holder, item)
        holder.bookTitleView.text = item.title
        holder.bookAuthorView.text = holder.context.getString(R.string.info_authors, item.authors)
        if (item.imageUrl != null) {
            Picasso.get()
                .load(item.imageUrl!!)
                .placeholder(ColorDrawable(Color.BLACK))
                .into(holder.bookCoverImageView)
        } else {
            holder.bookCoverImageView.setImageDrawable(ColorDrawable(Color.BLACK))
        }
    }
}

class ProgressBookViewBinder(
    private val onUpdateProgress: (SelectWithBookInformation, Int) -> Unit
) : BaseItemViewBinder<SelectWithBookInformation, ContainerViewHolder>(R.layout.item_currently_reading_book) {
    override fun onCreateViewHolder(itemView: View): ContainerViewHolder {
        return ContainerViewHolder(itemView).also {
            initViewHolder(it)
        }
    }

    private val ContainerViewHolder.progressItem: SelectWithBookInformation
        get() = itemView.tag as SelectWithBookInformation

    private fun initViewHolder(holder: ContainerViewHolder) {
        holder.bookProgressView.onProgressChangeListener = {
            holder.handleProgressChanged(it)
        }
        holder.updateProgressButton.setOnClickListener {
            holder.notifyUpdateProgress()
        }
        holder.readPercentageEditTextView.afterUserTextChanged = {
            val progress = it?.toIntOrNull() ?: 0
            holder.bookProgressView.currentValue = percentToPage(progress, holder.progressItem.numPages)
        }
        holder.readPercentageEditTextView.filters = arrayOf(RangeInputFilter(0..100))
        holder.readPercentageEditTextView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                holder.notifyUpdateProgress()
                return@setOnEditorActionListener true
            }
            false
        }
        holder.readPercentageTextView.setOnClickListener {
            holder.readPercentageEditTextView.requestFocus()
        }
    }

    private fun ContainerViewHolder.notifyUpdateProgress() {
        val progress = bookProgressView.currentValue
        onUpdateProgress(progressItem, progress)
    }

    override fun onBindViewHolder(holder: ContainerViewHolder, item: SelectWithBookInformation) {
        super.onBindViewHolder(holder, item)
        holder.itemView.tag = item

        holder.bindBookInformation(item)
        holder.bindImage(item)
        holder.bindProgressView(item)

        holder.updateProgressButton.visibility = View.GONE
        holder.updateProgressButton.alpha = 0f
    }

    private fun ContainerViewHolder.bindBookInformation(item: SelectWithBookInformation) {
        bookTitleView.text = item.bookTitle
        bookAuthorView.text = itemView.context.getString(R.string.info_authors, item.bookAuthors)
    }

    private fun ContainerViewHolder.bindImage(item: SelectWithBookInformation) {
        if (item.bookImageUrl != null) {
            Picasso.get()
                .load(item.bookImageUrl!!)
                .placeholder(ColorDrawable(Color.BLACK))
                .into(bookCoverImageView)
        } else {
            bookCoverImageView.setImageDrawable(ColorDrawable(Color.BLACK))
        }
    }

    private fun ContainerViewHolder.bindProgressView(item: SelectWithBookInformation) {
        with(bookProgressView) {
            maxValue = item.numPages
            currentValue = item.actualPage
            bindProgress(item, currentValue)
        }
    }

    private fun ContainerViewHolder.bindProgress(progressItem: SelectWithBookInformation, page: Int) {
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

    private fun ContainerViewHolder.handleProgressChanged(progress: Int) {
        bindProgress(progressItem, progress)
        animateShowSaveButton()
    }

    private fun ContainerViewHolder.animateShowSaveButton() = with(updateProgressButton) {
        if (visibility != View.GONE) return
        alpha = 0f
        visibility = View.VISIBLE
        animate().alpha(1f).setDuration(1000).start()
    }
}
