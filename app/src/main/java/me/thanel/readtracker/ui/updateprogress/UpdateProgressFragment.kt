package me.thanel.readtracker.ui.updateprogress

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.squareup.picasso.Picasso
import com.squareup.sqldelight.Query
import kotlinx.android.synthetic.main.item_book_card.*
import kotlinx.android.synthetic.main.update_progress.*
import kotlinx.android.synthetic.main.update_progress_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.thanel.readtracker.R
import me.thanel.readtracker.ReadProgressQueries
import me.thanel.readtracker.SelectWithBookInformation
import me.thanel.readtracker.di.ReadTracker
import me.thanel.readtracker.ui.base.BaseFragment
import me.thanel.readtracker.ui.util.afterTextChanged
import me.thanel.readtracker.ui.util.onProgressChanged
import me.thanel.readtracker.ui.util.toIntOrElse
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt

class UpdateProgressFragment : BaseFragment(R.layout.update_progress_fragment) {
    private lateinit var viewModel: UpdateProgressViewModel

    private var numPages = 0
    private var bookId = 0L
    private var reviewId = 0L

    private val commentBody get() = userStatusCommentTextInputEditText.text?.toString()

    @Inject
    internal lateinit var readProgressQueries: ReadProgressQueries

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReadTracker.dependencyInjector.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && progressInput.text.toIntOrElse { 0 } > progressSeekBar.max) {
                progressInput.setText(progressSeekBar.max.toString())
            }
        }

        setupSeekBarProgressListener()

        userStatusCommentTextInputEditText.afterTextChanged {
            val inputLength = it?.length ?: 0
            updateProgressButton.isEnabled =
                inputLength <= userStatusCommentTextInputLayout.counterMaxLength
        }

        updateProgressButton.setOnClickListener {
            onUpdateProgressButtonClicked()
        }

        progressTypeSegmentedGroup.setOnCheckedChangeListener { _, checkedId ->
            setupProgressTypeButton(checkedId == R.id.pageProgressTypeButton)
        }

        increaseProgressButton.setOnClickListener {
            progressSeekBar.progress += 1
        }
        decreaseProgressButton.setOnClickListener {
            progressSeekBar.progress -= 1
        }

        exploreBooksButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.goodreads.com/book")))
        }
    }

    private fun setupSeekBarProgressListener() {
        var skipChange = false
        progressSeekBar.onProgressChanged {
            if (!skipChange) {
                progressInput.setText(it.toString())
            }
            val isMax = it == progressSeekBar.max
            val isMin = it == 0
            updateProgressButton.setText(if (isMax) R.string.action_finished else R.string.action_update_progress)
            increaseProgressButton.isEnabled = !isMax
            decreaseProgressButton.isEnabled = !isMin
            ratingBar.visibility = if (isMax) View.VISIBLE else View.GONE
        }

        progressInput.afterTextChanged {
            skipChange = true
            progressSeekBar.progress = it.toIntOrElse { 0 }
            skipChange = false
        }
    }

    private fun setupProgressTypeButton(usePages: Boolean) {
        val progress = progressSeekBar.progress / progressSeekBar.max.toFloat()
        if (usePages) {
            progressTypeSegmentedGroup.check(R.id.pageProgressTypeButton)
            progressSeekBar.max = numPages
        } else {
            progressTypeSegmentedGroup.check(R.id.percentProgressTypeButton)
            progressSeekBar.max = 100
        }

        val newProgress = (progress * progressSeekBar.max).roundToInt()
        progressInput.setText(newProgress.toString())
    }

    private fun updateProgress(progress: Int, usePages: Boolean) {
        setupProgressTypeButton(usePages)
        progressSeekBar.progress = progress
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(UpdateProgressViewModel::class.java)

        readProgressQueries.selectWithBookInformation()
            .executeAsListLiveData()
            .observe(this, Observer(::bindProgress))

        launch(Dispatchers.IO) {
            viewModel.synchronizeDatabase()
        }
    }

    private fun bindProgress(data: List<SelectWithBookInformation>) {
        val progress = data.firstOrNull()
        if (progress == null) {
            showNoDataInformation()
            return
        }

        bookId = progress.bookId
        numPages = progress.numPages
        reviewId = progress.reviewId

        // when {
        //     progress.page > 0 -> updateProgress(progress.page, usePages = true)
        //     progress.percent > 0 -> updateProgress(progress.percent, usePages = false)
        //     else -> throw IllegalStateException("Received progress without valid information")
        // }

        bookTitleView.text = progress.bookTitle
        pagesTextView.text = getString(R.string.info_pages, progress.numPages)
        bookAuthorView.text = getString(R.string.info_authors, progress.bookAuthors)

        if (progress.bookImageUrl != null) {
            Picasso.get()
                .load(progress.bookImageUrl!!)
                .placeholder(ColorDrawable(Color.BLACK))
                .into(bookCoverImageView)
        } else {
            bookCoverImageView.setImageDrawable(ColorDrawable(Color.BLACK))
        }

        bookCard.visibility = View.VISIBLE
        updateProgressContent.visibility = View.VISIBLE
        updateProgressContentProgressBar.visibility = View.GONE
        noInformationContainer.visibility = View.GONE
    }

    private fun showNoDataInformation() {
        noInformationContainer.visibility = View.VISIBLE
        updateProgressContentProgressBar.visibility = View.GONE
    }

    private fun onUpdateProgressButtonClicked() {
        if (progressSeekBar.progress == progressSeekBar.max) {
            finishReading()
            return
        }
        if (progressTypeSegmentedGroup.checkedRadioButtonId == R.id.pageProgressTypeButton) {
            updateProgressByPageNumber()
        } else {
            updateProgressByPercent()
        }
    }

    private fun finishReading() = launch {
        viewModel.finishReading(reviewId, ratingBar.progress, commentBody)
        onProgressUpdated()
        activity?.finish()
    }

    private fun updateProgressByPercent() = launch {
        viewModel.updateProgressByPercent(bookId, progressSeekBar.progress, commentBody)
        onProgressUpdated()
    }

    private fun updateProgressByPageNumber() = launch {
        viewModel.updateProgressByPageNumber(bookId, progressSeekBar.progress, commentBody)
        onProgressUpdated()
    }

    private fun onProgressUpdated() {
        Toast.makeText(requireContext(), "Updated progress", Toast.LENGTH_SHORT).show()
        userStatusCommentTextInputEditText.text?.clear()
    }

    companion object {
        fun newInstance() = UpdateProgressFragment()
    }
}

fun <RowType : Any> Query<RowType>.executeAsListLiveData(): LiveData<List<RowType>> {
    return object : LiveData<List<RowType>>(), Query.Listener, CoroutineScope {
        private lateinit var job: Job

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + job

        override fun queryResultsChanged() {
            fetchData()
        }

        override fun onActive() {
            job = Job()
            addListener(this)
            fetchData()
        }

        override fun onInactive() {
            removeListener(this)
            job.cancel()
        }

        private fun fetchData() = launch {
            postValue(executeAsList())
        }
    }
}
