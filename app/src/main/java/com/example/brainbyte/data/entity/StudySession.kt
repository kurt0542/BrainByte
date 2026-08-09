package com.example.brainbyte.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val deckId: String,
    val userId: String,
    val startTime: Long,
    val endTime: Long,
    val totalCards: Int,
    val correctCount: Int,
    val incorrectCount: Int
)
