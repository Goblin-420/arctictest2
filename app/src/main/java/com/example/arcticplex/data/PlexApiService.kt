package com.example.arcticplex.data

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface PlexApiService {

    @Headers("Accept: application/json")
    @GET("library/sections")
    suspend fun getSections(
        @Query("X-Plex-Token") token: String
    ): PlexResponse

    @Headers("Accept: application/json")
    @GET("library/sections/{sectionKey}/all")
    suspend fun getSectionItems(
        @Path("sectionKey") sectionKey: String,
        @Query("X-Plex-Token") token: String
    ): PlexResponse

    @Headers("Accept: application/json")
    @GET("library/recentlyAdded")
    suspend fun getRecentlyAdded(
        @Query("X-Plex-Token") token: String
    ): PlexResponse

    @Headers("Accept: application/json")
    @GET("library/onDeck")
    suspend fun getOnDeck(
        @Query("X-Plex-Token") token: String
    ): PlexResponse

    @Headers("Accept: application/json")
    @GET(":/timeline")
    suspend fun updateTimeline(
        @Query("ratingKey") ratingKey: String,
        @Query("key") key: String,
        @Query("state") state: String,
        @Query("time") currentTimeMs: Long,
        @Query("duration") durationMs: Long,
        @Query("X-Plex-Token") token: String,
        @Query("X-Plex-Client-Identifier") clientId: String = "ArcticPlex-FireTV"
    )

    companion object {
        fun create(baseUrl: String): PlexApiService {
            val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val headerInterceptor = Interceptor { chain ->
                val req = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("X-Plex-Product", "ArcticPlex")
                    .addHeader("X-Plex-Version", "1.0")
                    .addHeader("X-Plex-Client-Identifier", "ArcticPlex-FireTV-Client")
                    .addHeader("X-Plex-Platform", "Android TV")
                    .build()
                chain.proceed(req)
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(normalizedUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(PlexApiService::class.java)
        }
    }
}
