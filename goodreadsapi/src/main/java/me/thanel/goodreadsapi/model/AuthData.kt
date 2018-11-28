package me.thanel.goodreadsapi.model

data class AuthData(
    val authUrl: String,
    val token: String,
    val tokenSecret: String
)