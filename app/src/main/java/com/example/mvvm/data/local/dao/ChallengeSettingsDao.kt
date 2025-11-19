package com.example.mvvm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mvvm.data.local.entity.ChallengeSettingsEntity

@Dao
interface ChallengeSettingsDao {

    @Query("SELECT scheduledStartMs FROM challenge_settings WHERE id = 0 LIMIT 1")
    suspend fun getStartTime(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(settings: ChallengeSettingsEntity)

    @Query("DELETE FROM challenge_settings WHERE id = 0")
    suspend fun clear()
}