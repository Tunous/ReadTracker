package me.thanel.goodreadsapi.internal

import kotlinx.coroutines.Deferred
import me.thanel.goodreadsapi.model.UserResponse
import retrofit2.http.GET
import retrofit2.http.Path

internal interface GoodreadsService {
    @GET("/user/show/{id}.xml?key=KUtQGqdhmKy1nUyQnFZRzA")
    fun getUser(@Path("id") id: Long): Deferred<UserResponse>
}
