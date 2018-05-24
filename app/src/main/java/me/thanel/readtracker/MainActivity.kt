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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressInput.filters = arrayOf(RangeInputFilter(0, 100))
        mergeSeekBarAndInput(progressSeekBar, progressInput)

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

    private fun mergeSeekBarAndInput(seekBar: SeekBar, editText: EditText) {
        var skipChange = false
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (skipChange) return
                val percentage = ((progress / seekBar.max.toFloat()) * 100).toInt()
                editText.setText(percentage.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                skipChange = true
                val input = s?.toString()?.toIntOrNull() ?: 0
                seekBar.progress = ((input / 100f) * seekBar.max).toInt()
                skipChange = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })
    }
}
