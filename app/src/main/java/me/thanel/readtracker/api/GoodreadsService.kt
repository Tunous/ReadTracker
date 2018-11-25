package me.thanel.readtracker.api

import kotlinx.coroutines.Deferred
import me.thanel.readtracker.api.model.ReviewListResponse
import me.thanel.readtracker.api.model.UserResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GoodreadsService {
    @GET("/user/show/{id}.xml?key=KUtQGqdhmKy1nUyQnFZRzA")
    fun getUser(@Path("id") id: Int): Deferred<UserResponse>

    @GET("/review/list?v=2&key=KUtQGqdhmKy1nUyQnFZRzA")
    fun getMemberBooksFromShelf(
        @Query("id") id: Int,
        @Query("shelf") shelf: String? = null
        // TODO: More parameters
    ): Deferred<ReviewListResponse>
}
