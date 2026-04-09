package com.halan.twittercounter.domain.usecase

import com.halan.twittercounter.domain.model.Tweet
import com.halan.twittercounter.domain.model.TweetResult
import com.halan.twittercounter.domain.repository.TweetRepository
import javax.inject.Inject

class PostTweetUseCase @Inject constructor(
    private val repository: TweetRepository
) {
    suspend operator fun invoke(text: String): TweetResult {
        return repository.postTweet(Tweet(text))
    }
}