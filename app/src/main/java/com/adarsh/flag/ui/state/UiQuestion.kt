package com.adarsh.flag.ui.state

import com.adarsh.flag.domain.model.Country

data class UiQuestion(
    val index: Int,
    val countryCode: String,
    val correctAnswerId: Int,
    val options: List<Country>
)
