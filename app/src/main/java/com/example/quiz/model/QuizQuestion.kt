package com.example.quiz.model

data class QuizQuestion(
    val questionText: String,
    val correctAnswer: String,
    val allOptions: List<String>
)