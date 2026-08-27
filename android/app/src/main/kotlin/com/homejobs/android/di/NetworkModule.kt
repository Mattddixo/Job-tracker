package com.homejobs.android.di

import com.homejobs.android.data.remote.DynamicBaseUrlInterceptor
import com.homejobs.android.data.remote.HomeJobsApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

/** Never resolved as-is: [DynamicBaseUrlInterceptor] rewrites every request's host before it's sent. */
private const val PLACEHOLDER_BASE_URL = "http://homejobs.invalid/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(dynamicBaseUrlInterceptor)
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
            )
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(PLACEHOLDER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideHomeJobsApiService(retrofit: Retrofit): HomeJobsApiService =
        retrofit.create(HomeJobsApiService::class.java)
}
