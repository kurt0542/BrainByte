package com.example.brainbyte.data.repository

import com.example.brainbyte.data.dao.StudySessionDao
import com.example.brainbyte.data.entity.StudySession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StudySessionRepository @Inject constructor(
    private val studySessionDao: StudySessionDao
) {
    fun getStudySessionsByUser(userId: String): Flow<List<StudySession>> =
        studySessionDao.getStudySessionsByUser(userId)

    fun getStudySessionsByDeck(deckId: String): Flow<List<StudySession>> =
        studySessionDao.getStudySessionsByDeck(deckId)

    suspend fun insertStudySession(session: StudySession) =
        studySessionDao.insertStudySession(session)
}
