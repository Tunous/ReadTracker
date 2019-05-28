package me.thanel.goodreadsapi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.thanel.goodreadsapi.internal.GoodreadsApiImpl
import me.thanel.goodreadsapi.model.AccessTokenData
import me.thanel.goodreadsapi.model.Book
import me.thanel.goodreadsapi.model.GoodreadsSecrets
import me.thanel.goodreadsapi.model.ReadingProgressStatusGroup
import me.thanel.goodreadsapi.model.RequestTokenData
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer
import se.akerfeldt.okhttp.signpost.OkHttpOAuthProvider

/**
 * Core interface for communication with the Goodreads API.
 */
interface GoodreadsApi {

    /// Requests

    /**
     * Gets the id of the currently authenticated user.
     */
    suspend fun getUserId(): Long

    /**
     * Gets books in the [shelf] of user with id equal to [userId].
     */
    suspend fun getBooksInShelf(userId: Long, shelf: String): List<Book>

    /**
     * Gets currently read books together with their progress for the user with id equal to
     * [userId].
     */
    suspend fun getReadingProgressStatus(userId: Long): ReadingProgressStatusGroup

    /// Actions

    suspend fun updateProgressByPageNumber(bookId: Long, page: Int, body: String?)

    suspend fun finishReading(reviewId: Long, rating: Int?, body: String?)

    suspend fun startReadingBook(bookId: Long)

    companion object {

        /**
         * Creates class for communicating with the Goodreads API.
         *
         * @param secrets secret information required by some of the calls to the Goodreads API.
         * @param baseUrl the base url of the Goodreads API.
         */
        fun create(
            secrets: GoodreadsSecrets,
            baseUrl: String = DEFAULT_BASE_URL
        ): GoodreadsApi = GoodreadsApiImpl(secrets, baseUrl)

        suspend fun authorize(
            consumerKey: String,
            consumerSecret: String,
            baseUrl: String = DEFAULT_BASE_URL
        ) = withContext(Dispatchers.IO) {
            val provider = createAuthProvider(baseUrl)
            val consumer = OkHttpOAuthConsumer(consumerKey, consumerSecret)
            val authUrl = provider.retrieveRequestToken(consumer, CALLBACK_URL)
            RequestTokenData(authUrl, consumer.token, consumer.tokenSecret)
        }

        suspend fun getAccessToken(
            consumerKey: String,
            consumerSecret: String,
            token: String,
            tokenSecret: String,
            baseUrl: String = DEFAULT_BASE_URL
        ) = withContext(Dispatchers.IO) {
            val consumer = OkHttpOAuthConsumer(consumerKey, consumerSecret)
            consumer.setTokenWithSecret(token, tokenSecret)
            createAuthProvider(baseUrl).retrieveAccessToken(consumer, null)
            AccessTokenData(consumer.token, consumer.tokenSecret)
        }

        private const val DEFAULT_BASE_URL = "https://www.goodreads.com/"
        private const val CALLBACK_URL = "me.thanel.readtracker://oauth"

        private fun createAuthProvider(baseUrl: String) = OkHttpOAuthProvider(
            "${baseUrl}oauth/request_token",
            "${baseUrl}oauth/access_token",
            "${baseUrl}oauth/authorize?mobile=1"
        )
    }
}
