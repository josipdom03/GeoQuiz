package com.example.quiz


import android.widget.Button
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quiz.data.FirebaseManager
import com.example.quiz.databinding.ActivityMyResultsBinding
import com.example.quiz.model.UserScore

class MyResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyResultsBinding
    private val firebaseManager = FirebaseManager()
    private var fullList: List<UserScore> = emptyList() // Ovdje čuvamo sve podatke

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("QuizPrefs", MODE_PRIVATE)
        val currentUsername = sharedPref.getString("USERNAME", "") ?: ""

        binding.rvMyResults.layoutManager = LinearLayoutManager(this)

        // 1. Inicijalno učitavanje svih podataka
        loadData(currentUsername)

        // 2. Postavljanje klikova za sortiranje/filtriranje
        binding.btnFilterAll.setOnClickListener {
            updateRecyclerView(fullList)
            updateButtonColors(binding.btnFilterAll)
        }

        binding.btnFilterCity.setOnClickListener {
            val filtered = fullList.filter { it.gameMode == "GUESS_CITY" }
            updateRecyclerView(filtered)
            updateButtonColors(binding.btnFilterCity)
        }

        binding.btnFilterCountry.setOnClickListener {
            val filtered = fullList.filter { it.gameMode == "GUESS_COUNTRY" }
            updateRecyclerView(filtered)
            updateButtonColors(binding.btnFilterCountry)
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadData(username: String) {
        firebaseManager.getMyScores("GUESS_CITY", username) { cityScores ->
            firebaseManager.getMyScores("GUESS_COUNTRY", username) { countryScores ->
                // Spajamo sve i sortiramo od najvećeg
                fullList = (cityScores + countryScores).sortedByDescending { it.score }
                updateRecyclerView(fullList)
            }
        }
    }

    private fun updateRecyclerView(list: List<UserScore>) {
        binding.rvMyResults.adapter = LeaderboardAdapter(list)
    }

    private fun updateButtonColors(selected: Button) {
        val buttons = listOf(binding.btnFilterAll, binding.btnFilterCity, binding.btnFilterCountry)
        buttons.forEach { btn ->
            if (btn == selected) {
                btn.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#6200EE"))
                btn.setTextColor(android.graphics.Color.WHITE)
            } else {
                btn.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#E0E0E0"))
                btn.setTextColor(android.graphics.Color.BLACK)
            }
        }
    }
}