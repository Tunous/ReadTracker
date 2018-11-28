package me.thanel.goodreadsapi.model

data class RequestTokenData(
    val authUrl: String,
    val token: String,
    val tokenSecret: String
)