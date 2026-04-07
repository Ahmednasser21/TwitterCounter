package com.halan.twittercounter.domain.model

sealed class TweetResult {
    data class Success(val tweet: PostedTweet) : TweetResult()
    data class Failure(val error: TweetError) : TweetResult()
}