package com.halan.twittercounter.data.repository

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.halan.twittercounter.data.remote.dto.PostTweetRequest
import com.halan.twittercounter.data.remote.TwitterApiService
import com.halan.twittercounter.domain.model.PostedTweet
import com.halan.twittercounter.domain.model.Tweet
import com.halan.twittercounter.domain.model.TweetError
import com.halan.twittercounter.domain.model.TweetResult
import com.halan.twittercounter.domain.repository.TweetRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class TweetRepositoryImpl @Inject constructor(
    private val apiService: TwitterApiService,
    @ApplicationContext private val context: Context,
) : TweetRepository {

    override suspend fun postTweet(tweet: Tweet): TweetResult = try {
        val response = apiService.postTweet(PostTweetRequest(tweet.text))

        if (response.data != null) {
            TweetResult.Success(PostedTweet(id = response.data.id, text = response.data.text))
        } else {
            TweetResult.Failure(TweetError.Unknown)
        }
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        val isDuplicate = errorBody?.contains("duplicate", ignoreCase = true) == true

        val error = when (e.code()) {
            401 -> TweetError.Unauthorized
            402 -> TweetError.PaymentRequired
            403 -> if (isDuplicate) TweetError.DuplicateTweet else TweetError.Forbidden
            404 -> TweetError.NotFound
            429 -> {
                val retryAfter = e.response()?.headers()?.get("retry-after")?.toIntOrNull() ?: 60
                TweetError.RateLimited(retryAfterSeconds = retryAfter)
            }

            in 500..599 -> TweetError.ServerError(e.message())
            else -> TweetError.Unknown
        }
        TweetResult.Failure(error)
    } catch (_: IOException) {
        TweetResult.Failure(TweetError.NetworkUnavailable)
    } catch (_: Exception) {
        TweetResult.Failure(TweetError.Unknown)
    }

    override fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("tweet", text)
        clipboard.setPrimaryClip(clip)
    }
}