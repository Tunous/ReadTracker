package me.thanel.readtracker.api

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.*

object GoodreadsApi {
    private const val BASE_URL = "https://www.goodreads.com/"
    private val dateFormat = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US)

    val service: GoodreadsService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(
            TikXmlConverterFactory.create(
                TikXml.Builder()
                    .exceptionOnUnreadXml(false)
                    .build()
            )
        )
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()
        .create(GoodreadsService::class.java)
}
