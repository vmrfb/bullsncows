package com.example.bullncows.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bullncows.databinding.ItemAttemptBinding
import com.example.bullncows.model.Attempt

class AttemptsAdapter : RecyclerView.Adapter<AttemptsAdapter.AttemptViewHolder>() {
    private var attempts = listOf<Attempt>()

    fun updateAttempts(newAttempts: List<Attempt>) {
        attempts = newAttempts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttemptViewHolder {
        val binding = ItemAttemptBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AttemptViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttemptViewHolder, position: Int) {
        holder.bind(attempts[position])
    }

    override fun getItemCount(): Int = attempts.size

    class AttemptViewHolder(private val binding: ItemAttemptBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(attempt: Attempt) {
            binding.tvGuess.text = attempt.guess
            binding.tvResult.text = attempt.getResultText(binding.root.context)
        }
    }
} 