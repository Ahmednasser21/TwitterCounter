package com.halan.twittercounter.data.di

import com.halan.twittercounter.domain.repository.TweetRepository
import com.halan.twittercounter.data.repository.TweetRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTweetRepository(
        impl: TweetRepositoryImpl
    ): TweetRepository
}