package com.halan.twittercounter.repository

import com.halan.twittercounter.domain.model.Tweet
import com.halan.twittercounter.domain.model.TweetError
import com.halan.twittercounter.domain.model.TweetResult
import com.halan.twittercounter.data.remote.TwitterApiService
import com.halan.twittercounter.data.remote.dto.PostTweetResponse
import com.halan.twittercounter.data.remote.dto.TweetData
import com.halan.twittercounter.data.repository.TweetRepositoryImpl
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class TweetRepositoryImplTest {

    private lateinit var apiService: TwitterApiService
    private lateinit var repository: TweetRepositoryImpl

    @Before
    fun setUp() {
        apiService = mockk()
        repository = TweetRepositoryImpl(apiService = apiService)
    }


    @Test
    fun `postTweet returns Success when response has data`() = runTest {
        coEvery { apiService.postTweet(any()) } returns PostTweetResponse(
            data = TweetData(id = "123", text = "Hello"),
            errors = null
        )

        val result = repository.postTweet(Tweet("Hello"))

        assertTrue(result is TweetResult.Success)
        val success = result as TweetResult.Success
        assertEquals("123", success.tweet.id)
        assertEquals("Hello", success.tweet.text)
    }

    @Test
    fun `postTweet returns Failure Unknown when response data is null`() = runTest {
        coEvery { apiService.postTweet(any()) } returns PostTweetResponse(
            data = null,
            errors = null
        )

        val result = repository.postTweet(Tweet("Hello"))

        assertTrue(result is TweetResult.Failure)
        assertEquals(TweetError.Unknown, (result as TweetResult.Failure).error)
    }


    private fun httpException(code: Int, body: String = "") = HttpException(
        Response.error<Any>(code, body.toResponseBody())
    )

    @Test
    fun `postTweet 401 maps to Unauthorized`() = runTest {
        coEvery { apiService.postTweet(any()) } throws httpException(401)

        val result = repository.postTweet(Tweet("Hello"))

        assertEquals(TweetError.Unauthorized, (result as TweetResult.Failure).error)
    }

    @Test
    fun `postTweet 402 maps to PaymentRequired`() = runTest {
        coEvery { apiService.postTweet(any()) } throws httpException(402)

        val result = repository.postTweet(Tweet("Hello"))

        assertEquals(TweetError.PaymentRequired, (result as TweetResult.Failure).error)
    }

    @Test
    fun `postTweet 403 without duplicate keyword maps to Forbidden`() = runTest {
        coEvery { apiService.postTweet(any()) } throws httpException(403, """{"title":"Forbidden"}""")

        val result = repository.postTweet(Tweet("Hello"))

        assertEquals(TweetError.Forbidden, (result as TweetResult.Failure).error)
    }

    @Test
    fun `postTweet 403 with duplicate in body maps to DuplicateTweet`() = runTest {
        coEvery { apiService.postTweet(any()) } throws httpException(403, """{"detail":"duplicate content"}""")

        val result = repository.postTweet(Tweet("Hello"))

        assertEquals(TweetError.DuplicateTweet, (result as TweetResult.Failure).error)
    }

    @Test
    fun `postTweet 404 maps to NotFound`() = runTest {
        coEvery { apiService.postTweet(any()) } throws httpException(404)

        val result = repository.postTweet(Tweet("Hello"))

        assertEquals(TweetError.NotFound, (result as TweetResult.Failure).error)
    }

    @Test
    fun `postTweet 429 maps to RateLimited with default 60 when header missing`() = runTest {
        coEvery { apiService.postTweet(any()) } throws httpException(429)

        val result = repository.postTweet(Tweet("Hello"))

        val error = (result as TweetResult.Failure).error as TweetError.RateLimited
        assertEquals(60, error.retryAfterSeconds)
    }

    @Test
    fun `postTweet 500 maps to ServerError`() = runTest {
        coEvery { apiService.postTweet(any()) } throws httpException(500)

        val result = repository.postTweet(Tweet("Hello"))

        assertTrue((result as TweetResult.Failure).error is TweetError.ServerError)
    }

    @Test
    fun `postTweet 503 maps to ServerError`() = runTest {
        coEvery { apiService.postTweet(any()) } throws httpException(503)

        val result = repository.postTweet(Tweet("Hello"))

        assertTrue((result as TweetResult.Failure).error is TweetError.ServerError)
    }

    @Test
    fun `postTweet unknown HTTP code maps to Unknown`() = runTest {
        coEvery { apiService.postTweet(any()) } throws httpException(418)

        val result = repository.postTweet(Tweet("Hello"))

        assertEquals(TweetError.Unknown, (result as TweetResult.Failure).error)
    }

    @Test
    fun `postTweet IOException maps to NetworkUnavailable`() = runTest {
        coEvery { apiService.postTweet(any()) } throws IOException("No network")

        val result = repository.postTweet(Tweet("Hello"))

        assertEquals(TweetError.NetworkUnavailable, (result as TweetResult.Failure).error)
    }

    @Test
    fun `postTweet generic Exception maps to Unknown`() = runTest {
        coEvery { apiService.postTweet(any()) } throws RuntimeException("Unexpected")

        val result = repository.postTweet(Tweet("Hello"))

        assertEquals(TweetError.Unknown, (result as TweetResult.Failure).error)
    }
}