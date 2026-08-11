package com.example.brainbyte.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbyte.data.entity.StudySession
import com.example.brainbyte.data.repository.AuthRepository
import com.example.brainbyte.data.repository.StudySessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val accuracyPercentage: Int = 0,
    val history: List<StudySession> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val studySessionRepository: StudySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val user = authRepository.getCurrentUser()
                studySessionRepository.getStudySessionsByUser(user.id).collect { sessions ->
                    val history = sessions.sortedByDescending { it.startTime }
                    
                    var totalCorrect = 0
                    var totalCards = 0
                    
                    for (session in sessions) {
                        totalCorrect += session.correctCount
                        totalCards += session.totalCards
                    }
                    
                    val accuracy = if (totalCards > 0) {
                        ((totalCorrect.toFloat() / totalCards.toFloat()) * 100).toInt()
                    } else {
                        0
                    }
                    
                    _uiState.value = AnalyticsUiState(
                        isLoading = false,
                        accuracyPercentage = accuracy,
                        history = history,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load analytics"
                )
            }
        }
    }
}
