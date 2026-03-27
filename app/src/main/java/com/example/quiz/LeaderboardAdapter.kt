package com.example.quiz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.quiz.databinding.ItemLeaderboardBinding
import com.example.quiz.model.UserScore

class LeaderboardAdapter(private val scores: List<UserScore>) :
    RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    // ViewHolder koji drži binding za item_leaderboard.xml
    class ViewHolder(val binding: ItemLeaderboardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = scores[position]

        // Povezivanje podataka s elementima (provjeri ID-ove u XML-u!)
        holder.binding.apply {
            tvRank.text = "${position + 1}."
            tvUsername.text = user.username
            tvScore.text = "${user.score} pts"
        }
    }

    override fun getItemCount() = scores.size
}