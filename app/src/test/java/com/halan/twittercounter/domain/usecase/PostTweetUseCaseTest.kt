package com.halan.twittercounter.domain.usecase

import com.halan.twittercounter.domain.model.PostedTweet
import com.halan.twittercounter.domain.model.TweetError
import com.halan.twittercounter.domain.model.TweetResult
import com.halan.twittercounter.domain.repository.TweetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PostTweetUseCaseTest {

    private lateinit var repository: TweetRepository
    private lateinit var useCase: PostTweetUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = PostTweetUseCase(repository)
    }

    @Test
    fun `invoke passes text wrapped in Tweet to repository`() = runTest {
        coEvery { repository.postTweet(any()) } returns TweetResult.Success(
            PostedTweet(id = "1", text = "Hello")
        )

        useCase("Hello")

        coVerify { repository.postTweet(match { it.text == "Hello" }) }
    }

    @Test
    fun `invoke returns Success from repository`() = runTest {
        val expected = TweetResult.Success(PostedTweet(id = "42", text = "Test"))
        coEvery { repository.postTweet(any()) } returns expected

        val result = useCase("Test")

        assertEquals(expected, result)
    }

    @Test
    fun `invoke returns Failure from repository`() = runTest {
        val expected = TweetResult.Failure(TweetError.NetworkUnavailable)
        coEvery { repository.postTweet(any()) } returns expected

        val result = useCase("Test")

        assertTrue(result is TweetResult.Failure)
        assertEquals(TweetError.NetworkUnavailable, (result as TweetResult.Failure).error)
    }
}