package me.thanel.readtracker.di

import dagger.Component
import me.thanel.readtracker.ui.readinglist.ReadingListFragment
import me.thanel.readtracker.ui.readinglist.ReadingListViewModel
import me.thanel.readtracker.ui.review.ReviewDialog
import javax.inject.Singleton

@Component(
    modules = [
        RootModule::class,
        ApiModule::class,
        StorageModule::class
    ]
)
@Singleton
interface RootComponent {
    fun inject(viewModel: ReadingListViewModel)
    fun inject(fragment: ReadingListFragment)
    fun inject(reviewDialog: ReviewDialog)
}
