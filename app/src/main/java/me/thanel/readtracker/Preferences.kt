package me.thanel.readtracker

import com.chibatching.kotpref.KotprefModel

object Preferences : KotprefModel() {
    var token by nullableStringPref()
    var tokenSecret by nullableStringPref()
    var isAuthorized by booleanPref()
}