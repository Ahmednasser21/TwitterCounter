package com.halan.twittercounter.ui.screen

data class TwitterCounterUiState(
    val tweetText: String = "",
    val charactersTyped: Int = 0,
    val charactersRemaining: Int = MAX_TWEET_LENGTH,
    val isOverLimit: Boolean = false,
) {
    companion object {
        const val MAX_TWEET_LENGTH = 280
    }
}