package com.ngao.maternalcare.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngao.maternalcare.data.remote.SessionManager
import com.ngao.maternalcare.data.repository.NgaoRepository
import com.ngao.maternalcare.util.AppResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedInRole: String? = null
)

class AuthViewModel(
    private val repository: NgaoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter your email and password")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.signIn(email.trim(), password)) {
                is AppResult.Success -> _uiState.value =
                    AuthUiState(isLoading = false, loggedInRole = result.data)
                is AppResult.Error -> _uiState.value =
                    _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    fun signUp(fullName: String, email: String, password: String, role: String) {
        if (fullName.isBlank() || email.isBlank() || password.length < 6) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Fill in all fields (password must be 6+ characters)"
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = repository.signUp(email.trim(), password, fullName.trim(), role)) {
                is AppResult.Success -> {
                    // If Supabase has email confirmation ON, there's no session yet —
                    // send the user to sign in instead of the dashboard.
                    val role = sessionManager.currentRole()
                    _uiState.value = AuthUiState(isLoading = false, loggedInRole = role ?: "PENDING_CONFIRMATION")
                }
                is AppResult.Error -> _uiState.value =
                    _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
