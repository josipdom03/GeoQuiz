package com.example.quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu) // Standardni način postavljanja layouta

        auth = FirebaseAuth.getInstance()

        // Dohvaćanje elemenata preko ID-a (findViewById)
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val btnLeaderboard = findViewById<Button>(R.id.btnLeaderboard)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val sharedPref = getSharedPreferences("QuizPrefs", MODE_PRIVATE)
        val username = sharedPref.getString("USERNAME", "Igrač")
        tvWelcome.text = "Pozdrav, $username!"
        val btnGuessCity = findViewById<Button>(R.id.btnGuessCity) // Pitanje: Država -> Odgovor: Grad
        val btnGuessCountry = findViewById<Button>(R.id.btnGuessCountry) // Pitanje: Grad -> Odgovor: Država

        btnGuessCity.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("GAME_MODE", "GUESS_CITY")
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnShowMyResults).setOnClickListener {
            val intent = Intent(this, MyResultsActivity::class.java)
            startActivity(intent)
        }
        btnGuessCountry.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("GAME_MODE", "GUESS_COUNTRY")
            startActivity(intent)
        }


        btnLeaderboard.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            sharedPref.edit().remove("USERNAME").apply()
            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}