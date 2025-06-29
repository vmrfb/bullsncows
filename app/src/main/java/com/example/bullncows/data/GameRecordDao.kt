package com.example.bullncows.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {
    @Query("SELECT * FROM game_records ORDER BY timeInSeconds ASC, attempts ASC LIMIT 10")
    fun getTopRecords(): Flow<List<GameRecord>>

    @Insert
    suspend fun insertRecord(record: GameRecord)

    @Query("SELECT COUNT(*) FROM game_records")
    suspend fun getRecordCount(): Int
} 