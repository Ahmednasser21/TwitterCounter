package com.halan.twittercounter.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halan.twittercounter.R
import com.halan.twittercounter.domain.model.TweetError
import com.halan.twittercounter.domain.model.TweetResult
import com.halan.twittercounter.domain.usecase.CountCharactersUseCase
import com.halan.twittercounter.domain.usecase.PostTweetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TwitterCounterViewModel @Inject constructor(
    private val countCharactersUseCase: CountCharactersUseCase,
    @ApplicationContext private val context: Context,
    private val postTweetUseCase: PostTweetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TwitterCounterUiState())
    val uiState: StateFlow<TwitterCounterUiState> = _uiState.asStateFlow()

    init {
        val maxLength = countCharactersUseCase.maxTweetLength
        _uiState.update {
            it.copy(
                charactersRemaining = maxLength,
                maxTweetLength = maxLength,
            )
        }
    }

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
        val text = _uiState.value.tweetText
        if (text.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = SnackbarMessage.EmptyTextField) }
        } else {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("tweet", text))
            _uiState.update { it.copy(snackbarMessage = SnackbarMessage.CopiedToClipboard) }
        }
    }

    private fun clearText() {
        if (_uiState.value.tweetText.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = SnackbarMessage.EmptyTextField) }
        } else {
            _uiState.update {
                it.copy(
                    tweetText = "",
                    charactersTyped = 0,
                    charactersRemaining = countCharactersUseCase.maxTweetLength,
                    isOverLimit = false,
                    maxTweetLength = countCharactersUseCase.maxTweetLength,
                    snackbarMessage = null,
                )
            }
        }
    }

    private fun postTweet() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = postTweetUseCase(_uiState.value.tweetText)
            _uiState.update { state ->
                when (result) {
                    is TweetResult.Success -> state.copy(
                        isLoading = false,
                        tweetText = "",
                        charactersTyped = 0,
                        charactersRemaining = countCharactersUseCase.maxTweetLength,
                        isOverLimit = false,
                        maxTweetLength = countCharactersUseCase.maxTweetLength,
                        snackbarMessage = SnackbarMessage.TweetPosted,
                    )
                    is TweetResult.Failure -> state.copy(
                        isLoading = false,
                        snackbarMessage = SnackbarMessage.Error(
                            messageRes = when (result.error) {
                                is TweetError.NetworkUnavailable -> R.string.snackbar_error_network
                                is TweetError.Unauthorized -> R.string.snackbar_error_unauthorized
                                is TweetError.RateLimited -> R.string.snackbar_error_rate_limited
                                is TweetError.ServerError -> R.string.snackbar_error_server
                                is TweetError.PaymentRequired -> R.string.snackbar_error_payment_required
                                is TweetError.Forbidden -> R.string.snackbar_error_forbidden
                                is TweetError.DuplicateTweet -> R.string.snackbar_error_duplicate_tweet
                                is TweetError.NotFound -> R.string.snackbar_error_not_found
                                is TweetError.Unknown -> R.string.snackbar_error_unknown
                            },
                            formatArgs = when (val error = result.error) {
                                is TweetError.RateLimited -> arrayOf(error.retryAfterSeconds)
                                is TweetError.ServerError -> arrayOf(error.message)
                                else -> emptyArray()
                            }
                        )
                    )
                }
            }
        }
    }

    private fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}