package me.thanel.readtracker.ui.util

import android.os.Bundle

fun Bundle.getLongOptional(key: String): Long? {
    if (containsKey(key)) {
        return getLong(key)
    }
    return null
}

fun Bundle.putLongOptional(key: String, value: Long?) {
    if (value != null) {
        putLong(key, value)
    } else {
        remove(key)
    }
}
