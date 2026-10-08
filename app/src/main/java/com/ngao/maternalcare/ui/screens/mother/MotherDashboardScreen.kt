package com.ngao.maternalcare.ui.screens.mother

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.ngao.maternalcare.data.model.CheckIn
import com.ngao.maternalcare.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotherDashboardScreen(
    viewModel: MotherDashboardViewModel,
    onStartCheckIn: () -> Unit,
    onOpenEducation: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPanicConfirm by remember { mutableStateOf(false) }

    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        sendPanicWithLocation(context, granted, viewModel)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("NgaoMaternal Care") },
            actions = {
                IconButton(onClick = onLogout) {
                    Icon(Icons.Filled.Logout, contentDescription = "Log out")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Welcome, ${state.fullName}", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Monitor your health and stay connected with your care team",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Pregnancy Week",
                    value = state.pregnancyWeek.toString(),
                    subtitle = "${state.weeksRemaining} weeks remaining",
                    icon = Icons.Filled.Favorite,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
                StatCard(
                    title = "Recent Check-ins",
                    value = state.checkInsLast7Days.toString(),
                    subtitle = "Last 7 days",
                    icon = Icons.Filled.Timeline,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth()) {
                StatCard(
                    title = "Next Visit",
                    value = state.nextVisit,
                    subtitle = "Clinic appointment",
                    icon = Icons.Filled.CalendarMonth,
                    modifier = Modifier.weight(1f),
                    valueIsSmall = true
                )
                Spacer(Modifier.width(10.dp))
                StatCard(
                    title = "Active Alerts",
                    value = state.activeAlerts.toString(),
                    subtitle = "Emergency alerts",
                    icon = Icons.Filled.NotificationsActive,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth()) {
                ActionCard(
                    title = "Daily Health Check-in",
                    description = "Complete your daily health assessment to help us monitor your wellbeing",
                    buttonText = "Start Check-in",
                    buttonIcon = Icons.Filled.MonitorHeart,
                    buttonColor = BrandBlue,
                    onClick = onStartCheckIn,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Emergency Alert", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Press the panic button if you need immediate medical assistance",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { showPanicConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (state.panicSending) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Warning, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("EMERGENCY PANIC BUTTON")
                        }
                    }
                }
            }

            state.errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            if (state.panicSent) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Emergency alert sent to the nearest clinic with your location.",
                    color = SuccessGreen
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Recent Health Check-ins", style = MaterialTheme.typography.titleMedium)
            Text("Your health monitoring history", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))

            if (state.recentCheckIns.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No check-ins yet. Complete your first health check-in to start monitoring.",
                        color = TextSecondary
                    )
                }
            } else {
                state.recentCheckIns.forEach { checkIn -> CheckInRow(checkIn) }
            }

            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableCard { onOpenEducation() }
            ) {
                Row(
                    Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.MenuBook, contentDescription = null, tint = Teal)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Educational Resources", style = MaterialTheme.typography.titleMedium)
                        Text("Learn about pregnancy health and warning signs", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showPanicConfirm) {
        AlertDialog(
            onDismissRequest = { showPanicConfirm = false },
            title = { Text("Send emergency alert?") },
            text = { Text("This will immediately notify the nearest clinic with your current location.") },
            confirmButton = {
                TextButton(onClick = {
                    showPanicConfirm = false
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        sendPanicWithLocation(context, true, viewModel)
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }) { Text("Send Alert", color = DangerRed) }
            },
            dismissButton = {
                TextButton(onClick = { showPanicConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@SuppressLint("MissingPermission")
private fun sendPanicWithLocation(
    context: android.content.Context,
    hasPermission: Boolean,
    viewModel: MotherDashboardViewModel
) {
    if (!hasPermission) {
        viewModel.triggerPanic(null, null)
        return
    }
    try {
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.lastLocation.addOnSuccessListener { location ->
            viewModel.triggerPanic(location?.latitude, location?.longitude)
        }.addOnFailureListener {
            viewModel.triggerPanic(null, null)
        }
    } catch (e: Exception) {
        viewModel.triggerPanic(null, null)
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    valueIsSmall: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                style = if (valueIsSmall) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineMedium
            )
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    description: String,
    buttonText: String,
    buttonIcon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(buttonIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(buttonText)
            }
        }
    }
}

@Composable
private fun CheckInRow(checkIn: CheckIn) {
    val (badgeColor, badgeText) = when (checkIn.riskLevel) {
        "critical" -> DangerRed to "CRITICAL"
        "flagged" -> WarningAmber to "FLAGGED"
        else -> SuccessGreen to "NORMAL"
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(checkIn.createdAt?.take(10) ?: "—", style = MaterialTheme.typography.bodyMedium)
                Box(
                    Modifier
                        .background(badgeColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(badgeText, color = badgeColor, style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row {
                checkIn.bpSystolic?.let { sys ->
                    Text("BP: $sys/${checkIn.bpDiastolic ?: "-"}  ", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
                checkIn.heartRate?.let {
                    Text("HR: $it bpm  ", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
                checkIn.fetalMovement?.let {
                    Text("Fetal Movement: $it", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (checkIn.symptoms.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(checkIn.symptoms.joinToString(", "), color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun Modifier.clickableCard(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
