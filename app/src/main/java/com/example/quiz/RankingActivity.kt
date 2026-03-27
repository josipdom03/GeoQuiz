package com.example.quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.quiz.data.FirebaseManager

class RankingActivity : AppCompatActivity() {

    private val firebaseManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)

        val tvRankingMessage = findViewById<TextView>(R.id.tvRankingMessage)
        val btnBackToMain = findViewById<Button>(R.id.btnBackToMain)

        val currentScore = intent.getIntExtra("CURRENT_SCORE", 0)
        val gameMode = intent.getStringExtra("GAME_MODE") ?: "GUESS_CITY"

        // Logika za računanje ranga
        firebaseManager.getAllScoresForRanking(gameMode) { allScores ->
            if (allScores != null) {
                // Sortiramo sve rezultate od najvećeg prema najmanjem
                val sortedScores = allScores.sortedByDescending { it.score }

                // Pronalazimo prvu poziciju gdje se pojavljuje naš rezultat
                // +1 jer index kreće od 0
                val rank = sortedScores.indexOfFirst { it.score <= currentScore } + 1

                // Ako indexOfFirst vrati 0 (što znači -1 + 1), stavljamo na kraj
                val finalRank = if (rank <= 0) sortedScores.size else rank

                tvRankingMessage.text = "Ovo je $finalRank. najbolji ostvareni rezultat do sada."
            } else {
                tvRankingMessage.text = "Ovo je tvoj prvi rezultat!"
            }
        }

        btnBackToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}