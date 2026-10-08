package com.ngao.maternalcare.ui.screens.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ngao.maternalcare.data.remote.SessionManager

@Composable
fun SplashScreen(
    sessionManager: SessionManager,
    onResolved: (route: String) -> Unit
) {
    LaunchedEffect(Unit) {
        val token = sessionManager.currentAccessToken()
        val role = sessionManager.currentRole()
        if (token != null && role != null) {
            onResolved(if (role == "provider") "provider_dashboard" else "mother_dashboard")
        } else {
            onResolved("login")
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
