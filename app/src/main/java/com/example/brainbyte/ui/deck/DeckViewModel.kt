package com.example.brainbyte.ui.deck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.brainbyte.data.entity.Deck
import com.example.brainbyte.data.repository.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DeckUiState {
    object Loading : DeckUiState()
    data class Success(val decks: List<Deck>) : DeckUiState()
    data class Error(val message: String) : DeckUiState()
}

@HiltViewModel
class DeckViewModel @Inject constructor(
    private val repository: FlashcardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeckUiState>(DeckUiState.Loading)
    val uiState: StateFlow<DeckUiState> = _uiState.asStateFlow()

    init {
        loadDecks()
    }

    private fun loadDecks() {
        viewModelScope.launch {
            try {
                repository.getAllDecks().collectLatest { decks ->
                    _uiState.value = DeckUiState.Success(decks)
                }
            } catch (e: Exception) {
                _uiState.value = DeckUiState.Error(e.message ?: "Failed to load decks")
            }
        }
    }

    fun addDeck(name: String) {
        viewModelScope.launch {
            val deck = Deck(name = name)
            repository.insertDeck(deck)
        }
    }

    fun deleteDeck(deck: Deck) {
        viewModelScope.launch {
            repository.deleteDeck(deck)
        }
    }
}
