package me.thanel.readtracker.ui.readinglist

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_reading_list.*
import kotlinx.coroutines.launch
import me.thanel.readtracker.Book
import me.thanel.readtracker.R
import me.thanel.readtracker.SelectWithBookInformation
import me.thanel.readtracker.di.ReadTracker
import me.thanel.readtracker.ui.base.BaseFragment
import me.thanel.readtracker.ui.review.ReviewDialog
import me.thanel.readtracker.ui.updateprogress.UpdateProgressViewModel
import me.thanel.recyclerviewutils.adapter.lazyAdapterWrapper

class ReadingListFragment : BaseFragment(R.layout.fragment_reading_list) {

    private lateinit var viewModel: UpdateProgressViewModel

    private val adapterWrapper by lazyAdapterWrapper {
        register(ProgressBookViewBinder(::onUpdateBookProgress), object: DiffUtil.ItemCallback<SelectWithBookInformation>() {
            override fun areItemsTheSame(
                oldItem: SelectWithBookInformation,
                newItem: SelectWithBookInformation
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: SelectWithBookInformation,
                newItem: SelectWithBookInformation
            ): Boolean {
                return oldItem as SelectWithBookInformation.Impl == newItem as SelectWithBookInformation.Impl
            }
        })
        register(ToReadBookViewBinder(), object: DiffUtil.ItemCallback<Book>() {
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem as Book.Impl == newItem as Book.Impl
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
        viewModel = ViewModelProviders.of(this).get(UpdateProgressViewModel::class.java)
        viewModel.getReadingStatusLiveData().observe(this, Observer(::fillProgressBooks))
        viewModel.getBooksToReadLiveData().observe(this, Observer(::fillBooksToRead))
        launch {
            viewModel.synchronizeDatabase()
        }
    }

    private fun onUpdateBookProgress(progressItem: SelectWithBookInformation, progress: Int) {
        val hasFinishedReading = progress == progressItem.numPages
        val dialog = if (hasFinishedReading) {
            ReviewDialog.createForFinished(progressItem.reviewId)
        } else {
            ReviewDialog.createForInProgress(progressItem.bookId, progress)
        }
        dialog.show(fragmentManager, "reviewDialog")
    }

    private fun fillProgressBooks(data: List<SelectWithBookInformation>?) {
        progressBooks = data ?: emptyList()
    }

    private var progressBooks = listOf<SelectWithBookInformation>()
    private var futureBooks = listOf<Book>()

    private fun fillBooksToRead(books: List<Book>?) {
        futureBooks = books ?: emptyList()
        fillBooks()
    }

    private fun fillBooks() = launch {
        val items = mutableListOf<Any>()
        items.addAll(progressBooks)
        items.addAll(futureBooks)
        adapterWrapper.updateItems(items)
    }

    companion object {
        fun newInstance() = ReadingListFragment()
    }
}
