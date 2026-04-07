package com.halan.twittercounter.ui.screen

sealed interface TwitterCounterEvent {
    data class OnTextChanged(val text: String) : TwitterCounterEvent
    data object OnCopyText : TwitterCounterEvent
    data object OnClearText : TwitterCounterEvent
    data object OnPostTweet : TwitterCounterEvent
}