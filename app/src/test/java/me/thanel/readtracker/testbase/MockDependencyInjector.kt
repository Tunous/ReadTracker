package me.thanel.readtracker.testbase

import me.thanel.readtracker.api.ReadingProgressRepository
import me.thanel.readtracker.di.RootComponent
import me.thanel.readtracker.sync.ProgressSynchronizationWorker
import me.thanel.readtracker.ui.readinglist.ReadingListFragment
import me.thanel.readtracker.ui.readinglist.ReadingListViewModel
import me.thanel.readtracker.ui.review.ReviewDialog
import org.mockito.Mockito

class MockDependencyInjector : RootComponent {

    val readingProgressRepositoryMock: ReadingProgressRepository =
        Mockito.mock(ReadingProgressRepository::class.java)

    override fun inject(viewModel: ReadingListViewModel) {
        throw NotImplementedError("not used")
    }

    override fun inject(fragment: ReadingListFragment) {
        throw NotImplementedError("not used")
    }

    override fun inject(reviewDialog: ReviewDialog) {
        throw NotImplementedError("not used")
    }

    override fun inject(progressSynchronizationWorker: ProgressSynchronizationWorker) {
        progressSynchronizationWorker.readingProgressRepository = readingProgressRepositoryMock
    }
}
