package me.thanel.readtracker.ui.util

import android.os.Bundle
import androidx.fragment.app.Fragment

fun <T : Fragment> T.withArguments(argumentsInitializer: Bundle.() -> Unit): T = apply {
    arguments = Bundle().also(argumentsInitializer)
}

fun Fragment.requireArguments() = checkNotNull(arguments) { "Missing arguments" }
