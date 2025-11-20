package com.adarsh.flag.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents the raw question data structure as received from the JSON file.
 * This is a Data Transfer Object (DTO).
 */
@Serializable
data class QuestionDto(
    val answer_id: Int,
    val countries: List<Country>,
    val country_code: String
)
