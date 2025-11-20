package com.adarsh.flag.startup

import android.content.Context
import com.adarsh.flag.repository.ChallengeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializer @Inject constructor(
    private val repo: ChallengeRepository
) {
    suspend fun populateIfNeeded(rawResId: Int) {
        repo.populateQuestionsIfEmpty( rawResId)
    }
}