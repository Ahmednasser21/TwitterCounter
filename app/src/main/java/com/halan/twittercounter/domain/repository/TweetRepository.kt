package com.halan.twittercounter.domain.repository

import com.halan.twittercounter.domain.model.Tweet
import com.halan.twittercounter.domain.model.TweetResult

interface TweetRepository {
    suspend fun postTweet(tweet: Tweet): TweetResult
    fun copyToClipboard(text: String)
}