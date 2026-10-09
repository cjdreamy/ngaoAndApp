package com.ngao.maternalcare.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------- Auth (Supabase GoTrue) ----------

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val data: SignUpMetadata? = null
)

@Serializable
data class SignUpMetadata(
    @SerialName("full_name") val fullName: String,
    val role: String
)

@Serializable
data class SignInRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
    val user: SupabaseUser? = null,
    // GoTrue returns "msg"/"error_description" style fields on failure
    @SerialName("error_description") val errorDescription: String? = null,
    val msg: String? = null
)

@Serializable
data class SupabaseUser(
    val id: String,
    val email: String? = null
)

// ---------- App domain (mirrors the web app's `profiles`, `pregnancies`,
// `checkins`, `alerts`, `education_content` tables — see README for the SQL) ----------

@Serializable
data class Profile(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    val role: String? = null, // "mother" | "provider"
    val phone: String? = null
)

@Serializable
data class Pregnancy(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("start_date") val startDate: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("next_visit") val nextVisit: String? = null
)

@Serializable
data class CheckIn(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String? = null,
    @SerialName("bp_systolic") val bpSystolic: Int? = null,
    @SerialName("bp_diastolic") val bpDiastolic: Int? = null,
    @SerialName("heart_rate") val heartRate: Int? = null,
    @SerialName("fetal_movement") val fetalMovement: Int? = null,
    val symptoms: List<String> = emptyList(),
    val notes: String? = null,
    @SerialName("risk_level") val riskLevel: String = "normal", // normal | flagged | critical
    val reviewed: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Alert(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("user_name") val userName: String? = null,
    val type: String = "panic", // panic | critical_checkin
    val latitude: Double? = null,
    val longitude: Double? = null,
    val status: String = "active", // active | resolved
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class EducationContent(
    val id: String? = null,
    val title: String,
    val category: String,
    val week: Int,
    val body: String
)
