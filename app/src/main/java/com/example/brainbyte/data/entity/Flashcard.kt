package com.example.brainbyte.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = Deck::class,
            parentColumns = ["id"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["deckId"])]
)
data class Flashcard(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val deckId: String,
    val term: String,
    val definition: String,
    
    // Spaced Repetition / Study Fields
    val easeFactor: Double = 2.5,
    val interval: Int = 0,
    val nextReviewDate: Long = System.currentTimeMillis(),
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

