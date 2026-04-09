package com.halan.twittercounter.data.remote.dto


data class PostTweetResponse(
    val data: TweetData?,
    val errors: List<TweetApiError>?
)