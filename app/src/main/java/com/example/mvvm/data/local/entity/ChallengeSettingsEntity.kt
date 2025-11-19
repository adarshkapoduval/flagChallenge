package com.example.mvvm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "challenge_settings")
data class ChallengeSettingsEntity(
    @PrimaryKey val id: Int = 0, // always one row
    val scheduledStartMs: Long
)
