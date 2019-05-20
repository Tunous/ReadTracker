package me.thanel.readtracker.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_reading_list.*
import kotlinx.android.synthetic.main.item_book_card.view.bookAuthorView
import kotlinx.android.synthetic.main.item_book_card.view.bookCoverImageView
import kotlinx.android.synthetic.main.item_book_card.view.bookTitleView
import kotlinx.android.synthetic.main.item_currently_reading_book.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.thanel.readtracker.R
import me.thanel.readtracker.ReadProgressQueries
import me.thanel.readtracker.SelectWithBookInformation
import me.thanel.readtracker.di.ReadTracker
import me.thanel.readtracker.ui.base.BaseFragment
import me.thanel.readtracker.ui.updateprogress.UpdateProgressViewModel
import me.thanel.readtracker.ui.updateprogress.executeAsListLiveData
import javax.inject.Inject
import kotlin.math.roundToInt

class ReadingListFragment : BaseFragment(R.layout.fragment_reading_list) {

    companion object {
        fun newInstance() = ReadingListFragment()
    }

    @Inject
    internal lateinit var readProgressQueries: ReadProgressQueries

    private lateinit var viewModel: UpdateProgressViewModel

    private val adapter = BookAdapter(::onUpdateBookProgress)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReadTracker.dependencyInjector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        readingRecyclerView.adapter = adapter
        readingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun onUpdateBookProgress(progressItem: SelectWithBookInformation, progress: Int) {
        launch {
            when {
                progressItem.page > 0 -> {
                    viewModel.updateProgressByPageNumber(progressItem.bookId, progress, body = null)
                }
                progressItem.percent > 0 -> {
                    viewModel.updateProgressByPercent(progressItem.bookId, progress, body = null)
                }
                else -> {
                    Toast.makeText(requireContext(), "Couldn't update progress", Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }

            Toast.makeText(requireContext(), "Updated progress", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(UpdateProgressViewModel::class.java)

        readProgressQueries.selectWithBookInformation()
            .executeAsListLiveData()
            .observe(this, Observer(::fillBooks))

        launch(Dispatchers.IO) {
            viewModel.fetchReadProgress()
        }
    }

    private fun fillBooks(data: List<SelectWithBookInformation>?) {
        adapter.items.clear()
        if (data != null) {
            adapter.items.addAll(data)
        }
        adapter.notifyDataSetChanged()
    }
}

class BookViewHolder(
    itemView: View,
    onUpdateProgressCallback: (SelectWithBookInformation, Int) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    init {
        itemView.bookProgressView.onProgressChangeListener = {
            val progressItem = itemView.tag as SelectWithBookInformation
            val page: Int
            val percent: Int
            if (progressItem.page > 0) {
                percent = ((it / progressItem.numPages.toFloat()) * 100).roundToInt()
                page = it
            } else {
                percent = it
                page = ((it / 100f) * progressItem.numPages).roundToInt()
            }
            itemView.bookProgressIndicatorView.text = "$page/${progressItem.numPages} pages ($percent%)"

            with(itemView.updateBookProgressButton) {
                if (visibility == View.GONE) {
                    alpha = 0f
                    visibility = View.VISIBLE
                    animate().alpha(1f).setDuration(1000).start()
                }
            }
        }
        itemView.updateBookProgressButton.setOnClickListener {
            val progressItem = itemView.tag as SelectWithBookInformation
            val progress = itemView.bookProgressView.currentValue
            onUpdateProgressCallback(progressItem, progress)
        }
    }
}

class BookAdapter(
    private val onUpdateProgressCallback: (SelectWithBookInformation, Int) -> Unit
) : RecyclerView.Adapter<BookViewHolder>() {

    val items = mutableListOf<SelectWithBookInformation>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_currently_reading_book, parent, false)
        return BookViewHolder(view, onUpdateProgressCallback)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val progressItem = items[position]

        holder.itemView.tag = progressItem

        holder.itemView.bookTitleView.text = progressItem.bookTitle
        holder.itemView.bookAuthorView.text = holder.itemView.context.getString(R.string.info_authors, progressItem.bookAuthors)
        if (progressItem.bookImageUrl != null) {
            Picasso.get()
                .load(progressItem.bookImageUrl!!)
                .placeholder(ColorDrawable(Color.BLACK))
                .into(holder.itemView.bookCoverImageView)
        } else {
            holder.itemView.bookCoverImageView.setImageDrawable(ColorDrawable(Color.BLACK))
        }

        with(holder.itemView.bookProgressView) {
            when {
                progressItem.page > 0 -> {
                    maxValue = progressItem.numPages
                    currentValue = progressItem.page
                }
                progressItem.percent > 0 -> {
                    maxValue = 100
                    currentValue = progressItem.percent
                }
                else -> throw IllegalStateException("Received progress without valid information")
            }
        }

        holder.itemView.bookProgressIndicatorView.text = "${progressItem.page}/${progressItem.numPages} (${progressItem.percent}%)"
        holder.itemView.updateBookProgressButton.visibility = View.GONE
        holder.itemView.updateBookProgressButton.alpha = 0f
    }
}
