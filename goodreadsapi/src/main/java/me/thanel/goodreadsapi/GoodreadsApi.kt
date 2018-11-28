package me.thanel.goodreadsapi

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thanel.goodreadsapi.internal.GoodreadsService
import me.thanel.goodreadsapi.model.UserResponse
import retrofit2.Retrofit

object GoodreadsApi {
    private const val BASE_URL = "https://www.goodreads.com/"

    private val tikXml = TikXml.Builder()
        .exceptionOnUnreadXml(false)
        .build()

    private val service: GoodreadsService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(TikXmlConverterFactory.create(tikXml))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()
        .create(GoodreadsService::class.java)

    suspend fun getUser(id: Long): UserResponse = withContext(Dispatchers.IO) {
        service.getUser(id).await()
    }
}
