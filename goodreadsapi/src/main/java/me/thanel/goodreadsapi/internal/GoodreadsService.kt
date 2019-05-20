package me.thanel.goodreadsapi.internal

import kotlinx.coroutines.Deferred
import me.thanel.goodreadsapi.model.ShortDate
import me.thanel.goodreadsapi.model.UserResponse
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface GoodreadsService {
    @GET("/user/show/{id}.xml")
    fun getUserAsync(@Path("id") id: Long): Deferred<UserResponse>

    @GET("/api/auth_user")
    fun getUserIdAsync(): Deferred<UserResponse>

    @POST("/user_status.xml")
    @FormUrlEncoded
    fun updateUserStatusByPercentAsync(
        @Field("user_status[book_id]") bookId: Long,
        @Field("user_status[percent]") percent: Int,
        @Field("user_status[body]") body: String?
    ): Deferred<ResponseBody>

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
}
