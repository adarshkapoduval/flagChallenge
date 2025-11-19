package com.adarsh.flag.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.adarsh.flag.data.local.entity.ChallengeSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChallengeSettingsDao {

    @Query("SELECT scheduledStartMs FROM challenge_settings WHERE id = 0 LIMIT 1")
    fun getStartTime(): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(settings: ChallengeSettingsEntity)

    @Query("DELETE FROM challenge_settings WHERE id = 0")
    suspend fun clear()
}