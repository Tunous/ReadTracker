package me.thanel.readtracker.ui.readinglist

import androidx.lifecycle.ViewModel
import me.thanel.readtracker.api.ReadingProgressRepository
import me.thanel.readtracker.di.ReadTracker
import javax.inject.Inject

class ReadingListViewModel : ViewModel() {

    @Inject
    lateinit var readingProgressRepository: ReadingProgressRepository

    init {
        ReadTracker.dependencyInjector.inject(this)
    }

    val readingStatusLiveData = readingProgressRepository.getBooksWithProgressAsLiveData()

    val booksToReadLiveData = readingProgressRepository.getBooksToReadAsLiveData()
}
