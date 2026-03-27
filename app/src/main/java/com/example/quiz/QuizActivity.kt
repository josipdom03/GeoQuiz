package com.example.quiz

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quiz.model.CountryCity
import com.example.quiz.model.QuizQuestion
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class QuizActivity : AppCompatActivity() {

    private lateinit var allCountryData: List<CountryCity>
    private var currentQuestions = mutableListOf<QuizQuestion>()
    private var currentQuestionIndex = 0
    private var score = 0
    private var timer: CountDownTimer? = null

    // --- VARIJABLE ZA STATISTIKU ---
    private var correctAnswersCount = 0
    private var totalTimeSpent = 0L
    private val totalQuestionsCount = 10

    // --- VARIJABLE ZA POLA-POLA BONUS ---
    private var halfHalfCount = 3
    private lateinit var btnHalfHalf: Button

    // --- NOVE VARIJABLE ZA UNAPRIJEĐENJA ---
    private lateinit var progressBar: ProgressBar
    private lateinit var tvCurrentScore: TextView

    private var secondsLeft = 0
    private var gameMode: String? = "GUESS_CITY"

    private lateinit var tvQuestionText: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvCount: TextView
    private lateinit var buttons: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        gameMode = intent.getStringExtra("GAME_MODE")

        // Inicijalizacija UI elemenata
        tvQuestionText = findViewById(R.id.tvQuestionText)
        tvTimer = findViewById(R.id.tvTimer)
        tvCount = findViewById(R.id.tvQuestionCount)
        btnHalfHalf = findViewById(R.id.btnHalfHalf)

        // Inicijalizacija novih elemenata
        progressBar = findViewById(R.id.quizProgressBar)
        tvCurrentScore = findViewById(R.id.tvCurrentScore)

        buttons = listOf(
            findViewById(R.id.btnOpt1),
            findViewById(R.id.btnOpt2),
            findViewById(R.id.btnOpt3),
            findViewById(R.id.btnOpt4)
        )

        // Postavljanje početnih vrijednosti
        progressBar.max = totalQuestionsCount
        tvCurrentScore.text = "Bodovi: 0"
        btnHalfHalf.text = "Pola-Pola ($halfHalfCount)"

        btnHalfHalf.setOnClickListener {
            useHalfHalf()
        }

        allCountryData = loadCitiesFromJson()
        prepareQuizQuestions(count = totalQuestionsCount)
        showQuestion()
    }

    private fun loadCitiesFromJson(): List<CountryCity> {
        return try {
            val jsonString = assets.open("cities.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<CountryCity>>() {}.type
            Gson().fromJson(jsonString, listType)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun prepareQuizQuestions(count: Int) {
        val shuffledData = allCountryData.shuffled().take(count)

        for (item in shuffledData) {
            val questionText: String
            val correctAnswer: String
            val wrongOptions: List<String>

            if (gameMode == "GUESS_COUNTRY") {
                questionText = "Kojoj državi pripada glavni grad: ${item.city}?"
                correctAnswer = item.country
                wrongOptions = allCountryData
                    .filter { it.country != item.country }
                    .map { it.country }
                    .distinct()
                    .shuffled()
                    .take(3)
            } else {
                questionText = "Koji je glavni grad države: ${item.country}?"
                correctAnswer = item.city
                wrongOptions = allCountryData
                    .filter { it.city != item.city }
                    .map { it.city }
                    .shuffled()
                    .take(3)
            }

            val allOptions = (wrongOptions + correctAnswer).shuffled()

            currentQuestions.add(
                QuizQuestion(
                    questionText = questionText,
                    correctAnswer = correctAnswer,
                    allOptions = allOptions
                )
            )
        }
    }

    private fun showQuestion() {
        if (currentQuestionIndex < currentQuestions.size) {
            val q = currentQuestions[currentQuestionIndex]

            resetButtons()
            tvQuestionText.text = q.questionText
            tvCount.text = "Pitanje ${currentQuestionIndex + 1}/${currentQuestions.size}"

            // Ažuriranje Progress Bara
            progressBar.progress = currentQuestionIndex + 1

            for (i in 0 until buttons.size) {
                buttons[i].text = q.allOptions[i]
                buttons[i].setOnClickListener {
                    checkAnswer(buttons[i])
                }
            }
            startTimer()
        } else {
            finishQuiz()
        }
    }

    private fun useHalfHalf() {
        if (halfHalfCount > 0) {
            val correctAnswer = currentQuestions[currentQuestionIndex].correctAnswer

            val wrongButtons = buttons.filter { it.text != correctAnswer }
            val buttonsToHide = wrongButtons.shuffled().take(2)

            for (btn in buttonsToHide) {
                btn.visibility = View.INVISIBLE
                btn.isEnabled = false
            }

            halfHalfCount--
            btnHalfHalf.text = "Pola-Pola ($halfHalfCount)"
            btnHalfHalf.isEnabled = false

            Toast.makeText(this, "Bonus iskorišten!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAnswer(selectedButton: Button) {
        timer?.cancel()
        disableButtons()
        btnHalfHalf.isEnabled = false

        val timeSpentOnThisQuestion = 15 - secondsLeft
        totalTimeSpent += timeSpentOnThisQuestion

        val selectedAnswer = selectedButton.text.toString()
        val correctAnswer = currentQuestions[currentQuestionIndex].correctAnswer

        if (selectedAnswer == correctAnswer) {
            correctAnswersCount++
            val totalPoints = 20 + secondsLeft
            score += totalPoints

            // Ažuriranje prikaza trenutnih bodova
            tvCurrentScore.text = "Bodovi: $score"

            selectedButton.setBackgroundColor(Color.GREEN)
            selectedButton.setTextColor(Color.WHITE)
        } else {
            selectedButton.setBackgroundColor(Color.RED)
            selectedButton.setTextColor(Color.WHITE)

            val correctButton = buttons.find { it.text == correctAnswer }
            correctButton?.setBackgroundColor(Color.GREEN)
            correctButton?.setTextColor(Color.WHITE)
        }

        tvQuestionText.postDelayed({
            nextQuestion()
        }, 1500)
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(15000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsLeft = (millisUntilFinished / 1000).toInt()
                tvTimer.text = "Vrijeme: $secondsLeft"
            }
            override fun onFinish() {
                totalTimeSpent += 15
                secondsLeft = 0
                disableButtons()
                btnHalfHalf.isEnabled = false

                val correctAnswer = currentQuestions[currentQuestionIndex].correctAnswer
                buttons.find { it.text == correctAnswer }?.setBackgroundColor(Color.GREEN)

                tvQuestionText.postDelayed({ nextQuestion() }, 1500)
            }
        }.start()
    }

    private fun nextQuestion() {
        currentQuestionIndex++
        showQuestion()
    }

    private fun disableButtons() {
        buttons.forEach { it.isEnabled = false }
    }

    private fun resetButtons() {
        buttons.forEach {
            it.isEnabled = true
            it.visibility = View.VISIBLE
            it.setBackgroundColor(Color.parseColor("#6200EE"))
            it.setTextColor(Color.WHITE)
        }

        if (halfHalfCount > 0) {
            btnHalfHalf.isEnabled = true
        } else {
            btnHalfHalf.isEnabled = false
            btnHalfHalf.alpha = 0.5f
        }
    }

    private fun finishQuiz() {
        timer?.cancel()
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("SCORE", score)
        intent.putExtra("GAME_MODE", gameMode)
        intent.putExtra("CORRECT_ANSWERS", correctAnswersCount)
        intent.putExtra("TOTAL_QUESTIONS", totalQuestionsCount)

        val accuracy = (correctAnswersCount.toDouble() / totalQuestionsCount * 100).toInt()
        intent.putExtra("ACCURACY", accuracy)

        val averageTime = totalTimeSpent.toDouble() / totalQuestionsCount
        intent.putExtra("AVERAGE_TIME", String.format("%.1f", averageTime))

        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}