package com.ngao.maternalcare.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ngao.maternalcare.ui.theme.BrandBlue
import com.ngao.maternalcare.ui.theme.TextSecondary

private data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userName: String,
    onBack: () -> Unit
) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Hi $userName! I’m NgaoCare Assistant. I can help with pregnancy monitoring, warning signs, check-ins, and care-plan questions.",
                isUser = false
            )
        )
    }
    var input by remember { mutableStateOf("") }

    fun buildReply(prompt: String): String {
        val lower = prompt.lowercase()

        return when {
            lower.contains("bleeding") || lower.contains("severe headache") || lower.contains("blurred vision") || lower.contains("fetal movement") ->
                "These can be warning signs. Please contact your clinic or emergency services immediately if you have heavy bleeding, severe headache, blurred vision, or reduced fetal movement. If you feel unwell, seek urgent care without delay."
            lower.contains("emergency") || lower.contains("panic") || lower.contains("urgent") ->
                "If this is urgent or you feel you are in immediate danger, call emergency services or go to the nearest clinic right away. Your safety and urgent symptoms matter most."
            lower.contains("check-in") || lower.contains("daily") ->
                "Daily check-ins help track blood pressure, heart rate, symptoms, and overall wellbeing. Try to complete one at the same time each day and report any new or worsening symptoms."
            lower.contains("appointment") || lower.contains("visit") || lower.contains("next") ->
                "Keep your prenatal appointments and follow-up visits on schedule. If you’re unsure about the date, check the dashboard or contact your clinic for the exact visit details."
            lower.contains("nutrition") || lower.contains("meal") || lower.contains("food") ->
                "Aim for balanced meals with iron, folate, protein, and hydration. Foods like leafy greens, beans, lean proteins, fruits, and whole grains support a healthy pregnancy."
            lower.contains("rest") || lower.contains("sleep") || lower.contains("stress") ->
                "Rest is important during pregnancy. Try to take breaks, reduce stress when possible, and speak with your care team if you’re feeling unusually tired or overwhelmed."
            else ->
                "I can help with pregnancy health questions, warning signs, nutrition, daily check-ins, and when to seek care. If you are worried about a symptom, contact your clinic promptly."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NgaoCare Assistant") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                messages.forEach { message ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (message.isUser) BrandBlue else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = message.text,
                                color = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask about your care") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val trimmed = input.trim()
                        if (trimmed.isNotEmpty()) {
                            messages.add(ChatMessage(text = trimmed, isUser = true))
                            messages.add(ChatMessage(text = buildReply(trimmed), isUser = false))
                            input = ""
                        }
                    },
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send message")
                }
            }
        }
    }
}
