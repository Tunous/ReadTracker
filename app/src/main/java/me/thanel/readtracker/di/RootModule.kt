package me.thanel.readtracker.di

import android.app.Application
import dagger.Module
import dagger.Provides

@Module
class RootModule(
    private val appContext: Application
) {

    @Provides
    fun provideAppContext(): Application = appContext
}
