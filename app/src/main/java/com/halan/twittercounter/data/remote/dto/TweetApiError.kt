package com.halan.twittercounter.data.remote.dto

data class TweetApiError(
    val title: String,
    val type: String,
    val detail: String?,
    val status: Int?
)