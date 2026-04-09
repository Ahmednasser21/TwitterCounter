package com.halan.twittercounter.data.credentials

interface TwitterCredentials {
    val apiKey: String
    val apiSecret: String
    val accessToken: String
    val accessTokenSecret: String
}