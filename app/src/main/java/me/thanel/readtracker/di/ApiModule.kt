package me.thanel.readtracker.di

import dagger.Module
import dagger.Provides
import me.thanel.goodreadsapi.GoodreadsApi
import me.thanel.goodreadsapi.GoodreadsSecrets
import me.thanel.readtracker.BuildConfig
import me.thanel.readtracker.Preferences
import javax.inject.Singleton

@Module
class ApiModule {
    @Provides
    @Singleton
    fun provideGoodreadsApi(): GoodreadsApi {
        val secrets = GoodreadsSecrets(
            consumerKey = BuildConfig.GOODREADS_CONSUMER_KEY,
            consumerSecret = BuildConfig.GOODREADS_CONSUMER_SECRET,
            token = Preferences.token!!,
            tokenSecret = Preferences.tokenSecret!!
        )
        return GoodreadsApi.create(secrets)
    }
}
