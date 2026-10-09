package com.ngao.maternalcare.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ngao.maternalcare.data.model.Alert
import com.ngao.maternalcare.data.model.CheckIn
import com.ngao.maternalcare.data.model.Profile
import com.ngao.maternalcare.ui.theme.*

private enum class ProviderTab(val label: String) {
    FLAGGED("Flagged Check-ins"),
    ALERTS("Emergency Alerts"),
    PATIENTS("Patients"),
    ACTIVITY("Recent Activity")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDashboardScreen(
    viewModel: ProviderDashboardViewModel,
    fullName: String,
    onLogout: () -> Unit,
    onOpenEducation: () -> Unit,
    onOpenChat: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(ProviderTab.FLAGGED) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NgaoMaternal Care") },
                actions = {
                    TextButton(onClick = onOpenEducation) { Text("Education") }
                    IconButton(onClick = onOpenChat) {
                        Icon(Icons.Filled.ChatBubbleOutline, contentDescription = "Open chat assistant")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Log out")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (fullName.isNotBlank()) {
                Text("Welcome, $fullName", style = MaterialTheme.typography.titleLarge)
            }
            Text("Monitor patients and respond to alerts", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxWidth()) {
                ProviderStat("Active Alerts", state.activeAlerts.size.toString(), "Require immediate response", Icons.Filled.Warning, DangerRed, Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                ProviderStat("Flagged Check-ins", state.flaggedCheckIns.size.toString(), "Pending review", Icons.Filled.Timeline, WarningAmber, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth()) {
                ProviderStat("Critical Cases", state.criticalCasesCount.toString(), "High-risk patients", Icons.Filled.TrendingUp, DangerRed, Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                ProviderStat("Total Patients", state.patients.size.toString(), "Under monitoring", Icons.Filled.People, BrandBlue, Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 0.dp,
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            ) {
                ProviderTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.label) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                when (selectedTab) {
                    ProviderTab.FLAGGED -> FlaggedCheckInsList(state.flaggedCheckIns) { viewModel.markReviewed(it) }
                    ProviderTab.ALERTS -> EmergencyAlertsList(state.activeAlerts) { viewModel.resolveAlert(it) }
                    ProviderTab.PATIENTS -> PatientsList(state.patients)
                    ProviderTab.ACTIVITY -> RecentActivityList(state.flaggedCheckIns, state.activeAlerts)
                }
            }
        }
    }
}

@Composable
private fun ProviderStat(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FlaggedCheckInsList(items: List<CheckIn>, onMarkReviewed: (String) -> Unit) {
    if (items.isEmpty()) {
        EmptyState("No flagged check-ins right now.")
        return
    }
    Column {
        items.forEach { checkIn ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(checkIn.userName ?: "Patient", style = MaterialTheme.typography.titleMedium)
                            Text(checkIn.createdAt?.take(10) ?: "", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                        val color = if (checkIn.riskLevel == "critical") DangerRed else WarningAmber
                        Box(
                            Modifier
                                .background(color, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(checkIn.riskLevel.uppercase(), color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {
                        checkIn.bpSystolic?.let { Text("BP: $it/${checkIn.bpDiastolic ?: "-"}   ", color = TextSecondary) }
                        checkIn.heartRate?.let { Text("HR: $it bpm   ", color = TextSecondary) }
                        checkIn.fetalMovement?.let { Text("Fetal Movement: $it", color = TextSecondary) }
                    }
                    if (checkIn.symptoms.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Row {
                            checkIn.symptoms.forEach { symptom ->
                                Box(
                                    Modifier
                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) { Text(symptom, style = MaterialTheme.typography.bodyMedium) }
                                Spacer(Modifier.width(6.dp))
                            }
                        }
                    }
                    checkIn.notes?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(it, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row {
                        OutlinedButton(onClick = {}) { Text("View Patient") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { checkIn.id?.let(onMarkReviewed) }) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Mark as Reviewed")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmergencyAlertsList(items: List<Alert>, onResolve: (String) -> Unit) {
    if (items.isEmpty()) {
        EmptyState("No active emergency alerts.")
        return
    }
    Column {
        items.forEach { alert ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(alert.userName ?: "Patient", style = MaterialTheme.typography.titleMedium)
                            Text(alert.type.uppercase(), color = DangerRed, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(alert.createdAt?.take(16)?.replace("T", " ") ?: "", color = TextSecondary)
                    }
                    if (alert.latitude != null && alert.longitude != null) {
                        Spacer(Modifier.height(6.dp))
                        Text("Location: ${alert.latitude}, ${alert.longitude}", color = TextSecondary)
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { alert.id?.let(onResolve) }) { Text("Mark Resolved") }
                }
            }
        }
    }
}

@Composable
private fun PatientsList(patients: List<Profile>) {
    if (patients.isEmpty()) {
        EmptyState("No patients registered yet.")
        return
    }
    Column {
        patients.forEach { patient ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Row(
                    Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(patient.fullName ?: "Patient", style = MaterialTheme.typography.titleMedium)
                        patient.phone?.let { Text(it, color = TextSecondary, style = MaterialTheme.typography.bodyMedium) }
                    }
                    OutlinedButton(onClick = {}) { Text("View") }
                }
            }
        }
    }
}

@Composable
private fun RecentActivityList(checkIns: List<CheckIn>, alerts: List<Alert>) {
    val combinedEmpty = checkIns.isEmpty() && alerts.isEmpty()
    if (combinedEmpty) {
        EmptyState("No recent activity.")
        return
    }
    Column {
        alerts.forEach { alert ->
            ActivityRow(
                title = "${alert.userName ?: "Patient"} triggered a ${alert.type.replace("_", " ")}",
                timestamp = alert.createdAt,
                color = DangerRed
            )
        }
        checkIns.forEach { checkIn ->
            ActivityRow(
                title = "${checkIn.userName ?: "Patient"} submitted a ${checkIn.riskLevel} check-in",
                timestamp = checkIn.createdAt,
                color = if (checkIn.riskLevel == "critical") DangerRed else WarningAmber
            )
        }
    }
}

@Composable
private fun ActivityRow(title: String, timestamp: String?, color: androidx.compose.ui.graphics.Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).background(color, RoundedCornerShape(4.dp)))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(timestamp?.take(16)?.replace("T", " ") ?: "", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, color = TextSecondary)
    }
}
