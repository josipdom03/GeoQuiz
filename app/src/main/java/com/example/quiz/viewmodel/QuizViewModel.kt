package com.example.quiz.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.quiz.model.CountryCity
import com.example.quiz.model.QuizQuestion

class QuizViewModel : ViewModel() {

    private var timer: CountDownTimer? = null

    // LiveData koju Activity promatra
    private val _currentQuestion = MutableLiveData<QuizQuestion>()
    val currentQuestion: LiveData<QuizQuestion> = _currentQuestion

    private val _timeLeft = MutableLiveData<Long>()
    val timeLeft: LiveData<Long> = _timeLeft

    private val _score = MutableLiveData<Int>(0)
    val score: LiveData<Int> = _score

    private val _isGameOver = MutableLiveData<Boolean>(false)
    val isGameOver: LiveData<Boolean> = _isGameOver

    private var allCountries = listOf<CountryCity>()
    private var remainingQuestions = mutableListOf<CountryCity>()

    // Inicijalizacija kviza
    fun setupQuiz(data: List<CountryCity>, initialTimeSeconds: Long) {
        allCountries = data
        remainingQuestions = data.shuffled().toMutableList()
        _timeLeft.value = initialTimeSeconds
        nextQuestion()
        startTimer(initialTimeSeconds)
    }

    private fun nextQuestion() {
        if (remainingQuestions.isEmpty()) {
            _isGameOver.value = true
            return
        }

        val correctPair = remainingQuestions.removeAt(0)

        // Logika za generiranje 3 kriva odgovora
        val wrongOptions = allCountries
            .filter { it.city != correctPair.city }
            .shuffled()
            .take(3)
            .map { it.city }

        val options = (wrongOptions + correctPair.city).shuffled()

        _currentQuestion.value = QuizQuestion(
            questionText = "What is the capital of ${correctPair.country}?",
            correctAnswer = correctPair.city,
            allOptions = options
        )
    }

    fun submitAnswer(answer: String) {
        if (answer == _currentQuestion.value?.correctAnswer) {
            _score.value = (_score.value ?: 0) + 10
            addTime(5000) // Bonus 5 sekundi
        } else {
            _score.value = (_score.value ?: 0) - 5
            addTime(-10000) // Kazna 10 sekundi
        }
        nextQuestion()
    }

    // Tajmer logika (Ključno za rješavanje prekida pozivom)
    private fun startTimer(seconds: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished / 1000
            }

            override fun onFinish() {
                _isGameOver.value = true
            }
        }.start()
    }

    fun pauseTimer() {
        timer?.cancel()
    }

    fun resumeTimer() {
        _timeLeft.value?.let { startTimer(it) }
    }

    private fun addTime(millis: Long) {
        val currentTime = (_timeLeft.value ?: 0) * 1000
        val newTime = (currentTime + millis).coerceAtLeast(0)
        startTimer(newTime / 1000)
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}