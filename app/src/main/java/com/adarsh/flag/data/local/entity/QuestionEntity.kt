package com.adarsh.flag.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val questionIndex: Int,
    val countryCode: String,
    val correctAnswerId: Int,
    val optionsJson: String
)