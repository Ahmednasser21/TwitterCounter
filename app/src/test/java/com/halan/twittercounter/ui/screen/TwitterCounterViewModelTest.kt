package com.halan.twittercounter.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import app.cash.turbine.test
import com.halan.twittercounter.R
import com.halan.twittercounter.domain.model.PostedTweet
import com.halan.twittercounter.domain.model.TweetError
import com.halan.twittercounter.domain.model.TweetResult
import com.halan.twittercounter.domain.usecase.CountCharactersUseCase
import com.halan.twittercounter.domain.usecase.PostTweetUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TwitterCounterViewModelTest{

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var countCharactersUseCase: CountCharactersUseCase
    private lateinit var context: Context
    private lateinit var postTweetUseCase: PostTweetUseCase
    private lateinit var viewModel: TwitterCounterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        countCharactersUseCase = CountCharactersUseCase()
        postTweetUseCase = mockk()

        val clipboardManager = mockk<ClipboardManager>(relaxed = true)
        context = mockk {
            every { getSystemService(Context.CLIPBOARD_SERVICE) } returns clipboardManager
        }

        viewModel = TwitterCounterViewModel(
            context = context,
            countCharactersUseCase = countCharactersUseCase,
            postTweetUseCase = postTweetUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `initial state has correct max length and full remaining count`() {
        val state = viewModel.uiState.value
        assertEquals(280, state.maxTweetLength)
        assertEquals(280, state.charactersRemaining)
        assertEquals(0, state.charactersTyped)
        assertEquals("", state.tweetText)
        assertFalse(state.isOverLimit)
        assertFalse(state.isLoading)
        assertNull(state.snackbarMessage)
    }


    @Test
    fun `OnTextChanged updates tweetText and character counts`() {
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        val state = viewModel.uiState.value
        assertEquals("Hello", state.tweetText)
        assertEquals(5, state.charactersTyped)
        assertEquals(275, state.charactersRemaining)
        assertFalse(state.isOverLimit)
    }

    @Test
    fun `OnTextChanged with empty string resets counts`() {
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged(""))
        val state = viewModel.uiState.value
        assertEquals("", state.tweetText)
        assertEquals(0, state.charactersTyped)
        assertEquals(280, state.charactersRemaining)
        assertFalse(state.isOverLimit)
    }

    @Test
    fun `OnTextChanged with exactly 280 characters is not over limit`() {
        val text = "a".repeat(280)
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged(text))
        val state = viewModel.uiState.value
        assertEquals(280, state.charactersTyped)
        assertEquals(0, state.charactersRemaining)
        assertFalse(state.isOverLimit)
    }

    @Test
    fun `OnTextChanged with 281 characters sets isOverLimit true`() {
        val text = "a".repeat(281)
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged(text))
        val state = viewModel.uiState.value
        assertEquals(281, state.charactersTyped)
        assertEquals(-1, state.charactersRemaining)
        assertTrue(state.isOverLimit)
    }

    @Test
    fun `OnTextChanged with URL counts URL as 23 characters`() {
        val url = "https://example.com/test/long/path"
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged(url))
        val state = viewModel.uiState.value
        assertEquals(23, state.charactersTyped)
        assertEquals(257, state.charactersRemaining)
        assertFalse(state.isOverLimit)
    }

    @Test
    fun `OnTextChanged with text and URL counts correctly`() {
        val text = "Test URL https://example.com"
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged(text))
        val state = viewModel.uiState.value
        assertEquals(32, state.charactersTyped)
    }

    @Test
    fun `OnTextChanged with emoji counts by code points`() {
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("😀"))
        val state = viewModel.uiState.value
        assertEquals(2, state.charactersTyped)
        assertEquals(278, state.charactersRemaining)
    }

    @Test
    fun `OnTextChanged with 140 emojis is exactly at limit`() {
        val text = "😀".repeat(140)
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged(text))
        val state = viewModel.uiState.value
        assertEquals(280, state.charactersTyped)
        assertEquals(0, state.charactersRemaining)
        assertFalse(state.isOverLimit)
    }

    @Test
    fun `OnTextChanged with 141 emojis is over limit`() {
        val text = "😀".repeat(141)
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged(text))
        val state = viewModel.uiState.value
        assertEquals(282, state.charactersTyped)
        assertTrue(state.isOverLimit)
    }

    @Test
    fun `OnTextChanged with multiple URLs counts each as 23`() {
        val text = "https://a.com https://b.com"
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged(text))
        val state = viewModel.uiState.value
        assertEquals(47, state.charactersTyped)
    }


    @Test
    fun `OnCopyText with blank text shows EmptyTextField snackbar`() {
        viewModel.onEvent(TwitterCounterEvent.OnCopyText)
        assertEquals(SnackbarMessage.EmptyTextField, viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `OnCopyText with whitespace-only text shows EmptyTextField snackbar`() {
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("   "))
        viewModel.onEvent(TwitterCounterEvent.OnCopyText)
        assertEquals(SnackbarMessage.EmptyTextField, viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `OnCopyText with valid text shows CopiedToClipboard snackbar`() {
        mockkStatic(ClipData::class)
        every { ClipData.newPlainText(any(), any()) } returns mockk()

        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        viewModel.onEvent(TwitterCounterEvent.OnCopyText)

        assertEquals(SnackbarMessage.CopiedToClipboard, viewModel.uiState.value.snackbarMessage)

        unmockkStatic(ClipData::class)
    }

    @Test
    fun `OnCopyText does not show CopiedToClipboard when text is blank`() {
        viewModel.onEvent(TwitterCounterEvent.OnCopyText)
        assertEquals(SnackbarMessage.EmptyTextField, viewModel.uiState.value.snackbarMessage)
    }


    @Test
    fun `OnClearText with blank text shows EmptyTextField snackbar`() {
        viewModel.onEvent(TwitterCounterEvent.OnClearText)
        assertEquals(SnackbarMessage.EmptyTextField, viewModel.uiState.value.snackbarMessage)
    }

    @Test
    fun `OnClearText with valid text resets all fields`() {
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello World"))
        viewModel.onEvent(TwitterCounterEvent.OnClearText)
        val state = viewModel.uiState.value
        assertEquals("", state.tweetText)
        assertEquals(0, state.charactersTyped)
        assertEquals(280, state.charactersRemaining)
        assertFalse(state.isOverLimit)
        assertNull(state.snackbarMessage)
    }

    @Test
    fun `OnClearText resets isOverLimit when text was over limit`() {
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("a".repeat(300)))
        assertTrue(viewModel.uiState.value.isOverLimit)
        viewModel.onEvent(TwitterCounterEvent.OnClearText)
        assertFalse(viewModel.uiState.value.isOverLimit)
    }


    @Test
    fun `OnSnackbarDismissed clears snackbarMessage`() {
        viewModel.onEvent(TwitterCounterEvent.OnCopyText)
        viewModel.onEvent(TwitterCounterEvent.OnSnackbarDismissed)
        assertNull(viewModel.uiState.value.snackbarMessage)
    }


    @Test
    fun `OnPostTweet success resets state and shows TweetPosted snackbar`() = runTest {
        coEvery { postTweetUseCase(any()) } returns TweetResult.Success(
            PostedTweet(id = "1", text = "Hello")
        )
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(TwitterCounterEvent.OnPostTweet)
            val loading = awaitItem()
            assertTrue(loading.isLoading)
            val done = awaitItem()
            assertFalse(done.isLoading)
            assertEquals("", done.tweetText)
            assertEquals(0, done.charactersTyped)
            assertEquals(280, done.charactersRemaining)
            assertFalse(done.isOverLimit)
            assertEquals(SnackbarMessage.TweetPosted, done.snackbarMessage)
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `OnPostTweet NetworkUnavailable shows correct error snackbar`() = runTest {
        coEvery { postTweetUseCase(any()) } returns TweetResult.Failure(TweetError.NetworkUnavailable)
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        viewModel.onEvent(TwitterCounterEvent.OnPostTweet)
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(
            SnackbarMessage.Error(messageRes = R.string.snackbar_error_network),
            state.snackbarMessage
        )
    }

    @Test
    fun `OnPostTweet Unauthorized shows correct error snackbar`() = runTest {
        coEvery { postTweetUseCase(any()) } returns TweetResult.Failure(TweetError.Unauthorized)
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        viewModel.onEvent(TwitterCounterEvent.OnPostTweet)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            SnackbarMessage.Error(messageRes = R.string.snackbar_error_unauthorized),
            viewModel.uiState.value.snackbarMessage
        )
    }

    @Test
    fun `OnPostTweet RateLimited includes retryAfterSeconds in formatArgs`() = runTest {
        coEvery { postTweetUseCase(any()) } returns TweetResult.Failure(TweetError.RateLimited(retryAfterSeconds = 30))
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        viewModel.onEvent(TwitterCounterEvent.OnPostTweet)
        testDispatcher.scheduler.advanceUntilIdle()
        val msg = viewModel.uiState.value.snackbarMessage as SnackbarMessage.Error
        assertEquals(R.string.snackbar_error_rate_limited, msg.messageRes)
        assertTrue(msg.formatArgs.contains(30))
    }

    @Test
    fun `OnPostTweet ServerError includes message in formatArgs`() = runTest {
        coEvery { postTweetUseCase(any()) } returns TweetResult.Failure(TweetError.ServerError("Internal error"))
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        viewModel.onEvent(TwitterCounterEvent.OnPostTweet)
        testDispatcher.scheduler.advanceUntilIdle()
        val msg = viewModel.uiState.value.snackbarMessage as SnackbarMessage.Error
        assertEquals(R.string.snackbar_error_server, msg.messageRes)
        assertTrue(msg.formatArgs.contains("Internal error"))
    }

    @Test
    fun `OnPostTweet PaymentRequired shows correct error snackbar`() = runTest {
        coEvery { postTweetUseCase(any()) } returns TweetResult.Failure(TweetError.PaymentRequired)
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        viewModel.onEvent(TwitterCounterEvent.OnPostTweet)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            SnackbarMessage.Error(messageRes = R.string.snackbar_error_payment_required),
            viewModel.uiState.value.snackbarMessage
        )
    }

    @Test
    fun `OnPostTweet Forbidden shows correct error snackbar`() = runTest {
        coEvery { postTweetUseCase(any()) } returns TweetResult.Failure(TweetError.Forbidden)
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        viewModel.onEvent(TwitterCounterEvent.OnPostTweet)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            SnackbarMessage.Error(messageRes = R.string.snackbar_error_forbidden),
            viewModel.uiState.value.snackbarMessage
        )
    }

    @Test
    fun `OnPostTweet DuplicateTweet shows correct error snackbar`() = runTest {
        coEvery { postTweetUseCase(any()) } returns TweetResult.Failure(TweetError.DuplicateTweet)
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        viewModel.onEvent(TwitterCounterEvent.OnPostTweet)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            SnackbarMessage.Error(messageRes = R.string.snackbar_error_duplicate_tweet),
            viewModel.uiState.value.snackbarMessage
        )
    }

    @Test
    fun `OnPostTweet NotFound shows correct error snackbar`() = runTest {
        coEvery { postTweetUseCase(any()) } returns TweetResult.Failure(TweetError.NotFound)
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        viewModel.onEvent(TwitterCounterEvent.OnPostTweet)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            SnackbarMessage.Error(messageRes = R.string.snackbar_error_not_found),
            viewModel.uiState.value.snackbarMessage
        )
    }

    @Test
    fun `OnPostTweet Unknown error shows correct error snackbar`() = runTest {
        coEvery { postTweetUseCase(any()) } returns TweetResult.Failure(TweetError.Unknown)
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        viewModel.onEvent(TwitterCounterEvent.OnPostTweet)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(
            SnackbarMessage.Error(messageRes = R.string.snackbar_error_unknown),
            viewModel.uiState.value.snackbarMessage
        )
    }

    @Test
    fun `OnPostTweet failure preserves tweetText`() = runTest {
        coEvery { postTweetUseCase(any()) } returns TweetResult.Failure(TweetError.Unknown)
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        viewModel.onEvent(TwitterCounterEvent.OnPostTweet)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Hello", viewModel.uiState.value.tweetText)
    }

    @Test
    fun `isLoading is true during post and false after`() = runTest {
        coEvery { postTweetUseCase(any()) } returns TweetResult.Success(
            PostedTweet("1", "Hello")
        )
        viewModel.onEvent(TwitterCounterEvent.OnTextChanged("Hello"))
        viewModel.uiState.test {
            awaitItem()
            viewModel.onEvent(TwitterCounterEvent.OnPostTweet)
            assertTrue(awaitItem().isLoading)
            assertFalse(awaitItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}