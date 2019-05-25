package me.thanel.goodreadsapi.internal.util

// TODO: Shouldn't be public
fun String?.nullIfBlank(): String? = if (isNullOrBlank()) null else this
