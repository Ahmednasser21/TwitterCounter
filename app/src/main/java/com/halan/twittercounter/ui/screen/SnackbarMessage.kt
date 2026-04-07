package com.halan.twittercounter.ui.screen

sealed interface SnackbarMessage {
    data object TweetPosted : SnackbarMessage
    data object CopiedToClipboard : SnackbarMessage
    data class Error(val reason: String) : SnackbarMessage
}