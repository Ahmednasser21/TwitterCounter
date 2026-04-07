package com.halan.twittercounter.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halan.twittercounter.domain.model.TweetError
import com.halan.twittercounter.domain.model.TweetResult
import com.halan.twittercounter.domain.usecase.CopyTextUseCase
import com.halan.twittercounter.domain.usecase.CountCharactersUseCase
import com.halan.twittercounter.domain.usecase.PostTweetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TwitterCounterViewModel @Inject constructor(
    private val countCharactersUseCase: CountCharactersUseCase,
    private val copyTextUseCase: CopyTextUseCase,
    private val postTweetUseCase: PostTweetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TwitterCounterUiState())
    val uiState: StateFlow<TwitterCounterUiState> = _uiState.asStateFlow()

    fun onEvent(event: TwitterCounterEvent) {
        when (event) {
            is TwitterCounterEvent.OnTextChanged -> onTextChanged(event.text)
            is TwitterCounterEvent.OnCopyText -> copyText()
            is TwitterCounterEvent.OnClearText -> clearText()
            is TwitterCounterEvent.OnPostTweet -> postTweet()
            is TwitterCounterEvent.OnSnackbarDismissed -> dismissSnackbar()
        }
    }

    private fun onTextChanged(text: String) {
        val count = countCharactersUseCase(text)
        _uiState.update {
            it.copy(
                tweetText = text,
                charactersTyped = count.typed,
                charactersRemaining = count.remaining,
                isOverLimit = count.isOverLimit,
            )
        }
    }

    private fun copyText() {
        copyTextUseCase(_uiState.value.tweetText)
        _uiState.update { it.copy(snackbarMessage = SnackbarMessage.CopiedToClipboard) }
    }

    private fun clearText() {
        _uiState.update { TwitterCounterUiState() }
    }

    private fun postTweet() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = postTweetUseCase(_uiState.value.tweetText)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    snackbarMessage = when (result) {
                        is TweetResult.Success -> SnackbarMessage.TweetPosted
                        is TweetResult.Failure -> when (result.error) {
                            is TweetError.NetworkUnavailable -> SnackbarMessage.Error("No internet connection.")
                            is TweetError.Unauthorized -> SnackbarMessage.Error("Authentication failed. Check your API keys.")
                            is TweetError.RateLimited -> SnackbarMessage.Error("Rate limited. Try again in ${result.error.retryAfterSeconds}s.")
                            is TweetError.ServerError -> SnackbarMessage.Error("Server error: ${result.error.message}")
                            is TweetError.Unknown -> SnackbarMessage.Error("Something went wrong. Please try again.")
                        }
                    }
                )
            }
        }
    }

    private fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}