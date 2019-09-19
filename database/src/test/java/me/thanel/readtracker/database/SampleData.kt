package me.thanel.readtracker.database

object SampleData {
    val bookBeingRead: Book = Book.Impl(
        id = 6L,
        title = "Harry Potter and the Goblet of Fire (Harry Potter, #4)",
        numPages = 734,
        imageUrl = "https://images.gr-assets.com/books/1554006152m/6.jpg",
        authors = "J.K. Rowling",
        position = 1
    )

    val bookToRead: Book = Book.Impl(
        id = 5L,
        title = "Harry Potter and the Prisoner of Azkaban (Harry Potter, #3)",
        numPages = 435,
        imageUrl = null,
        authors = "J.K. Rowling",
        position = 2
    )

    val progressForBookBeingRead: ReadProgress = ReadProgress.Impl(
        id = 12L,
        bookId = bookBeingRead.id,
        page = 203,
        reviewId = 5L
    )

    val progressForBookToRead: ReadProgress = ReadProgress.Impl(
        id = 83L,
        bookId = bookToRead.id,
        page = 0,
        reviewId = 22L
    )

    fun generateBooks(startNumber: Int = 1, count: Int = 10): List<Book> {
        val endNumber = startNumber + count
        return (startNumber until endNumber).map { number ->
            Book.Impl(
                id = number.toLong(),
                title = "Book #$number",
                numPages = number * 100,
                imageUrl = null,
                authors = "Author #$number",
                position = number
            )
        }
    }

    fun generateReadProgress(count: Int = 10): List<ReadProgress> {
        return (1..count).map { number ->
            ReadProgress.Impl(
                id = number.toLong(),
                bookId = number.toLong(),
                page = number * 50,
                reviewId = number.toLong()
            )
        }
    }
}
