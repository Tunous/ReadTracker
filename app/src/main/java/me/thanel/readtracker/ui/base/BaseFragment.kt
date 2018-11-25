package me.thanel.readtracker.ui.base


import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class BaseFragment(
    @LayoutRes private val layoutResId: Int
) : Fragment(), CoroutineScope {
    private lateinit var viewJob: Job

    override val coroutineContext: CoroutineContext
        get() = viewJob + Dispatchers.Main

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layoutResId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewJob = Job()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewJob.cancel()
    }
}
