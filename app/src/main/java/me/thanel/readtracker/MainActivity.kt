package me.thanel.readtracker

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_book_card.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import me.thanel.readtracker.api.GoodreadsApi
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    var numPages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressInput.filters = arrayOf(RangeInputFilter(0, 100))
        mergeSeekBarAndInput(progressSeekBar, progressInput)
        progressTypeButton.setOnClickListener {
            val progress = progressSeekBar.progress / progressSeekBar.max.toFloat()
            if (progressTypeButton.text.startsWith("%")) {
                progressTypeButton.text = "of $numPages #"
                progressSeekBar.max = numPages
            } else {
                progressTypeButton.text = "% done"
                progressSeekBar.max = 100
            }
            progressInput.filters = arrayOf(RangeInputFilter(0, progressSeekBar.max))
            progressInput.setText((progress * progressSeekBar.max).toInt().toString())
        }

        launch(UI) {
            val response = withContext(CommonPool) {
                GoodreadsApi.service.listReviews(77741768, "currently-reading").await()
            }
            val reviews = response.reviews.reviews

            val review = reviews?.firstOrNull()
            if (review != null) {
                bookTitleView.text = review.book.title
                bookAuthorView.text = review.book.authors.joinToString(prefix = "by ") { it.name }
                ratingView.text = review.book.averageRating.toString()
                val formatter = DecimalFormat("#,###")
                totalRatingsView.text = formatter.format(review.book.ratingsCount) + " ratings"
                numPages = review.book.numPages
            }
        }
    }

    private fun mergeSeekBarAndInput(seekBar: SeekBar, editText: EditText) {
        var skipChange = false
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (!skipChange) {
                    editText.setText(progress.toString())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                skipChange = true
                seekBar.progress = s?.toString()?.toIntOrNull() ?: 0
                skipChange = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })
    }
}
