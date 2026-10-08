package com.ngao.maternalcare.data.remote

import com.ngao.maternalcare.data.model.Alert
import com.ngao.maternalcare.data.model.CheckIn
import com.ngao.maternalcare.data.model.EducationContent
import com.ngao.maternalcare.data.model.Pregnancy
import com.ngao.maternalcare.data.model.Profile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * Thin wrapper around Supabase's auto-generated REST API (PostgREST).
 * See README.md for the exact SQL used to create these tables + RLS policies.
 */
interface SupabaseRestApi {

    // ---------- profiles ----------

    @GET("rest/v1/profiles")
    suspend fun getProfile(@Query("id") idFilter: String): Response<List<Profile>>

    @GET("rest/v1/profiles")
    suspend fun getProfiles(@QueryMap filters: Map<String, String>): Response<List<Profile>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("rest/v1/profiles")
    suspend fun insertProfile(@Body body: Profile): Response<List<Profile>>

    // ---------- pregnancies ----------

    @GET("rest/v1/pregnancies")
    suspend fun getPregnancy(@Query("user_id") userIdFilter: String): Response<List<Pregnancy>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("rest/v1/pregnancies")
    suspend fun insertPregnancy(@Body body: Pregnancy): Response<List<Pregnancy>>

    // ---------- checkins ----------

    @GET("rest/v1/checkins")
    suspend fun getCheckIns(@QueryMap filters: Map<String, String>): Response<List<CheckIn>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("rest/v1/checkins")
    suspend fun insertCheckIn(@Body body: CheckIn): Response<List<CheckIn>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("rest/v1/checkins")
    suspend fun updateCheckIn(
        @Query("id") idFilter: String,
        @Body body: Map<String, Boolean>
    ): Response<List<CheckIn>>

    // ---------- alerts ----------

    @GET("rest/v1/alerts")
    suspend fun getAlerts(@QueryMap filters: Map<String, String>): Response<List<Alert>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("rest/v1/alerts")
    suspend fun insertAlert(@Body body: Alert): Response<List<Alert>>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("rest/v1/alerts")
    suspend fun updateAlertStatus(
        @Query("id") idFilter: String,
        @Body body: Map<String, String>
    ): Response<List<Alert>>

    // ---------- education_content ----------

    @GET("rest/v1/education_content")
    suspend fun getEducationContent(@QueryMap filters: Map<String, String>): Response<List<EducationContent>>
}
