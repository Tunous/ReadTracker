package me.thanel.readtracker.ui.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

inline fun <reified T : ViewModel> FragmentActivity.viewModel() = lazy {
    ViewModelProviders.of(this).get(T::class.java)
}

inline fun <reified T: ViewModel> Fragment.viewModel() = lazy {
    ViewModelProviders.of(this).get(T::class.java)
}
