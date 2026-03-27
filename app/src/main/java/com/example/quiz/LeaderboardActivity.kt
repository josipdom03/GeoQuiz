package com.example.quiz

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quiz.data.FirebaseManager
import com.example.quiz.databinding.ActivityLeaderboardBinding

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLeaderboardBinding
    private val firebaseManager = FirebaseManager()
    private var currentUsername: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Dohvaćanje korisničkog imena iz postavki
        val sharedPref = getSharedPreferences("QuizPrefs", MODE_PRIVATE)
        currentUsername = sharedPref.getString("USERNAME", "") ?: ""

        // 2. Postavljanje RecyclerView-a
        binding.rvLeaderboard.layoutManager = LinearLayoutManager(this)

        // 3. NOVO: Dohvaćanje i prikaz UKUPNIH bodova korisnika
        loadTotalScore()

        // 4. Početno učitavanje ljestvice (Globalni gradovi)
        loadLeaderboard("GUESS_CITY", isPersonal = false)

        // 5. Postavljanje klikova na gumbe
        setupClickListeners()
    }

    private fun loadTotalScore() {
        firebaseManager.getTotalScore(currentUsername) { total ->
            // Pretpostavka je da tvoj XML ima TextView s id-em tvTotalUserPoints
            binding.tvTotalUserPoints.text = "UKUPAN BROJ BODOVA: $total"
        }
    }

    private fun setupClickListeners() {
        // Globalni rezultati - Gradovi
        binding.btnModeCity.setOnClickListener {
            loadLeaderboard("GUESS_CITY", isPersonal = false)
            updateUI(binding.btnModeCity)
        }

        // Globalni rezultati - Države
        binding.btnModeCountry.setOnClickListener {
            loadLeaderboard("GUESS_COUNTRY", isPersonal = false)
            updateUI(binding.btnModeCountry)
        }

        // Moji rezultati - Gradovi
        binding.btnMyCity.setOnClickListener {
            loadLeaderboard("GUESS_CITY", isPersonal = true)
            updateUI(binding.btnMyCity)
        }

        // Moji rezultati - Države
        binding.btnMyCountry.setOnClickListener {
            loadLeaderboard("GUESS_COUNTRY", isPersonal = true)
            updateUI(binding.btnMyCountry)
        }

        // Povratak natrag
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadLeaderboard(mode: String, isPersonal: Boolean) {
        if (isPersonal) {
            // Dohvati samo rezultate trenutnog korisnika za taj mod
            firebaseManager.getMyScores(mode, currentUsername) { scores ->
                binding.rvLeaderboard.adapter = LeaderboardAdapter(scores)
            }
        } else {
            // Dohvati TOP 10 rezultata svih igrača za taj mod
            firebaseManager.getTopScores(mode) { scores ->
                binding.rvLeaderboard.adapter = LeaderboardAdapter(scores)
            }
        }
    }

    private fun updateUI(selectedButton: android.widget.Button) {
        val buttons = listOf(
            binding.btnModeCity,
            binding.btnModeCountry,
            binding.btnMyCity,
            binding.btnMyCountry
        )

        buttons.forEach { button ->
            if (button == selectedButton) {
                // Stil za ODABRANI gumb (Ljubičasta pozadina, bijeli tekst)
                button.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#6200EE")
                )
                button.setTextColor(android.graphics.Color.WHITE)
            } else {
                // Stil za OSTALE gumbe (Siva pozadina, crni tekst)
                button.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#E0E0E0")
                )
                button.setTextColor(android.graphics.Color.BLACK)
            }
        }
    }
}