package com.example.brainbyte.data.entity

data class QuizQuestion(
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)
