package me.thanel.readtracker.database

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver

object DatabaseFactory {

    fun createDatabase(context: Context, name: String? = null): Database {
        val driver = AndroidSqliteDriver(Database.Schema, context, name)
        return Database(driver)
    }
}
