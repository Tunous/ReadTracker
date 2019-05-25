package me.thanel.goodreadsapi.internal

import kotlinx.coroutines.Deferred
import me.thanel.goodreadsapi.internal.model.ReviewResponse
import me.thanel.goodreadsapi.internal.model.ShortDate
import me.thanel.goodreadsapi.internal.model.UserResponse
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

internal interface GoodreadsService {
    @GET("/user/show/{id}.xml")
    fun getUserAsync(@Path("id") id: Long): Deferred<UserResponse>

    @GET("/api/auth_user")
    fun getUserIdAsync(): Deferred<UserResponse>

    @GET("/review/list/{userId}.xml?v=2&order=a&sort=position")
    fun getBooksInShelfAsync(
        @Path("userId") userId: Long,
        @Query("shelf") shelf: String?
    ): Deferred<ReviewResponse>

    @POST("/user_status.xml")
    @FormUrlEncoded
    fun updateUserStatusByPageNumberAsync(
        @Field("user_status[book_id]") bookId: Long,
        @Field("user_status[page]") page: Int,
        @Field("user_status[body]") body: String?
    ): Deferred<ResponseBody>

    @POST("/review/{reviewId}.xml")
    @FormUrlEncoded
    fun editReviewAsync(
        @Path("reviewId") reviewId: Long,
        @Field("review[review]") reviewText: String?,
        @Field("review[rating]") rating: Int?,
        @Field("review[read_at]") dateRead: ShortDate?,
        @Field("finished") finished: Boolean?,
        @Field("shelf") shelf: String?
    ): Deferred<ResponseBody>

    @POST("/shelf/add_to_shelf.xml")
    @FormUrlEncoded
    fun addBookToShelfAsync(
        @Field("name") shelfName: String,
        @Field("book_id") bookId: Long
    ): Deferred<ResponseBody>
}
