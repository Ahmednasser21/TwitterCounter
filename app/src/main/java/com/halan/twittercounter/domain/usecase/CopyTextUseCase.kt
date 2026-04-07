package com.halan.twittercounter.domain.usecase

import com.halan.twittercounter.domain.repository.TweetRepository
import javax.inject.Inject

class CopyTextUseCase @Inject constructor(
    private val repository: TweetRepository
) {
    operator fun invoke(text: String) {
        repository.copyToClipboard(text)
    }
}