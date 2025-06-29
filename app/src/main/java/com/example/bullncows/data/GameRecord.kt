package com.example.bullncows.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playerName: String,
    val attempts: Int,
    val timeInSeconds: Long,
    val timestamp: Long = System.currentTimeMillis()
) 