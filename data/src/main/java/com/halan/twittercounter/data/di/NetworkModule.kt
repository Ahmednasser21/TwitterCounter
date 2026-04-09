package com.halan.twittercounter.data.di

import com.halan.twittercounter.domain.credentials.TwitterCredentials
import com.halan.twittercounter.data.remote.OAuthHelper
import com.halan.twittercounter.data.remote.TwitterApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.x.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(credentials: TwitterCredentials): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val signed = OAuthHelper.sign(
                    request = chain.request(),
                    consumerKey = credentials.apiKey,
                    consumerSecret = credentials.apiSecret,
                    accessToken = credentials.accessToken,
                    accessTokenSecret = credentials.accessTokenSecret,
                )
                chain.proceed(signed)
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideTwitterApiService(retrofit: Retrofit): TwitterApiService =
        retrofit.create(TwitterApiService::class.java)
}