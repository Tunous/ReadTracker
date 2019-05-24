package me.thanel.readtracker.di

import dagger.Module
import dagger.Provides
import me.thanel.goodreadsapi.GoodreadsApi
import me.thanel.goodreadsapi.GoodreadsApiInterface
import me.thanel.readtracker.BuildConfig
import me.thanel.readtracker.Preferences
import javax.inject.Singleton

@Module
class ApiModule {
    @Provides
    @Singleton
    fun provideGoodreadsApi(): GoodreadsApiInterface {
        return GoodreadsApi(
            consumerKey = BuildConfig.GOODREADS_CONSUMER_KEY,
            consumerSecret = BuildConfig.GOODREADS_CONSUMER_SECRET,
            token = Preferences.token!!,
            tokenSecret = Preferences.tokenSecret!!
        )
    }
}
