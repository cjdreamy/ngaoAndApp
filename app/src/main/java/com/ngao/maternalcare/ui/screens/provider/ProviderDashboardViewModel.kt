package com.ngao.maternalcare.ui.screens.provider

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngao.maternalcare.data.model.Alert
import com.ngao.maternalcare.data.model.CheckIn
import com.ngao.maternalcare.data.model.Profile
import com.ngao.maternalcare.data.remote.SessionManager
import com.ngao.maternalcare.data.repository.NgaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProviderDashboardState(
    val isLoading: Boolean = true,
    val flaggedCheckIns: List<CheckIn> = emptyList(),
    val activeAlerts: List<Alert> = emptyList(),
    val patients: List<Profile> = emptyList(),
    val criticalCasesCount: Int = 0
)

class ProviderDashboardViewModel(
    private val repository: NgaoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ProviderDashboardState())
    val state: StateFlow<ProviderDashboardState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val flagged = repository.getFlaggedCheckIns()
            val alerts = repository.getActiveAlerts()
            val patients = repository.getAllPatients()

            _state.value = ProviderDashboardState(
                isLoading = false,
                flaggedCheckIns = flagged,
                activeAlerts = alerts,
                patients = patients,
                criticalCasesCount = flagged.count { it.riskLevel == "critical" }
            )
        }
    }

    fun markReviewed(checkInId: String) {
        viewModelScope.launch {
            repository.markCheckInReviewed(checkInId)
            load()
        }
    }

    fun resolveAlert(alertId: String) {
        viewModelScope.launch {
            repository.resolveAlert(alertId)
            load()
        }
    }
}
