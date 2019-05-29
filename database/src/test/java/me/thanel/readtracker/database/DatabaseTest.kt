package me.thanel.readtracker.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before

abstract class DatabaseTest {
    protected lateinit var database: Database

    @Before
    fun setupBase() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database = DatabaseFactory.createDatabase(context, name = null)
    }
}
