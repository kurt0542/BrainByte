package com.example.brainbyte.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.brainbyte.data.entity.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySession)

    @Query("SELECT * FROM study_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getStudySessionsByUser(userId: String): Flow<List<StudySession>>
    
    @Query("SELECT * FROM study_sessions WHERE deckId = :deckId ORDER BY startTime DESC")
    fun getStudySessionsByDeck(deckId: String): Flow<List<StudySession>>
}
