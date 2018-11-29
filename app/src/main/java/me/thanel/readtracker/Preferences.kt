package me.thanel.readtracker

import com.chibatching.kotpref.KotprefModel

object Preferences : KotprefModel() {
    var token by nullableStringPref()
    var tokenSecret by nullableStringPref()
    var isAuthorized by booleanPref()

    private var authUserId by longPref()
    var userId: Long?
        get() = if (authUserId != 0L) authUserId else null
        set(newValue) {
            authUserId = newValue ?: 0L
        }
}