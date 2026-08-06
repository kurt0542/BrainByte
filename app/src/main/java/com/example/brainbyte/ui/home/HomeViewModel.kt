package com.example.brainbyte.ui.home

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

data class HomeUiState(
    val totalDecks: Int = 0,
    val totalCards: Int = 0,
    val recentDeck: Deck? = null,
    val recentDeckCardCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FlashcardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllDecks().collectLatest { decks ->
                var totalCards = 0
                for (deck in decks) {
                    totalCards += repository.getFlashcardCountByDeck(deck.id)
                }

                val recentDeck = if (decks.isNotEmpty()) decks.maxByOrNull { it.updatedAt } else null
                val recentDeckCount = recentDeck?.let { repository.getFlashcardCountByDeck(it.id) } ?: 0

                _uiState.value = HomeUiState(
                    totalDecks = decks.size,
                    totalCards = totalCards,
                    recentDeck = recentDeck,
                    recentDeckCardCount = recentDeckCount,
                    isLoading = false
                )
            }
        }
    }
}
