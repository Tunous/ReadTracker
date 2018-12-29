package me.thanel.goodreadsapi.internal.util

fun String?.nullIfBlank(): String? = if (isNullOrBlank()) null else this
