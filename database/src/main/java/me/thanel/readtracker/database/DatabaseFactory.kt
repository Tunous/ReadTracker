package me.thanel.readtracker.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver

object DatabaseFactory {

    fun createDatabase(context: Context, name: String? = null): Database {
        val driver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = context,
            name = name,
            callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
                override fun onConfigure(db: SupportSQLiteDatabase) {
                    super.onConfigure(db)
                    db.setForeignKeyConstraintsEnabled(true)
                }
            }
        )
        return Database(driver)
    }
}
