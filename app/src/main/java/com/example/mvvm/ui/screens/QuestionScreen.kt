package com.example.mvvm.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mvvm.domain.constants.ChallengeConstants
import com.example.mvvm.domain.model.Country
import com.example.mvvm.domain.model.enums.Phase
import com.example.mvvm.ui.state.UiQuestion
import com.example.mvvm.vm.ChallengeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Composable
fun QuestionScreen(viewModel: ChallengeViewModel) {
    val ui by viewModel.ui.collectAsState()
    val phase = ui.phase

    val totalQuestions = ui.questions.size.takeIf { it > 0 } ?: ChallengeConstants.TOTAL_QUESTIONS

    var displayRemaining by remember { mutableStateOf(ui.remainingMs) }
    LaunchedEffect(ui.remainingMs) {
        displayRemaining = ui.remainingMs
    }

    LaunchedEffect(ui.phase, ui.currentQuestionIndex, ui.remainingMs) {
        if (ui.phase == Phase.QUESTION && ui.remainingMs > 0L) {
            while (viewModel.ui.value.phase == Phase.QUESTION && viewModel.ui.value.remainingMs > 0L) {
                displayRemaining = viewModel.ui.value.remainingMs
                delay(200L)
            }
        }
    }

    when (phase) {
        Phase.NOT_NEAR -> {
            ScheduleScreen(viewModel)
        }
        Phase.PRE_START -> {
            PreStartScreen(displayRemaining)
        }
        Phase.QUESTION -> {
            val idx = ui.currentQuestionIndex ?: 0
            val q = ui.questions.find { it.index == idx }
            if (q == null) {
                LoadingScreen()
            } else {
                QuestionContent(
                    question = q,
                    remainingMs = ui.remainingMs,
                    viewModel = viewModel,
                    totalQuestions = totalQuestions
                )
            }
        }
        Phase.INTERVAL -> {
            val finishedIdx = ui.currentQuestionIndex
            if (finishedIdx == null) {
                IntervalPlaceholder(ui.remainingMs)
            } else {
                val finishedQuestion = ui.questions.find { it.index == finishedIdx }
                if (finishedQuestion == null) {
                    IntervalPlaceholder(ui.remainingMs)
                } else {
                    IntervalResultView(
                        question = finishedQuestion,
                        remainingMs = ui.remainingMs,
                        viewModel = viewModel,
                        totalQuestions = totalQuestions
                    )
                }
            }
        }
        Phase.FINISHED -> {
            val totalQuestions = ui.questions.size.takeIf { it > 0 } ?: ChallengeConstants.TOTAL_QUESTIONS
            val finalScore = ui.finalScore ?: 0
            FinishedScreen(finalScore = finalScore, totalQuestions = totalQuestions, viewModel = viewModel)
        }
    }
}

@Composable
private fun PreStartScreen(remainingMs: Long) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Get Ready!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.scale(scale)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = formatMs(remainingMs),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading question...")
        }
    }
}

@Composable
private fun IntervalPlaceholder(remainingMs: Long) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Next Question",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = formatMs(remainingMs),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun QuestionContent(
    question: UiQuestion,
    remainingMs: Long,
    viewModel: ChallengeViewModel,
    totalQuestions: Int
) {
    val scope = rememberCoroutineScope()
    val json = remember { Json { ignoreUnknownKeys = true } }

    val options: List<Country> = remember(question.optionsJson) {
        try {
            json.decodeFromString(ListSerializer(Country.serializer()), question.optionsJson)
        } catch (t: Throwable) {
            listOf(
                Country(country_name = "Option 1", id = 1),
                Country(country_name = "Option 2", id = 2),
                Country(country_name = "Option 3", id = 3),
                Country(country_name = "Option 4", id = 4)
            )
        }
    }

    var selectedId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(question.index) {
        try {
            val saved = viewModel.getSavedAnswer(question.index)
            selectedId = saved?.selectedOptionId
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    val showResult = remainingMs <= 0L

    // FIX: Use remainingMs directly from ViewModel instead of displayRemainingMs
    // Capture initial duration when the question first appears
    var initialDuration by remember(question.index) { mutableStateOf(0L) }

    // Update displayRemainingMs locally to ensure smooth updates
    var localDisplayRemaining by remember(question.index) { mutableStateOf(remainingMs) }

    LaunchedEffect(question.index, remainingMs) {
        // Set initial duration only once per question
        if (initialDuration == 0L && remainingMs > 0L) {
            initialDuration = remainingMs
        }
        localDisplayRemaining = remainingMs

        // Continue updating while question is active
        while (remainingMs > 0L) {
            delay(100L)
            localDisplayRemaining = viewModel.ui.value.remainingMs
            if (viewModel.ui.value.phase != Phase.QUESTION) break
        }
    }

    val timeProgress = if (initialDuration > 0) {
        (localDisplayRemaining.toFloat() / initialDuration).coerceIn(0f, 1f)
    } else {
        1f // Start at full if we don't have initial duration yet
    }

    val progressColor = when {
        timeProgress > 0.5f -> MaterialTheme.colorScheme.primary
        timeProgress > 0.25f -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with progress and timer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${question.index + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = progressColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = progressColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${question.index + 1}/$totalQuestions",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = progressColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { timeProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatMs(localDisplayRemaining),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }
        }

        // Question content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
        ) {
            // Flag card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = question.countryCode,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Which country is this?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val clickable = remainingMs > 0L

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEachIndexed { index, opt ->
                    val isSelected = selectedId == opt.id
                    val isCorrect = opt.id == question.correctAnswerId
                    val showCorrectHighlight = showResult && isCorrect
                    val showWrongHighlight = showResult && isSelected && !isCorrect

                    AnimatedOptionCard(
                        text = opt.country_name,
                        index = index,
                        isSelected = isSelected,
                        isCorrect = showCorrectHighlight,
                        isWrong = showWrongHighlight,
                        isDisabled = !clickable,
                        showResult = showResult,
                        onClick = {
                            selectedId = opt.id
                            scope.launch {
                                viewModel.selectOption(question.index, opt.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedOptionCard(
    text: String,
    index: Int,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    isDisabled: Boolean,
    showResult: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected && !showResult) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val containerColor = when {
        isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.15f)
        isWrong -> Color(0xFFF44336).copy(alpha = 0.15f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isCorrect -> Color(0xFF4CAF50)
        isWrong -> Color(0xFFF44336)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    val textColor = when {
        isCorrect -> Color(0xFF2E7D32)
        isWrong -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .then(if (!isDisabled) Modifier.clickable { onClick() } else Modifier.alpha(0.9f)),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        shadowElevation = if (isSelected && !showResult) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option letter
            Surface(
                shape = CircleShape,
                color = borderColor.copy(alpha = 0.2f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = ('A' + index).toString(),
                        fontWeight = FontWeight.Bold,
                        color = borderColor,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            // Result icon
            AnimatedVisibility(
                visible = showResult && (isCorrect || isWrong),
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun IntervalResultView(
    question: UiQuestion,
    remainingMs: Long,
    viewModel: ChallengeViewModel,
    totalQuestions: Int
) {
    var savedAnswer by remember { mutableStateOf<com.example.mvvm.data.local.entity.AnswerEntity?>(null) }

    LaunchedEffect(question.index) {
        try {
            val ans = viewModel.getSavedAnswer(question.index)
            savedAnswer = ans
        } catch (t: Throwable) { }
    }

    val options: List<Country> = remember(question.optionsJson) {
        val json = Json { ignoreUnknownKeys = true }
        try {
            json.decodeFromString(ListSerializer(Country.serializer()), question.optionsJson)
        } catch (t: Throwable) {
            emptyList()
        }
    }

    val isCorrectAnswer = savedAnswer?.isCorrect == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (isCorrectAnswer) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFF44336).copy(alpha = 0.1f),
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isCorrectAnswer) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = if (isCorrectAnswer) Color(0xFF2E7D32) else Color(0xFFC62828)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isCorrectAnswer) "Correct!" else "Incorrect",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrectAnswer) Color(0xFF2E7D32) else Color(0xFFC62828)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Next question in ${formatMs(remainingMs)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Flag display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = question.countryCode,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Review Answer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                options.forEachIndexed { index, opt ->
                    val isCorrect = opt.id == question.correctAnswerId
                    val isSelected = savedAnswer?.selectedOptionId == opt.id

                    AnimatedOptionCard(
                        text = opt.country_name,
                        index = index,
                        isSelected = isSelected,
                        isCorrect = isCorrect,
                        isWrong = isSelected && !isCorrect,
                        isDisabled = true,
                        showResult = true,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun FinishedScreen(
    finalScore: Int,
    totalQuestions: Int,
    viewModel: ChallengeViewModel
) {
    val safeTotal = if (totalQuestions > 0) totalQuestions else 1
    val percentage = ((finalScore.toFloat() / safeTotal) * 100).toInt().coerceIn(0, 100)

    val infiniteTransition = rememberInfiniteTransition()
    val trophyScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .scale(trophyScale),
                tint = when {
                    percentage >= 80 -> Color(0xFFFFD700)
                    percentage >= 60 -> Color(0xFFC0C0C0)
                    else -> Color(0xFFCD7F32)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Challenge Complete!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Score",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$finalScore",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "out of $totalQuestions",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = (finalScore.toFloat() / safeTotal).coerceIn(0f, 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$percentage% Correct",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.resetChallenge() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Try Again",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


fun formatMs(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0L)
    val m = s / 60
    val sec = s % 60
    return "%02d:%02d".format(m, sec)
}