package com.ngao.maternalcare.ui.screens.education

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ngao.maternalcare.data.model.EducationContent
import com.ngao.maternalcare.data.remote.SessionManager
import com.ngao.maternalcare.data.repository.NgaoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

val educationCategories = listOf("All", "General", "Safety", "Nutrition", "Health", "Monitoring", "Preparation")

/** Fallback content shown if the `education_content` table is empty (e.g. fresh Supabase project). */
val fallbackEducationContent = listOf(
    EducationContent(title = "Welcome to Your Pregnancy Journey", category = "General", week = 1,
        body = "Congratulations on your pregnancy! This is an exciting time. Regular check-ins and monitoring are essential for you and your baby's health. Remember to attend all clinic appointments and report any unusual symptoms immediately."),
    EducationContent(title = "Understanding Warning Signs", category = "Safety", week = 4,
        body = "Important warning signs to watch for: severe headaches, blurred vision, severe abdominal pain, reduced fetal movement, vaginal bleeding, or sudden swelling. If you experience any of these, use the emergency alert immediately."),
    EducationContent(title = "Nutrition During Pregnancy", category = "Nutrition", week = 8,
        body = "Eating well is crucial for your baby's development. Focus on: iron-rich foods (spinach, beans), calcium (milk, yogurt), proteins (eggs, fish, meat), and plenty of fruits and vegetables. Drink at least 8 glasses of water daily."),
    EducationContent(title = "Blood Pressure Awareness", category = "Health", week = 20,
        body = "High blood pressure during pregnancy can be dangerous. Symptoms include: severe headaches, vision changes, upper abdominal pain. Monitor your blood pressure regularly and report any concerns."),
    EducationContent(title = "Fetal Movement Monitoring", category = "Monitoring", week = 28,
        body = "From week 28 onwards, monitor your baby's movements daily. You should feel at least 10 movements in 2 hours. If movements decrease significantly, contact your healthcare provider immediately."),
    EducationContent(title = "Preparing for Labor", category = "Preparation", week = 36,
        body = "As you approach your due date, prepare your hospital bag, know the route to your clinic, and ensure your emergency contacts are updated in the system. Practice breathing exercises and stay calm.")
)

data class EducationUiState(
    val selectedCategory: String = "All",
    val isLoading: Boolean = true,
    val content: List<EducationContent> = emptyList()
)

class EducationViewModel(
    private val repository: NgaoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EducationUiState())
    val uiState: StateFlow<EducationUiState> = _uiState.asStateFlow()

    init {
        load("All")
    }

    fun selectCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        load(category)
    }

    private fun load(category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val remote = repository.getEducationContent(category)
            val source = remote.ifEmpty {
                if (category == "All") fallbackEducationContent
                else fallbackEducationContent.filter { it.category == category }
            }
            _uiState.value = _uiState.value.copy(isLoading = false, content = source)
        }
    }
}
