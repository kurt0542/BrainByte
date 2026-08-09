package com.example.brainbyte.data.repository

import androidx.room.withTransaction
import com.example.brainbyte.data.BrainByteDatabase
import com.example.brainbyte.data.dao.DeckDao
import com.example.brainbyte.data.dao.FlashcardDao
import com.example.brainbyte.data.entity.Deck
import com.example.brainbyte.data.entity.Flashcard
import com.example.brainbyte.ocr.FlashcardPair
import kotlinx.coroutines.flow.Flow

class FlashcardRepository(
    private val database: BrainByteDatabase
) {
    private val deckDao = database.deckDao()
    private val flashcardDao = database.flashcardDao()

    fun getAllDecks(): Flow<List<Deck>> = deckDao.getAllDecks()

    suspend fun getAllDecksList(): List<Deck> = deckDao.getAllDecksList()

    suspend fun getDeckById(id: String): Deck? = deckDao.getDeckById(id)

    suspend fun getDeckByName(name: String): Deck? = deckDao.getDeckByName(name)

    suspend fun insertDeck(deck: Deck) = deckDao.insertDeck(deck)

    suspend fun updateDeck(deck: Deck) = deckDao.updateDeck(deck)

    suspend fun deleteDeck(deck: Deck) = deckDao.deleteDeck(deck)

    fun getFlashcardsByDeckId(deckId: String): Flow<List<Flashcard>> =
        flashcardDao.getFlashcardsByDeckId(deckId)

    suspend fun getFlashcardsByDeckIdList(deckId: String): List<Flashcard> =
        flashcardDao.getFlashcardsByDeckIdList(deckId)

    suspend fun insertFlashcard(flashcard: Flashcard) =
        flashcardDao.insertFlashcard(flashcard)

    suspend fun insertFlashcards(flashcards: List<Flashcard>) =
        flashcardDao.insertFlashcards(flashcards)

    suspend fun updateFlashcard(flashcard: Flashcard) =
        flashcardDao.updateFlashcard(flashcard)

    suspend fun deleteFlashcard(flashcard: Flashcard) =
        flashcardDao.deleteFlashcard(flashcard)

    suspend fun getFlashcardCountByDeck(deckId: String): Int =
        flashcardDao.getFlashcardCountByDeck(deckId)

    suspend fun createDeckWithFlashcards(
        deckName: String,
        flashcardPairs: List<FlashcardPair>
    ): String {
        return database.withTransaction {
            val deck = Deck(name = deckName)
            deckDao.insertDeck(deck)
            val deckId = deck.id

            val flashcards = flashcardPairs.map { pair ->
                Flashcard(
                    deckId = deckId,
                    term = pair.term,
                    definition = pair.definition
                )
            }
            flashcardDao.insertFlashcards(flashcards)

            deckId
        }
    }

    suspend fun addFlashcardsToDeck(
        deckId: String,
        flashcardPairs: List<FlashcardPair>
    ) {
        database.withTransaction {
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

            flashcardDao.insertFlashcards(flashcards)
        }
    }
}

