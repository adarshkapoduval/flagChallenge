package com.example.mvvm.ui.state

data class UiQuestion(
    val index: Int,
    val countryCode: String,
    val correctAnswerId: Int,
    val optionsJson: String
)
