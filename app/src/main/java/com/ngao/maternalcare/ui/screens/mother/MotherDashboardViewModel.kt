package com.ngao.maternalcare.ui.screens.mother

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngao.maternalcare.data.model.Alert
import com.ngao.maternalcare.data.model.CheckIn
import com.ngao.maternalcare.data.model.Pregnancy
import com.ngao.maternalcare.data.remote.SessionManager
import com.ngao.maternalcare.data.repository.NgaoRepository
import com.ngao.maternalcare.util.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class MotherDashboardState(
    val isLoading: Boolean = true,
    val fullName: String = "",
    val pregnancyWeek: Int = 0,
    val weeksRemaining: Int = 40,
    val checkInsLast7Days: Int = 0,
    val nextVisit: String = "Not set",
    val activeAlerts: Int = 0,
    val recentCheckIns: List<CheckIn> = emptyList(),
    val panicSending: Boolean = false,
    val panicSent: Boolean = false,
    val errorMessage: String? = null
)

class MotherDashboardViewModel(
    private val repository: NgaoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(MotherDashboardState())
    val state: StateFlow<MotherDashboardState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val userId = sessionManager.currentUserId() ?: return@launch
            val fullName = sessionManager.currentFullName() ?: ""

            val pregnancy = repository.getPregnancy(userId)
            val checkIns = repository.getMyCheckIns(userId)
            val alerts = repository.getMyAlerts(userId)

            val week = computeWeek(pregnancy)

            _state.value = _state.value.copy(
                isLoading = false,
                fullName = fullName,
                pregnancyWeek = week,
                weeksRemaining = (40 - week).coerceIn(0, 40),
                checkInsLast7Days = checkIns.count { isWithinDays(it.createdAt, 7) },
                nextVisit = pregnancy?.nextVisit ?: "Not set",
                activeAlerts = alerts.size,
                recentCheckIns = checkIns.take(5)
            )
        }
    }

    fun submitCheckIn(
        bpSystolic: Int?,
        bpDiastolic: Int?,
        heartRate: Int?,
        fetalMovement: Int?,
        symptoms: List<String>,
        notes: String,
        onDone: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val userId = sessionManager.currentUserId() ?: return@launch
            val name = sessionManager.currentFullName() ?: "Patient"
            when (val result = repository.submitCheckIn(
                userId, name, bpSystolic, bpDiastolic, heartRate, fetalMovement, symptoms, notes
            )) {
                is AppResult.Success -> {
                    load()
                    onDone(true, null)
                }
                is AppResult.Error -> onDone(false, result.message)
            }
        }
    }

    fun triggerPanic(latitude: Double?, longitude: Double?) {
        viewModelScope.launch {
            val userId = sessionManager.currentUserId() ?: return@launch
            val name = sessionManager.currentFullName() ?: "Patient"
            _state.value = _state.value.copy(panicSending = true, errorMessage = null)
            when (val result = repository.triggerPanicAlert(userId, name, latitude, longitude)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(panicSending = false, panicSent = true)
                    load()
                }
                is AppResult.Error -> _state.value =
                    _state.value.copy(panicSending = false, errorMessage = result.message)
            }
        }
    }

    fun dismissPanicSent() {
        _state.value = _state.value.copy(panicSent = false)
    }

    private fun computeWeek(pregnancy: Pregnancy?): Int {
        val dueDateStr = pregnancy?.dueDate ?: return 0
        return try {
            val due = LocalDate.parse(dueDateStr, DateTimeFormatter.ISO_DATE)
            val today = LocalDate.now()
            val daysUntilDue = ChronoUnit.DAYS.between(today, due)
            val week = 40 - (daysUntilDue / 7)
            week.toInt().coerceIn(0, 42)
        } catch (e: Exception) {
            0
        }
    }

    private fun isWithinDays(createdAt: String?, days: Int): Boolean {
        if (createdAt == null) return false
        return try {
            val date = java.time.OffsetDateTime.parse(createdAt).toLocalDate()
            ChronoUnit.DAYS.between(date, LocalDate.now()) <= days
        } catch (e: Exception) {
            false
        }
    }
}
