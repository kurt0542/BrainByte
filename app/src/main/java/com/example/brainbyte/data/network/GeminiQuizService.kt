package com.example.brainbyte.data.network

import com.example.brainbyte.BuildConfig
import com.example.brainbyte.data.entity.Flashcard
import com.example.brainbyte.data.entity.QuizQuestion
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject

class GeminiQuizService @Inject constructor() {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generateQuiz(flashcards: List<Flashcard>): List<QuizQuestion> {
        return withContext(Dispatchers.IO) {
            try {
                if (BuildConfig.GEMINI_API_KEY.isEmpty()) {
                    throw Exception("Gemini API Key is missing. Please add it to your .env or local.properties.")
                }

                val cardsText = flashcards.joinToString("\n") { "Term: ${it.term}, Definition: ${it.definition}" }
                
                val prompt = """
                    You are an expert tutor. Create a 5-question multiple choice quiz based on these flashcards:
                    $cardsText
                    
                    Return the output ONLY as a raw JSON array of objects. Do not include markdown formatting like ```json.
                    Each object must have the following exact keys:
                    "questionText": A string representing the question.
                    "options": An array of 4 string options.
                    "correctOptionIndex": An integer (0 to 3) indicating the correct option.
                    "explanation": A short explanation of why the answer is correct.
                """.trimIndent()

                val response = model.generateContent(content { text(prompt) })
                val responseText = response.text ?: throw Exception("Empty response from Gemini")
                
                // Clean the response in case the model adds markdown formatting despite instructions
                val cleanedText = responseText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                
                val jsonArray = JSONArray(cleanedText)
                val quizQuestions = mutableListOf<QuizQuestion>()
                
                for (i in 0 until jsonArray.length()) {
                    val jsonObj = jsonArray.getJSONObject(i)
                    val optionsArray = jsonObj.getJSONArray("options")
                    val optionsList = mutableListOf<String>()
                    for (j in 0 until optionsArray.length()) {
                        optionsList.add(optionsArray.getString(j))
                    }
                    
                    quizQuestions.add(
                        QuizQuestion(
                            questionText = jsonObj.getString("questionText"),
                            options = optionsList,
                            correctOptionIndex = jsonObj.getInt("correctOptionIndex"),
                            explanation = jsonObj.getString("explanation")
                        )
                    )
                }
                
                quizQuestions
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList() // Return empty list to trigger offline fallback
            }
        }
    }
}
