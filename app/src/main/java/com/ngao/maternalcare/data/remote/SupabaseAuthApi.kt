package com.ngao.maternalcare.data.remote

import com.ngao.maternalcare.data.model.AuthResponse
import com.ngao.maternalcare.data.model.SignInRequest
import com.ngao.maternalcare.data.model.SignUpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseAuthApi {

    @Headers("Content-Type: application/json")
    @POST("auth/v1/signup")
    suspend fun signUp(@Body body: SignUpRequest): Response<AuthResponse>

    @Headers("Content-Type: application/json")
    @POST("auth/v1/token")
    suspend fun signIn(
        @Query("grant_type") grantType: String = "password",
        @Body body: SignInRequest
    ): Response<AuthResponse>
}
