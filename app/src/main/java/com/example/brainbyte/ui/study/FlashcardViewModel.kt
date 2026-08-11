package com.example.brainbyte.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbyte.data.entity.Flashcard
import com.example.brainbyte.data.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FlashcardUiState {
    object Loading : FlashcardUiState()
    data class Success(val flashcards: List<Flashcard>) : FlashcardUiState()
    data class Error(val message: String) : FlashcardUiState()
}

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val repository: FlashcardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FlashcardUiState>(FlashcardUiState.Loading)
    val uiState: StateFlow<FlashcardUiState> = _uiState.asStateFlow()

    fun loadFlashcards(deckId: String) {
        viewModelScope.launch {
            try {
                repository.getFlashcardsByDeckId(deckId).collectLatest { cards ->
                    _uiState.value = FlashcardUiState.Success(cards)
                }
            } catch (e: Exception) {
                _uiState.value = FlashcardUiState.Error(e.message ?: "Failed to load flashcards")
            }
        }
    }

    fun addFlashcard(deckId: String, term: String, definition: String) {
        viewModelScope.launch {
            val card = Flashcard(deckId = deckId, term = term, definition = definition)
            repository.insertFlashcard(card)
        }
    }

    fun deleteFlashcard(flashcard: Flashcard) {
        viewModelScope.launch {
            repository.deleteFlashcard(flashcard)
        }
    }
}
