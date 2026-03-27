package com.example.quiz.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserScore(
    val username: String = "",
    val score: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val gameMode: String=""
)