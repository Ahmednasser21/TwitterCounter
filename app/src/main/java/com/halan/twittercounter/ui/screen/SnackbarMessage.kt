package com.halan.twittercounter.ui.screen


sealed interface SnackbarMessage {
    data object TweetPosted : SnackbarMessage
    data object CopiedToClipboard : SnackbarMessage
    data object EmptyTextField : SnackbarMessage
    data class Error(
        val messageRes: Int,
        val formatArgs: Array<out Any> = emptyArray(),
    ) : SnackbarMessage {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Error

            if (messageRes != other.messageRes) return false
            if (!formatArgs.contentEquals(other.formatArgs)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = messageRes
            result = 31 * result + formatArgs.contentHashCode()
            return result
        }
    }
}