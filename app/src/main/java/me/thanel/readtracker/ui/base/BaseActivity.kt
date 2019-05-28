package me.thanel.readtracker.ui.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class BaseActivity(@LayoutRes private val layoutResId: Int) : AppCompatActivity(),
    CoroutineScope {

    private lateinit var uiJob: Job

    override val coroutineContext: CoroutineContext
        get() = uiJob + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutResId)
        uiJob = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        uiJob.cancel()
    }
}
