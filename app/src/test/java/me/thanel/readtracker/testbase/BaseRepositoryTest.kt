package me.thanel.readtracker.testbase

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.chibatching.kotpref.Kotpref
import me.thanel.goodreadsapi.GoodreadsApi
import me.thanel.readtracker.Preferences
import me.thanel.readtracker.database.Database
import me.thanel.readtracker.database.DatabaseFactory
import org.junit.Before
import org.mockito.Mockito

abstract class BaseRepositoryTest {
    protected lateinit var database: Database
    protected lateinit var goodreadsApi: GoodreadsApi

    @Before
    fun setupBase() {
        val context: Context = ApplicationProvider.getApplicationContext()
        setupPreferences(context)
        setupDatabase(context)
        setupApi()
    }

    private fun setupPreferences(context: Context) {
        Kotpref.init(context)
        Preferences.clear()
    }

    private fun setupDatabase(context: Context) {
        database = DatabaseFactory.createDatabase(context)
    }

    private fun setupApi() {
        goodreadsApi = Mockito.mock(GoodreadsApi::class.java)
    }
}
