package com.adarsh.flag.startup

import android.content.Context
import com.adarsh.flag.repository.ChallengeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializer @Inject constructor(
    private val repo: ChallengeRepository,
    @ApplicationContext private val context: Context
) {
    suspend fun populateIfNeeded(rawResId: Int) {
        repo.populateQuestionsIfEmpty(context, rawResId)
    }
}