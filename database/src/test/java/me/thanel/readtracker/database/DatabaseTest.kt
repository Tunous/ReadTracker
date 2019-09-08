package me.thanel.readtracker.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Before

abstract class DatabaseTest {
    protected lateinit var database: Database

    protected val bookQueries get() = database.bookQueries
    protected val readProgressQueries get() = database.readProgressQueries

    @Before
    fun setupBase() {
        val context: Context = ApplicationProvider.getApplicationContext()
        database = DatabaseFactory.createDatabase(context, name = null)
    }
}

fun BookQueries.insert(books: List<Book>) {
    books.forEach { book ->
        insert(book)
    }
}

fun BookQueries.insert(book: Book) {
    insert(
        id = book.id,
        title = book.title,
        numPages = book.numPages,
        imageUrl = book.imageUrl,
        authors = book.authors,
        position = book.position,
        isCurrentlyReading = book.isCurrentlyReading
    )
}

fun ReadProgressQueries.insert(readProgresses: List<ReadProgress>) {
    readProgresses.forEach { readProgress ->
        insert(readProgress)
    }
}

fun ReadProgressQueries.insert(readProgress: ReadProgress) {
    insert(
        id = readProgress.id,
        bookId = readProgress.bookId,
        page = readProgress.page,
        reviewId = readProgress.reviewId
    )
}
