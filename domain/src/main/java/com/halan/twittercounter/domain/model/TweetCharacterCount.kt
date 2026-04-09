package com.halan.twittercounter.domain.model

data class TweetCharacterCount(
    val typed: Int,
    val remaining: Int,
    val isOverLimit: Boolean,
)