package com.adarsh.flag.data.mapper

import com.adarsh.flag.data.local.entity.AnswerEntity
import com.adarsh.flag.data.local.entity.QuestionEntity
import com.adarsh.flag.domain.model.Answer
import com.adarsh.flag.domain.model.Country
import com.adarsh.flag.domain.model.QuestionModel
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun QuestionEntity.toDomain(): QuestionModel {
    val optionsList = try {
        json.decodeFromString(ListSerializer(Country.serializer()), optionsJson)
    } catch (e: Exception) {
        emptyList()
    }
    return QuestionModel(
        questionIndex = questionIndex,
        countryCode = countryCode,
        correctAnswerId = correctAnswerId,
        options = optionsList
    )
}

fun QuestionModel.toEntity(): QuestionEntity {
    val optionsString = json.encodeToString(ListSerializer(Country.serializer()), options)
    return QuestionEntity(
        questionIndex = questionIndex,
        countryCode = countryCode,
        correctAnswerId = correctAnswerId,
        optionsJson = optionsString
    )
}

fun AnswerEntity.toDomain() = Answer(
    questionIndex = questionIndex,
    selectedOptionId = selectedOptionId,
    answeredAtMs = answeredAtMs,
    isCorrect = isCorrect
)

fun Answer.toEntity() = AnswerEntity(
    questionIndex = questionIndex,
    selectedOptionId = selectedOptionId,
    answeredAtMs = answeredAtMs,
    isCorrect = isCorrect
)
