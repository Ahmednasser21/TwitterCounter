package com.halan.twittercounter.domain.credentials

interface TwitterCredentials {
    val apiKey: String
    val apiSecret: String
    val accessToken: String
    val accessTokenSecret: String
}