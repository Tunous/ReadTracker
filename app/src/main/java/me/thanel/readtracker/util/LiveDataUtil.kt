package me.thanel.readtracker.util

import androidx.lifecycle.LiveData
import com.squareup.sqldelight.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

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
