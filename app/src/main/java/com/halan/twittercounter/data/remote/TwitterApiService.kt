package com.halan.twittercounter.data.remote

import com.halan.twittercounter.data.remote.dto.PostTweetRequest
import com.halan.twittercounter.data.remote.dto.PostTweetResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TwitterApiService {
    @POST("2/tweets")
    suspend fun postTweet(@Body body: PostTweetRequest): PostTweetResponse
}