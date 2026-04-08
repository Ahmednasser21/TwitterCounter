package com.halan.twittercounter.ui.screen

data class TwitterCounterUiState(
    val tweetText: String = "",
    val charactersTyped: Int = 0,
    val charactersRemaining: Int = 0,
    val isOverLimit: Boolean = false,
    val isLoading: Boolean = false,
    val maxTweetLength: Int = 0,
    val snackbarMessage: SnackbarMessage? = null,
)