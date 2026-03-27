package com.example.quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quiz.data.FirebaseManager

class ResultActivity : AppCompatActivity() {

    private val firebaseManager = FirebaseManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // 1. Povezivanje UI elemenata (Sada tvResultTitle postoji u XML-u)
        val tvTitle = findViewById<TextView>(R.id.tvResultTitle)
        val tvFinalScore = findViewById<TextView>(R.id.tvFinalScore)
        val tvAccuracy = findViewById<TextView>(R.id.tvAccuracy)
        val tvCorrectCount = findViewById<TextView>(R.id.tvCorrectCount)
        val tvAverageTime = findViewById<TextView>(R.id.tvAverageTime)
        val btnFinish = findViewById<Button>(R.id.btnFinish)

        // 2. Dohvaćanje podataka iz Intenta
        val score = intent.getIntExtra("SCORE", 0)
        val gameMode = intent.getStringExtra("GAME_MODE") ?: "GUESS_CITY"
        val correct = intent.getIntExtra("CORRECT_ANSWERS", 0)
        val total = intent.getIntExtra("TOTAL_QUESTIONS", 10)
        val accuracy = intent.getIntExtra("ACCURACY", 0)
        val avgTime = intent.getStringExtra("AVERAGE_TIME") ?: "0.0"

        // 3. Postavljanje teksta
        tvFinalScore.text = "Vaš rezultat: $score"
        tvAccuracy.text = "Točnost: $accuracy%"
        tvCorrectCount.text = "Pogođeno: $correct od $total"
        tvAverageTime.text = "Prosječna brzina: ${avgTime}s"

        // 4. Dinamička titula (ako je točnost visoka, promijeni naslov)
        tvTitle.text = when {
            accuracy >= 90 -> "Genijalno!"
            accuracy >= 70 -> "Odlično!"
            accuracy >= 50 -> "Dobro odrađeno!"
            else -> "Kviz Završen!"
        }

        // 5. Dohvaćanje korisničkog imena
        val sharedPref = getSharedPreferences("QuizPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "Nepoznati igrač") ?: "Nepoznati igrač"

        // 6. Logika gumba za kraj
        btnFinish.setOnClickListener {
            btnFinish.isEnabled = false
            btnFinish.text = "Spremanje..."

            firebaseManager.saveScore(username, score, gameMode) { success ->
                if (success) {
                    Toast.makeText(this, "Rezultat spremljen!", Toast.LENGTH_SHORT).show()

                    // NOVO: Umjesto u Main, idemo na RankingActivity
                    val intent = Intent(this, RankingActivity::class.java)
                    intent.putExtra("CURRENT_SCORE", score)
                    intent.putExtra("GAME_MODE", gameMode)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Greška pri spremanju!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}