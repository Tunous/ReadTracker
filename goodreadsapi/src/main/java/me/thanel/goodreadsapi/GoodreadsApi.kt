package me.thanel.goodreadsapi

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thanel.goodreadsapi.internal.GoodreadsService
import me.thanel.goodreadsapi.internal.model.ShortDate
import me.thanel.goodreadsapi.internal.util.applyIf
import me.thanel.goodreadsapi.internal.util.nullIfBlank
import me.thanel.goodreadsapi.model.AccessTokenData
import me.thanel.goodreadsapi.model.Book
import me.thanel.goodreadsapi.model.ReadingProgressStatus
import me.thanel.goodreadsapi.model.ReadingProgressStatusGroup
import me.thanel.goodreadsapi.model.RequestTokenData
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer
import se.akerfeldt.okhttp.signpost.OkHttpOAuthProvider
import se.akerfeldt.okhttp.signpost.SigningInterceptor

class GoodreadsApi(
    consumerKey: String,
    consumerSecret: String,
    private val token: String,
    private val tokenSecret: String
) {
    private val oAuthConsumer = OkHttpOAuthConsumer(consumerKey, consumerSecret).apply {
        setTokenWithSecret(this@GoodreadsApi.token, this@GoodreadsApi.tokenSecret)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(SigningInterceptor(oAuthConsumer))
        .applyIf(BuildConfig.DEBUG) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
        }
        .build()

    private val tikXml = TikXml.Builder()
        .exceptionOnUnreadXml(false)
        .build()

    private val service: GoodreadsService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(TikXmlConverterFactory.create(tikXml))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()
        .create(GoodreadsService::class.java)

    suspend fun getReadingProgressStatus(userId: Long): ReadingProgressStatusGroup =
        withContext(Dispatchers.Default) {
            val userResponse = service.getUserAsync(userId).await()
            val statuses = userResponse.user.userStatuses?.map {
                ReadingProgressStatus(it.id, it.book.id, it.page, it.percent, it.reviewId)
            }
            val books = userResponse.user.userStatuses?.map { status ->
                val book = status.book
                Book(
                    book.id,
                    book.title,
                    book.numPages,
                    book.imageUrl,
                    book.authors.joinToString { it.name }.nullIfBlank()
                )
            }
            return@withContext ReadingProgressStatusGroup(
                statuses ?: emptyList(),
                books ?: emptyList()
            )
        }

    suspend fun getBooksInShelf(userId: Long, shelf: String) = withContext(Dispatchers.Default) {
        val reviewsResponse = service.getBooksInShelfAsync(userId, shelf).await()
        return@withContext reviewsResponse.reviews.map {
            val book = it.book
            Book(
                book.id,
                book.title,
                book.numPages,
                book.imageUrl,
                book.authors.joinToString { it.name }.nullIfBlank()
            )
        }
    }

    suspend fun getUserId() = withContext(Dispatchers.Default) {
        service.getUserIdAsync().await().user.id
    }

    suspend fun updateProgressByPercent(bookId: Long, percent: Int, body: String?) =
        withContext(Dispatchers.Default) {
            service.updateUserStatusByPercentAsync(bookId, percent, body.nullIfBlank()).await()
        }

    suspend fun updateProgressByPageNumber(bookId: Long, page: Int, body: String?) =
        withContext(Dispatchers.Default) {
            service.updateUserStatusByPageNumberAsync(bookId, page, body.nullIfBlank()).await()
        }

    suspend fun finishReading(reviewId: Long, rating: Int?, body: String?) =
        withContext(Dispatchers.Default) {
            service.editReviewAsync(
                reviewId = reviewId,
                reviewText = body.nullIfBlank(),
                rating = rating,
                dateRead = ShortDate.now(),
                shelf = "read",
                finished = true
            ).await()
        }

    companion object {
        private const val BASE_URL = "https://www.goodreads.com/"
        private const val CALLBACK_URL = "me.thanel.readtracker://oauth"

        private val provider = OkHttpOAuthProvider(
            "${BASE_URL}oauth/request_token",
            "${BASE_URL}oauth/access_token",
            "${BASE_URL}oauth/authorize?mobile=1"
        )

        suspend fun authorize(consumerKey: String, consumerSecret: String) =
            withContext(Dispatchers.Default) {
                val consumer = OkHttpOAuthConsumer(consumerKey, consumerSecret)
                val authUrl = provider.retrieveRequestToken(consumer, CALLBACK_URL)
                RequestTokenData(authUrl, consumer.token, consumer.tokenSecret)
            }

        suspend fun getAccessToken(
            consumerKey: String,
            consumerSecret: String,
            token: String,
            tokenSecret: String
        ) = withContext(Dispatchers.Default) {
            val consumer = OkHttpOAuthConsumer(consumerKey, consumerSecret)
            consumer.setTokenWithSecret(token, tokenSecret)
            provider.retrieveAccessToken(consumer, null)
            AccessTokenData(consumer.token, consumer.tokenSecret)
        }
    }
}
