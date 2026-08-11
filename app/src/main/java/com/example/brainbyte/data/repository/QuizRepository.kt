package com.example.brainbyte.data.repository

import com.example.brainbyte.data.dao.FlashcardDao
import com.example.brainbyte.data.entity.Flashcard
import com.example.brainbyte.data.entity.QuizQuestion
import com.example.brainbyte.data.network.GeminiQuizService
import javax.inject.Inject
import kotlin.random.Random

class QuizRepository @Inject constructor(
    private val flashcardDao: FlashcardDao,
    private val geminiQuizService: GeminiQuizService
) {

    suspend fun generateQuiz(deckId: String): List<QuizQuestion> {
        val flashcards = flashcardDao.getFlashcardsByDeckIdList(deckId)
        
        if (flashcards.size < 4) {
            throw Exception("Not enough flashcards to generate a quiz. Minimum 4 required.")
        }

        // Try AI generation first
        val aiGeneratedQuiz = geminiQuizService.generateQuiz(flashcards)
        
        if (aiGeneratedQuiz.isNotEmpty()) {
            return aiGeneratedQuiz
        }

        // Offline / Error Fallback: Algorithmic MCQs
        return generateOfflineQuiz(flashcards)
    }

    private fun generateOfflineQuiz(flashcards: List<Flashcard>): List<QuizQuestion> {
        val quizList = mutableListOf<QuizQuestion>()
        
        val targetSize = minOf(flashcards.size, 10) // Up to 10 questions
        val selectedCards = flashcards.shuffled().take(targetSize)

        for (card in selectedCards) {
            val allOtherDefinitions = flashcards.filter { it.id != card.id }.map { it.definition }.shuffled()
            val wrongOptions = allOtherDefinitions.take(3).toMutableList()
            
            // In case we don't have enough unique wrong options, duplicate some or just use what we have
            while (wrongOptions.size < 3) {
                wrongOptions.add("None of the above")
            }
            
            val correctIndex = Random.nextInt(4)
            val options = mutableListOf<String>()
            
            var wrongIndex = 0
            for (i in 0 until 4) {
                if (i == correctIndex) {
                    options.add(card.definition)
                } else {
                    options.add(wrongOptions[wrongIndex])
                    wrongIndex++
                }
            }
            
            quizList.add(
                QuizQuestion(
                    questionText = "What is the definition of '${card.term}'?",
                    options = options,
                    correctOptionIndex = correctIndex,
                    explanation = "The definition of ${card.term} is ${card.definition}."
                )
            )
        }
        
        return quizList
    }
}
