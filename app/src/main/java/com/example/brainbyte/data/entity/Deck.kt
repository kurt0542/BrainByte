package com.example.brainbyte.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "decks")
data class Deck(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "", // Owner of the deck
    val name: String,
    val description: String? = null,
    val isPublic: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

