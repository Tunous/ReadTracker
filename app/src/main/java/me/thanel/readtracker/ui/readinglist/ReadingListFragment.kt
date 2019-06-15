package me.thanel.readtracker.ui.readinglist

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_reading_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.thanel.readtracker.R
import me.thanel.readtracker.di.ReadTracker
import me.thanel.readtracker.model.BookWithProgress
import me.thanel.readtracker.ui.base.BaseFragment
import me.thanel.readtracker.ui.review.ReviewDialog
import me.thanel.readtracker.ui.util.viewModel
import me.thanel.recyclerviewutils.adapter.lazyAdapterWrapper

class ReadingListFragment : BaseFragment(R.layout.fragment_reading_list) {

    private val viewModel: ReadingListViewModel by viewModel()

    private var progressBooks = listOf<BookWithProgress>()
    private var futureBooks = listOf<BookWithProgress>()

    private val adapterWrapper by lazyAdapterWrapper {
        register(
            BookViewBinder(::onUpdateBookProgress),
            object : DiffUtil.ItemCallback<BookWithProgress>() {
                override fun areItemsTheSame(
                    oldItem: BookWithProgress,
                    newItem: BookWithProgress
                ) = oldItem.book.id == newItem.book.id

                override fun areContentsTheSame(
                    oldItem: BookWithProgress,
                    newItem: BookWithProgress
                ) = oldItem == newItem
            })

        register(SectionHeaderViewBinder(), object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return true
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReadTracker.dependencyInjector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        readingRecyclerView.adapter = adapterWrapper.adapter
        readingRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.readingStatusLiveData.observe(this, Observer(::fillProgressBooks))
        viewModel.booksToReadLiveData.observe(this, Observer(::fillBooksToRead))
        launch {
            // TODO: Extract to work task
            // TODO: Make possible to request manually
            viewModel.synchronizeDatabase()
        }
    }

    private fun onUpdateBookProgress(bookWithProgress: BookWithProgress, progress: Int) {
        val hasFinishedReading = progress == bookWithProgress.book.numPages
        val dialog = if (hasFinishedReading) {
            ReviewDialog.createForFinished(bookWithProgress.book.id, bookWithProgress.reviewId)
        } else {
            ReviewDialog.createForInProgress(bookWithProgress.book.id, progress)
        }
        dialog.show(fragmentManager, "reviewDialog")
    }

    private fun fillProgressBooks(books: List<BookWithProgress>?) {
        progressBooks = books ?: emptyList()
        fillBooks()
    }

    private fun fillBooksToRead(books: List<BookWithProgress>?) {
        futureBooks = books ?: emptyList()
        fillBooks()
    }

    private fun fillBooks() = launch(Dispatchers.Default) {
        val items = mutableListOf<Any>()
        if (progressBooks.isNotEmpty()) {
            items.add("Currently reading")
            items.addAll(progressBooks)
        }
        items.add("To read")
        items.addAll(futureBooks)
        adapterWrapper.updateItems(items)
    }

    companion object {
        fun newInstance() = ReadingListFragment()
    }
}
