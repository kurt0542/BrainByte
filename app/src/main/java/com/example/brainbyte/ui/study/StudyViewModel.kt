package com.example.brainbyte.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbyte.data.entity.Flashcard
import com.example.brainbyte.data.entity.StudySession
import com.example.brainbyte.data.repository.FlashcardRepository
import com.example.brainbyte.data.repository.StudySessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StudyUiState {
    object Loading : StudyUiState()
    data class Study(
        val currentCard: Flashcard,
        val currentIndex: Int,
        val totalCards: Int,
        val correctCount: Int
    ) : StudyUiState()
    data class Completed(val score: Int, val totalCards: Int) : StudyUiState()
    data class Error(val message: String) : StudyUiState()
}

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val flashcardRepository: FlashcardRepository,
    private val studySessionRepository: StudySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudyUiState>(StudyUiState.Loading)
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    private var flashcards: List<Flashcard> = emptyList()
    private var currentIndex = 0
    private var correctCount = 0
    private var deckId: String = ""

    fun startStudySession(deckId: String) {
        this.deckId = deckId
        viewModelScope.launch {
            _uiState.value = StudyUiState.Loading
            try {
                flashcards = flashcardRepository.getFlashcardsByDeckIdList(deckId)
                if (flashcards.isEmpty()) {
                    _uiState.value = StudyUiState.Error("No flashcards found in this deck")
                } else {
                    currentIndex = 0
                    correctCount = 0
                    emitCurrentState()
                }
            } catch (e: Exception) {
                _uiState.value = StudyUiState.Error(e.message ?: "Failed to start study session")
            }
        }
    }

    fun answerCard(isCorrect: Boolean) {
        if (isCorrect) correctCount++
        currentIndex++
        
        if (currentIndex < flashcards.size) {
            emitCurrentState()
        } else {
            finishStudySession()
        }
    }

    private fun emitCurrentState() {
        _uiState.value = StudyUiState.Study(
            currentCard = flashcards[currentIndex],
            currentIndex = currentIndex + 1,
            totalCards = flashcards.size,
            correctCount = correctCount
        )
    }

    private fun finishStudySession() {
        _uiState.value = StudyUiState.Completed(correctCount, flashcards.size)
        viewModelScope.launch {
            try {
                val session = StudySession(
                    userId = "current", // Ideally fetched from Auth
                    deckId = deckId,
                    startTime = System.currentTimeMillis() - 60000, // mock duration
                    endTime = System.currentTimeMillis(),
                    totalCards = flashcards.size,
                    correctCount = correctCount,
                    incorrectCount = flashcards.size - correctCount
                )
                studySessionRepository.insertStudySession(session)
            } catch (e: Exception) {
                // Log failure to save session
            }
        }
    }
}
