package me.thanel.goodreadsapi.internal

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import me.thanel.goodreadsapi.BuildConfig
import me.thanel.goodreadsapi.internal.util.applyIf
import me.thanel.goodreadsapi.model.GoodreadsSecrets
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer
import se.akerfeldt.okhttp.signpost.SigningInterceptor

internal class GoodreadsServiceCreator(
    private val baseUrl: String,
    secrets: GoodreadsSecrets
) {

    private val oAuthConsumer by lazy {
        OkHttpOAuthConsumer(secrets.consumerKey, secrets.consumerSecret).apply {
            setTokenWithSecret(secrets.token, secrets.tokenSecret)
        }
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(SigningInterceptor(oAuthConsumer))
            .applyIf(BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
            .build()
    }

    private val tikXml by lazy {
        TikXml.Builder()
            .exceptionOnUnreadXml(false)
            .build()
    }

    fun createService(): GoodreadsService = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(TikXmlConverterFactory.create(tikXml))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()
        .create(GoodreadsService::class.java)
}
