package com.adarsh.flag.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Country(
    val id: Int,
    val country_name: String
)
