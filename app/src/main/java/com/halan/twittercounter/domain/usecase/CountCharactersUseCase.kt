package com.halan.twittercounter.domain.usecase

import com.halan.twittercounter.domain.model.TweetCharacterCount
import javax.inject.Inject

class CountCharactersUseCase @Inject constructor() {

    companion object {
        const val MAX_TWEET_LENGTH = 280
        private const val URL_LENGTH = 23
        private val URL_REGEX = Regex("https?://\\S+")
    }

    val maxTweetLength = MAX_TWEET_LENGTH

    operator fun invoke(text: String): TweetCharacterCount {
        val normalizedText = text.replace(URL_REGEX) { "x".repeat(URL_LENGTH) }
        val count = weightedCount(normalizedText)
        return TweetCharacterCount(
            typed = count,
            remaining = MAX_TWEET_LENGTH - count,
            isOverLimit = count > MAX_TWEET_LENGTH,
        )
    }

    private fun weightedCount(text: String): Int {
        var count = 0
        var i = 0
        while (i < text.length) {
            val codePoint = text.codePointAt(i)
            count += if (isEmoji(codePoint)) 2 else 1
            i += Character.charCount(codePoint)
        }
        return count
    }

    private fun isEmoji(codePoint: Int): Boolean {
        return codePoint > 0xFFFF
    }
}