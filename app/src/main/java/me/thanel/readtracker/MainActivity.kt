package me.thanel.readtracker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.item_book_card.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import me.thanel.readtracker.api.GoodreadsApi

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launch(UI) {
            val response = withContext(CommonPool) {
                GoodreadsApi.service.listReviews(77741768, "currently-reading").await()
            }
            val reviews = response.reviews.reviews

            val review = reviews?.firstOrNull()
            if (review != null) {
                bookTitleView.text = review.book.title
                bookAuthorView.text = review.book.authors.joinToString(prefix = "by ") { it.name }
            }
        }
    }
}
