package com.halan.twittercounter.di

import com.halan.twittercounter.BuildConfig
import com.halan.twittercounter.domain.credentials.TwitterCredentials
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CredentialsModule {

    @Provides
    @Singleton
    fun provideTwitterCredentials(): TwitterCredentials = object : TwitterCredentials {
        override val apiKey = BuildConfig.TWITTER_API_KEY
        override val apiSecret = BuildConfig.TWITTER_API_SECRET
        override val accessToken = BuildConfig.TWITTER_ACCESS_TOKEN
        override val accessTokenSecret = BuildConfig.TWITTER_ACCESS_TOKEN_SECRET
    }
}