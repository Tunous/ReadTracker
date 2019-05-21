package me.thanel.readtracker.di

import dagger.Component
import me.thanel.readtracker.ui.ReadingListFragment
import me.thanel.readtracker.ui.review.ReviewDialog
import me.thanel.readtracker.ui.updateprogress.UpdateProgressFragment
import me.thanel.readtracker.ui.updateprogress.UpdateProgressViewModel
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
    fun inject(viewModel: UpdateProgressViewModel)
    fun inject(fragment: UpdateProgressFragment)
    fun inject(fragment: ReadingListFragment)
    fun inject(reviewDialog: ReviewDialog)
}
