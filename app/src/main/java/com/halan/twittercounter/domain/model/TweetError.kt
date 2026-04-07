package com.halan.twittercounter.domain.model

sealed class TweetError {
    data object NetworkUnavailable : TweetError()
    data object Unauthorized : TweetError()
    data class RateLimited(val retryAfterSeconds: Int) : TweetError()
    data class ServerError(val message: String) : TweetError()
    data object Unknown : TweetError()
}