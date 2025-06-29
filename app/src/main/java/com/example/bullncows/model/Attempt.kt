package com.example.bullncows.model

import android.content.Context
import com.example.bullncows.R

data class Attempt(
    val guess: String,
    val bulls: Int,
    val cows: Int
) {
    fun getResultText(context: Context): String {
        return when {
            bulls == 4 -> context.getString(R.string.win_message)
            bulls > 0 && cows > 0 -> context.getString(R.string.bulls_and_cows_both, bulls, cows)
            bulls > 0 -> context.getString(R.string.bulls_only, bulls)
            cows > 0 -> context.getString(R.string.cows_only, cows)
            else -> context.getString(R.string.no_matches)
        }
    }
} 