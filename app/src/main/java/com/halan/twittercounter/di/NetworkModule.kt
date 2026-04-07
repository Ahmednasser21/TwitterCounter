package com.halan.twittercounter.di

import com.halan.twittercounter.BuildConfig
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
    fun provideOkHttpClient(): OkHttpClient {
        val consumerKey = BuildConfig.TWITTER_API_KEY
        val consumerSecret = BuildConfig.TWITTER_API_SECRET
        val accessToken = BuildConfig.TWITTER_ACCESS_TOKEN
        val accessTokenSecret = BuildConfig.TWITTER_ACCESS_TOKEN_SECRET

        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val request = chain.request()
                val signed = OAuthHelper.sign(
                    request = request,
                    consumerKey = consumerKey,
                    consumerSecret = consumerSecret,
                    accessToken = accessToken,
                    accessTokenSecret = accessTokenSecret,
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