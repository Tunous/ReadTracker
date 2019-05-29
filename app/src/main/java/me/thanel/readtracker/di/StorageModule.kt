package me.thanel.readtracker.di

import android.app.Application
import dagger.Module
import dagger.Provides
import me.thanel.readtracker.database.BookQueries
import me.thanel.readtracker.database.Database
import me.thanel.readtracker.database.DatabaseFactory
import me.thanel.readtracker.database.ReadProgressQueries
import javax.inject.Singleton

@Module
class StorageModule {
    @Provides
    @Singleton
    fun provideDatabase(appContext: Application): Database {
        return DatabaseFactory.createDatabase(appContext, "readTracker.db")
    }

    @Provides
    fun provideReadProgressQueries(database: Database): ReadProgressQueries {
        return database.readProgressQueries
    }

    @Provides
    fun provideBookQueries(database: Database): BookQueries {
        return database.bookQueries
    }
}
