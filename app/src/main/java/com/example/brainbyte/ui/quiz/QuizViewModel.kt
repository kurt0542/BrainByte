package com.example.brainbyte.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbyte.data.entity.QuizQuestion
import com.example.brainbyte.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QuizUiState {
    object Loading : QuizUiState()
    data class Active(
        val currentQuestion: QuizQuestion,
        val currentIndex: Int,
        val totalQuestions: Int,
        val correctAnswers: Int,
        val isAnswered: Boolean,
        val selectedOptionIndex: Int?
    ) : QuizUiState()
    data class Completed(val score: Int, val totalQuestions: Int) : QuizUiState()
    data class Error(val message: String) : QuizUiState()
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var questions: List<QuizQuestion> = emptyList()
    private var currentIndex = 0
    private var correctCount = 0

    fun startQuiz(deckId: String) {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            try {
                questions = quizRepository.generateQuiz(deckId)
                if (questions.isEmpty()) {
                    _uiState.value = QuizUiState.Error("Could not generate quiz questions.")
                } else {
                    currentIndex = 0
                    correctCount = 0
                    emitActiveState(isAnswered = false, selectedOptionIndex = null)
                }
            } catch (e: Exception) {
                _uiState.value = QuizUiState.Error(e.message ?: "Failed to start quiz.")
            }
        }
    }

    fun submitAnswer(optionIndex: Int) {
        val currentState = _uiState.value
        if (currentState !is QuizUiState.Active || currentState.isAnswered) return

        val question = questions[currentIndex]
        if (optionIndex == question.correctOptionIndex) {
            correctCount++
        }
        
        emitActiveState(isAnswered = true, selectedOptionIndex = optionIndex)
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        if (currentState !is QuizUiState.Active || !currentState.isAnswered) return

        currentIndex++
        if (currentIndex < questions.size) {
            emitActiveState(isAnswered = false, selectedOptionIndex = null)
        } else {
            _uiState.value = QuizUiState.Completed(correctCount, questions.size)
        }
    }

    private fun emitActiveState(isAnswered: Boolean, selectedOptionIndex: Int?) {
        _uiState.value = QuizUiState.Active(
            currentQuestion = questions[currentIndex],
            currentIndex = currentIndex + 1,
            totalQuestions = questions.size,
            correctAnswers = correctCount,
            isAnswered = isAnswered,
            selectedOptionIndex = selectedOptionIndex
        )
    }
}
