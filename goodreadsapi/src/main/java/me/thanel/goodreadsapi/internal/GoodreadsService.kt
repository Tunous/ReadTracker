package me.thanel.goodreadsapi.internal

import kotlinx.coroutines.Deferred
import me.thanel.goodreadsapi.model.UserResponse
import okhttp3.ResponseBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

internal interface GoodreadsService {
    @GET("/user/show/{id}.xml")
    fun getUser(@Path("id") id: Long): Deferred<UserResponse>

    @GET("/api/auth_user")
    fun getUserId(): Deferred<UserResponse>

    @POST("/user_status.xml")
    @FormUrlEncoded
    fun updateUserStatus(
        @Field("user_status[book_id]") bookId: Long,
        @Field("user_status[percent]") percent: Int,
        @Field("user_status[body]") body: String?
    ): Deferred<ResponseBody>
}
