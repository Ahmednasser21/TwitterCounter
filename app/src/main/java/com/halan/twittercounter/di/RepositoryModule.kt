package com.halan.twittercounter.di

import com.halan.twittercounter.data.repository.TweetRepositoryImpl
import com.halan.twittercounter.domain.repository.TweetRepository
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