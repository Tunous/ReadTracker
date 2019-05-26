package me.thanel.goodreadsapi.internal.util

import java.net.URLDecoder

// TODO: Shouldn't be public
fun String?.nullIfBlank(): String? = if (isNullOrBlank()) null else this

internal fun String.urlDecode(): String = URLDecoder.decode(this, Charsets.UTF_8.name())
