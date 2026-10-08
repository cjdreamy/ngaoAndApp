package com.ngao.maternalcare.data.remote

import com.ngao.maternalcare.BuildConfig
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    /**
     * Every request to Supabase needs the project's anon `apikey` header.
     * If the user is logged in, we also attach their access token as the
     * Bearer token so Row Level Security policies apply to them correctly.
     */
    private fun authInterceptor(sessionManager: SessionManager) = Interceptor { chain ->
        val token = runBlocking { sessionManager.currentAccessToken() } ?: BuildConfig.SUPABASE_ANON_KEY
        val request = chain.request().newBuilder()
            .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer $token")
            .build()
        chain.proceed(request)
    }

    fun buildRetrofit(sessionManager: SessionManager): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor(sessionManager))
            .addInterceptor(logging)
            .build()

        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}
