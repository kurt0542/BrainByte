package com.example.brainbyte.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey
    val userId: String, // Linked to Appwrite Auth UID
    val email: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncAt: Long = 0
)
