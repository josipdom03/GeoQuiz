package com.example.quiz.data

import com.example.quiz.model.UserScore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirebaseManager {

    private val db = FirebaseFirestore.getInstance()
    private val scoresCollection = db.collection("scores")

    /**
     * Sprema rezultat koristeći UserScore model.
     * @param mode mora biti proslijeđen iz ResultActivity-a (GUESS_CITY ili GUESS_COUNTRY)
     */
    fun saveScore(username: String, score: Int, mode: String, onComplete: (Boolean) -> Unit) {
        if (username == "Nepoznati igrač" || username.isBlank()) {
            android.util.Log.e("Firebase", "Pokušaj spremanja bez valjanog korisničkog imena!")
            onComplete(false)
            return
        }

        // Umjesto hashMapOf, koristimo tvoju data klasu UserScore
        val userScore = UserScore(
            username = username,
            score = score,
            timestamp = System.currentTimeMillis(),
            gameMode = mode
        )

        // Firebase može direktno spremiti objekt klase ako polja odgovaraju
        scoresCollection
            .add(userScore)
            .addOnSuccessListener { documentReference ->
                android.util.Log.d("Firebase", "Rezultat spremljen! ID: ${documentReference.id}")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Firebase", "Greška pri spremanju!", e)
                onComplete(false)
            }
    }

    /**
     * Dohvaća TOP 10 rezultata za specifičan mod igre.
     * Na ovaj način ljestvica neće miješati bodove iz različitih tipova kviza.
     */
    fun getTopScores(mode: String, onResult: (List<UserScore>) -> Unit) {
        scoresCollection
            .whereEqualTo("gameMode", mode) // Filtriramo rezultate po modu
            .orderBy("score", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                // Pretvaranje dokumenata direktno u listu UserScore objekata
                val scoreList = documents.toObjects(UserScore::class.java)
                onResult(scoreList)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Firebase", "Greška pri dohvaćanju ljestvice!", e)
                onResult(emptyList())
            }
    }


    fun getMyScores(mode: String, username: String, callback: (List<UserScore>) -> Unit) {
        // ISPIS ZA DEBUGGING - Provjeri u Logcat-u jesu li vrijednosti točne
        android.util.Log.d("QUIZ_DEBUG", "Tražim: Mode=$mode, User='$username'")

        db.collection("scores")
            .whereEqualTo("gameMode", mode)
            .whereEqualTo("username", username)
            .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val scores = documents.toObjects(UserScore::class.java)
                android.util.Log.d("QUIZ_DEBUG", "Pronađeno rezultata: ${scores.size}")
                callback(scores)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("QUIZ_DEBUG", "Greška u upitu: ${e.message}")
                callback(emptyList())
            }
    }

    fun getTotalScore(username: String, onResult: (Int) -> Unit) {
        scoresCollection
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                var total = 0
                for (doc in documents) {
                    val score = doc.getLong("score")?.toInt() ?: 0
                    total += score
                }
                onResult(total)
            }
            .addOnFailureListener {
                onResult(0)
            }
    }

    fun getAllScoresForRanking(mode: String, onResult: (List<UserScore>) -> Unit) {
        scoresCollection
            .whereEqualTo("gameMode", mode)
            .orderBy("score", Query.Direction.DESCENDING) // Sortirano od najvećeg prema najmanjem
            .get()
            .addOnSuccessListener { documents ->
                val allScores = documents.toObjects(UserScore::class.java)
                onResult(allScores)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Firebase", "Greška pri dohvaćanju svih rezultata za rang!", e)
                onResult(emptyList())
            }
    }

}