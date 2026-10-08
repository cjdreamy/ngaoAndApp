package com.ngao.maternalcare.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngao.maternalcare.ui.theme.BrandBlue
import com.ngao.maternalcare.ui.theme.TextSecondary
import com.ngao.maternalcare.util.clickableNoRipple

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onSignUpSuccess: (role: String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("mother") }

    LaunchedEffect(uiState.loggedInRole) {
        uiState.loggedInRole?.let {
            if (it == "PENDING_CONFIRMATION") {
                onNavigateToLogin()
            } else {
                onSignUpSuccess(it)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Create Your Account", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            "Join NgaoMaternal Care for safer pregnancies",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))

        Text("I am a...", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            RoleOption(
                label = "Expectant Mother",
                selected = role == "mother",
                modifier = Modifier.weight(1f)
            ) { role = "mother" }
            Spacer(Modifier.width(10.dp))
            RoleOption(
                label = "Healthcare Provider",
                selected = role == "provider",
                modifier = Modifier.weight(1f)
            ) { role = "provider" }
        }

        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        uiState.errorMessage?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(22.dp))
        Button(
            onClick = { viewModel.signUp(fullName, email, password, role) },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Sign Up")
            }
        }

        Spacer(Modifier.height(16.dp))
        Row {
            Text("Already have an account? ", color = TextSecondary)
            Text("Log in", color = BrandBlue, modifier = Modifier.clickableNoRipple { onNavigateToLogin() })
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RoleOption(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    Card(
        modifier = modifier
            .selectable(selected = selected, onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) BrandBlue.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, if (selected) BrandBlue else MaterialTheme.colorScheme.outline
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp), contentAlignment = Alignment.Center) {
            Text(label, textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
