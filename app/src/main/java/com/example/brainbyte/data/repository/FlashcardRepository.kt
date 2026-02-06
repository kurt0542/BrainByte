package com.example.brainbyte.data.repository

import com.example.brainbyte.data.dao.DeckDao
import com.example.brainbyte.data.dao.FlashcardDao
import com.example.brainbyte.data.entity.Deck
import com.example.brainbyte.data.entity.Flashcard
import com.example.brainbyte.ocr.FlashcardPair
import kotlinx.coroutines.flow.Flow

class FlashcardRepository(
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao
) {
    fun getAllDecks(): Flow<List<Deck>> = deckDao.getAllDecks()

    suspend fun getAllDecksList(): List<Deck> = deckDao.getAllDecksList()

    suspend fun getDeckById(id: Long): Deck? = deckDao.getDeckById(id)

    suspend fun getDeckByName(name: String): Deck? = deckDao.getDeckByName(name)

    suspend fun insertDeck(deck: Deck): Long = deckDao.insertDeck(deck)

    suspend fun updateDeck(deck: Deck) = deckDao.updateDeck(deck)

    suspend fun deleteDeck(deck: Deck) = deckDao.deleteDeck(deck)

    fun getFlashcardsByDeckId(deckId: Long): Flow<List<Flashcard>> =
        flashcardDao.getFlashcardsByDeckId(deckId)

    suspend fun getFlashcardsByDeckIdList(deckId: Long): List<Flashcard> =
        flashcardDao.getFlashcardsByDeckIdList(deckId)

    suspend fun insertFlashcard(flashcard: Flashcard): Long =
        flashcardDao.insertFlashcard(flashcard)

    suspend fun insertFlashcards(flashcards: List<Flashcard>): List<Long> =
        flashcardDao.insertFlashcards(flashcards)

    suspend fun updateFlashcard(flashcard: Flashcard) =
        flashcardDao.updateFlashcard(flashcard)

    suspend fun deleteFlashcard(flashcard: Flashcard) =
        flashcardDao.deleteFlashcard(flashcard)

    suspend fun getFlashcardCountByDeck(deckId: Long): Int =
        flashcardDao.getFlashcardCountByDeck(deckId)
    suspend fun createDeckWithFlashcards(
        deckName: String,
        flashcardPairs: List<FlashcardPair>
    ): Long {
        val deck = Deck(name = deckName)
        val deckId = deckDao.insertDeck(deck)

        val flashcards = flashcardPairs.map { pair ->
            Flashcard(
                deckId = deckId,
                term = pair.term,
                definition = pair.definition
            )
        }
        flashcardDao.insertFlashcards(flashcards)

        return deckId
    }

    suspend fun addFlashcardsToDeck(
        deckId: Long,
        flashcardPairs: List<FlashcardPair>
    ): List<Long> {
        val flashcards = flashcardPairs.map { pair ->
            Flashcard(
                deckId = deckId,
                term = pair.term,
                definition = pair.definition
            )
        }

        deckDao.getDeckById(deckId)?.let { deck ->
            deckDao.updateDeck(deck.copy(updatedAt = System.currentTimeMillis()))
        }

        return flashcardDao.insertFlashcards(flashcards)
    }
}

