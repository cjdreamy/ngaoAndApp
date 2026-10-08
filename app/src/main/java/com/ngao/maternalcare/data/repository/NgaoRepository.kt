package com.ngao.maternalcare.data.repository

import com.ngao.maternalcare.data.model.Alert
import com.ngao.maternalcare.data.model.CheckIn
import com.ngao.maternalcare.data.model.EducationContent
import com.ngao.maternalcare.data.model.Pregnancy
import com.ngao.maternalcare.data.model.Profile
import com.ngao.maternalcare.data.model.SignInRequest
import com.ngao.maternalcare.data.model.SignUpMetadata
import com.ngao.maternalcare.data.model.SignUpRequest
import com.ngao.maternalcare.data.remote.NetworkModule
import com.ngao.maternalcare.data.remote.SessionManager
import com.ngao.maternalcare.data.remote.SupabaseAuthApi
import com.ngao.maternalcare.data.remote.SupabaseRestApi
import com.ngao.maternalcare.util.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NgaoRepository(private val sessionManager: SessionManager) {

    private val retrofit = NetworkModule.buildRetrofit(sessionManager)
    private val authApi = retrofit.create(SupabaseAuthApi::class.java)
    private val restApi = retrofit.create(SupabaseRestApi::class.java)

    // ---------------- AUTH ----------------

    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        role: String
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val resp = authApi.signUp(
                SignUpRequest(email, password, SignUpMetadata(fullName, role))
            )
            val body = resp.body()
            if (!resp.isSuccessful || body?.user == null) {
                return@withContext AppResult.Error(
                    body?.errorDescription ?: body?.msg ?: "Sign up failed (${resp.code()})"
                )
            }

            // Persist a profile row for this user (id must equal auth.users.id)
            restApi.insertProfile(Profile(id = body.user.id, fullName = fullName, role = role))

            // If Supabase requires email confirmation, access_token will be null here.
            if (body.accessToken != null) {
                sessionManager.saveSession(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    userId = body.user.id,
                    email = body.user.email,
                    role = role,
                    fullName = fullName
                )
                if (role == "mother") {
                    restApi.insertPregnancy(Pregnancy(userId = body.user.id))
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Network error during sign up")
        }
    }

    suspend fun signIn(email: String, password: String): AppResult<String> =
        withContext(Dispatchers.IO) {
            try {
                val resp = authApi.signIn(body = SignInRequest(email, password))
                val body = resp.body()
                if (!resp.isSuccessful || body?.accessToken == null || body.user == null) {
                    return@withContext AppResult.Error(
                        body?.errorDescription ?: body?.msg ?: "Invalid email or password"
                    )
                }

                // Look up their profile to know if they're a mother or a provider
                val profileResp = restApi.getProfile("eq.${body.user.id}")
                val profile = profileResp.body()?.firstOrNull()

                val role = profile?.role ?: "mother"
                val fullName = profile?.fullName ?: (body.user.email ?: "User")

                sessionManager.saveSession(
                    accessToken = body.accessToken,
                    refreshToken = body.refreshToken,
                    userId = body.user.id,
                    email = body.user.email,
                    role = role,
                    fullName = fullName
                )
                AppResult.Success(role)
            } catch (e: Exception) {
                AppResult.Error(e.message ?: "Network error during sign in")
            }
        }

    suspend fun signOut() {
        sessionManager.clear()
    }

    // ---------------- MOTHER: DASHBOARD ----------------

    suspend fun getPregnancy(userId: String): Pregnancy? = withContext(Dispatchers.IO) {
        try {
            restApi.getPregnancy("eq.$userId").body()?.firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMyCheckIns(userId: String): List<CheckIn> = withContext(Dispatchers.IO) {
        try {
            restApi.getCheckIns(
                mapOf(
                    "user_id" to "eq.$userId",
                    "order" to "created_at.desc"
                )
            ).body() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMyAlerts(userId: String): List<Alert> = withContext(Dispatchers.IO) {
        try {
            restApi.getAlerts(
                mapOf(
                    "user_id" to "eq.$userId",
                    "status" to "eq.active",
                    "order" to "created_at.desc"
                )
            ).body() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Symptoms that automatically mark a check-in (and raise an alert) as CRITICAL. */
    private val criticalSymptoms = setOf(
        "Vaginal bleeding", "Severe headache", "Severe abdominal pain",
        "Blurred vision", "Convulsions", "Reduced fetal movement"
    )

    suspend fun submitCheckIn(
        userId: String,
        userName: String,
        bpSystolic: Int?,
        bpDiastolic: Int?,
        heartRate: Int?,
        fetalMovement: Int?,
        symptoms: List<String>,
        notes: String
    ): AppResult<CheckIn> = withContext(Dispatchers.IO) {
        try {
            val isCritical = symptoms.any { it in criticalSymptoms }
            val riskLevel = if (isCritical) "critical" else if (symptoms.isNotEmpty()) "flagged" else "normal"

            val checkIn = CheckIn(
                userId = userId,
                userName = userName,
                bpSystolic = bpSystolic,
                bpDiastolic = bpDiastolic,
                heartRate = heartRate,
                fetalMovement = fetalMovement,
                symptoms = symptoms,
                notes = notes.ifBlank { null },
                riskLevel = riskLevel
            )
            val resp = restApi.insertCheckIn(checkIn)
            val saved = resp.body()?.firstOrNull()
                ?: return@withContext AppResult.Error("Could not save check-in (${resp.code()})")

            if (isCritical) {
                restApi.insertAlert(
                    Alert(userId = userId, userName = userName, type = "critical_checkin")
                )
            }
            AppResult.Success(saved)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Network error while submitting check-in")
        }
    }

    suspend fun triggerPanicAlert(
        userId: String,
        userName: String,
        latitude: Double?,
        longitude: Double?
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            restApi.insertAlert(
                Alert(
                    userId = userId,
                    userName = userName,
                    type = "panic",
                    latitude = latitude,
                    longitude = longitude
                )
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Could not send emergency alert. Check your connection.")
        }
    }

    // ---------------- PROVIDER: DASHBOARD ----------------

    suspend fun getFlaggedCheckIns(): List<CheckIn> = withContext(Dispatchers.IO) {
        try {
            restApi.getCheckIns(
                mapOf(
                    "risk_level" to "in.(flagged,critical)",
                    "reviewed" to "eq.false",
                    "order" to "created_at.desc"
                )
            ).body() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getActiveAlerts(): List<Alert> = withContext(Dispatchers.IO) {
        try {
            restApi.getAlerts(mapOf("status" to "eq.active", "order" to "created_at.desc")).body()
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllPatients(): List<Profile> = withContext(Dispatchers.IO) {
        try {
            restApi.getProfiles(mapOf("role" to "eq.mother")).body() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun markCheckInReviewed(id: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            restApi.updateCheckIn("eq.$id", mapOf("reviewed" to true))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Could not update check-in")
        }
    }

    suspend fun resolveAlert(id: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            restApi.updateAlertStatus("eq.$id", mapOf("status" to "resolved"))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Could not update alert")
        }
    }

    // ---------------- EDUCATION ----------------

    suspend fun getEducationContent(category: String?): List<EducationContent> =
        withContext(Dispatchers.IO) {
            try {
                val filters = mutableMapOf("order" to "week.asc")
                if (category != null && category != "All") filters["category"] = "eq.$category"
                restApi.getEducationContent(filters).body() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
}
