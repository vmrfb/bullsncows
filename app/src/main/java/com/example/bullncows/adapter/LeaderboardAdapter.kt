package com.example.bullncows.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bullncows.R
import com.example.bullncows.data.GameRecord
import com.example.bullncows.databinding.ItemLeaderboardBinding

class LeaderboardAdapter : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {
    private var records = listOf<GameRecord>()

    fun updateRecords(newRecords: List<GameRecord>) {
        records = newRecords
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val binding = ItemLeaderboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LeaderboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(records[position], position + 1)
    }

    override fun getItemCount(): Int = records.size

    class LeaderboardViewHolder(private val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: GameRecord, rank: Int) {
            binding.tvRank.text = rank.toString()
            binding.tvName.text = record.playerName
            binding.tvAttempts.text = "${record.attempts} попыток"
            binding.tvTime.text = formatTime(record.timeInSeconds)
        }

        private fun formatTime(seconds: Long): String {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }
} 