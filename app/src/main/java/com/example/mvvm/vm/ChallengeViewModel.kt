package com.example.mvvm.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvm.data.local.entity.AnswerEntity
import com.example.mvvm.domain.model.enums.Phase
import com.example.mvvm.repository.ChallengeRepository
import com.example.mvvm.ui.state.ChallengeState
import com.example.mvvm.ui.state.UiQuestion
import com.example.mvvm.ui.state.UiState
import com.example.mvvm.utils.computeChallengeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val repo: ChallengeRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var tickerJob: Job? = null

    init {
        viewModelScope.launch {
            repo.getAllQuestionsFlow().collect { list ->
                val uiQs = list.map {
                    UiQuestion(
                        it.questionIndex,
                        it.countryCode,
                        it.correctAnswerId,
                        it.optionsJson
                    )
                }
                _ui.update { it.copy(questions = uiQs) }
            }
        }
    }

    fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                updateFromSchedule()
                delay(300L)
            }
        }
    }

    fun stopTicker() {
        tickerJob?.cancel()
    }

    private suspend fun updateFromSchedule() {
        val scheduled = repo.getScheduledStartTime()
        if (scheduled == null) {
            _ui.update { it.copy(phase = Phase.NOT_NEAR, currentQuestionIndex = null, remainingMs = 0L) }
            return
        }
        val state: ChallengeState = computeChallengeState(scheduled)
        _ui.update { it.copy(phase = state.phase, currentQuestionIndex = state.questionIndex, remainingMs = state.remainingMs) }
    }

    fun scheduleChallenge(ms: Long) {
        viewModelScope.launch {
            repo.saveScheduledStartTime(ms)
        }
        startTicker()
    }


    // 1) expose a suspend getter to load saved answer for a question
    suspend fun getSavedAnswer(questionIndex: Int): AnswerEntity? {
        return withContext(Dispatchers.IO) {
            repo.getAnswer(questionIndex)
        }
    }

    // 2) update selectOption to also update UI state immediately (so composable reacts)
    fun selectOption(questionIndex: Int, selectedOptionId: Int) {
        viewModelScope.launch {
            // persist
            val q = repo.getQuestion(questionIndex) ?: return@launch
            val isCorrect = selectedOptionId == q.correctAnswerId
            val answer = AnswerEntity(questionIndex, selectedOptionId, System.currentTimeMillis(), isCorrect)
            repo.saveAnswer(answer)

            // update UiState so composable can reflect saved selection immediately
            _ui.update { current ->
                // If UiState stores more per-question info in future, update accordingly.
                current.copy() // no structural change here, but composable will query repo for saved answer (see below).
            }
        }
    }
}