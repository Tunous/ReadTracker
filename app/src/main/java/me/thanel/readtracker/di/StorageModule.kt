package me.thanel.readtracker.di

import android.app.Application
import com.squareup.sqldelight.android.AndroidSqliteDriver
import dagger.Module
import dagger.Provides
import me.thanel.readtracker.BookQueries
import me.thanel.readtracker.Database
import me.thanel.readtracker.ReadProgressQueries
import javax.inject.Singleton

@Module
class StorageModule {
    @Provides
    @Singleton
    fun provideDatabase(appContext: Application): Database {
        return Database(AndroidSqliteDriver(Database.Schema, appContext, "readTracker.db"))
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
