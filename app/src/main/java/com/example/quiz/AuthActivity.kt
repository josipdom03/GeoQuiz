package com.example.quiz

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quiz.databinding.ActivityAuthBinding
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Ako je korisnik već prijavljen, idemo na Main/Quiz
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.btnStart.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()

            if (username.isNotEmpty()) {
                loginAnonymously(username)
            } else {
                Toast.makeText(this, "Molimo unesite ime!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginAnonymously(username: String) {
        auth.signInAnonymously().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Ovdje možeš spremiti username u SharedPreferences da ga imaš kroz cijelu aplikaciju
                val sharedPref = getSharedPreferences("QuizPrefs", MODE_PRIVATE)
                sharedPref.edit().putString("USERNAME", username).apply()

                Toast.makeText(this, "Dobrodošao, $username!", Toast.LENGTH_SHORT).show()

                // Kreni na glavnu aktivnost
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Greška pri prijavi: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}