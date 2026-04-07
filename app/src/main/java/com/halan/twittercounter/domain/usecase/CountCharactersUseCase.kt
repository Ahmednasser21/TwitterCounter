package com.halan.twittercounter.domain.usecase

import com.halan.twittercounter.domain.model.TweetCharacterCount
import javax.inject.Inject

class CountCharactersUseCase @Inject constructor() {

    companion object {
        const val MAX_TWEET_LENGTH = 280
        private const val URL_LENGTH = 23
        private val URL_REGEX = Regex("https?://\\S+")
    }

    operator fun invoke(text: String): TweetCharacterCount {
        val normalizedText = text.replace(URL_REGEX) { "x".repeat(URL_LENGTH) }
        val count = normalizedText.codePointCount(0, normalizedText.length)
        return TweetCharacterCount(
            typed = count,
            remaining = MAX_TWEET_LENGTH - count,
            isOverLimit = count > MAX_TWEET_LENGTH,
        )
    }
}