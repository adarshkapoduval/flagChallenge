package com.adarsh.flag.utils

import android.content.Context
import com.adarsh.flag.data.local.entity.QuestionEntity
import com.adarsh.flag.domain.model.Country
import com.adarsh.flag.domain.model.QuestionsWrapper

import kotlinx.serialization.json.Json

object QuestionsParser {
    private val json = Json { ignoreUnknownKeys = true }

    // Read the questions.json file from res/raw and parse it into a list of QuestionEntity.

    fun parseFromRawResource(ctx: Context, resId: Int): List<QuestionEntity> {
        val raw = ctx.resources.openRawResource(resId).bufferedReader().use { it.readText() }
        val wrapper = json.decodeFromString<QuestionsWrapper>(raw)
        return wrapper.questions.mapIndexed { idx, dto ->
            // convert countries list to compact JSON for storage in optionsJson
            val optionsJson = json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(Country.serializer()),
                dto.countries
            )
            QuestionEntity(
                questionIndex = idx,
                countryCode = dto.country_code,
                correctAnswerId = dto.answer_id,
                optionsJson = optionsJson
            )
        }
    }
}
