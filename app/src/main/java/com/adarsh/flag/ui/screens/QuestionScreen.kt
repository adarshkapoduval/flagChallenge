package com.adarsh.flag.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.adarsh.flag.domain.constants.ChallengeConstants
import com.adarsh.flag.domain.model.Country
import com.adarsh.flag.domain.model.enums.Phase
import com.adarsh.flag.ui.state.UiQuestion
import com.adarsh.flag.vm.ChallengeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.adarsh.flag.domain.model.Answer
import com.adarsh.flag.ui.theme.CardBg
import com.adarsh.flag.ui.theme.DarkBg
import com.adarsh.flag.ui.theme.Dimens
import com.adarsh.flag.ui.theme.GameBlue
import com.adarsh.flag.ui.theme.GameGreen
import com.adarsh.flag.ui.theme.GamePink
import com.adarsh.flag.ui.theme.GamePurple
import com.adarsh.flag.ui.theme.GameRed
import com.adarsh.flag.ui.theme.GameYellow
import com.adarsh.flag.utils.formatMs

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
private fun isLandscape(): Boolean {
    val config = LocalConfiguration.current
    return config.orientation == Configuration.ORIENTATION_LANDSCAPE ||
            config.screenWidthDp > config.screenHeightDp
}

@Composable
private fun PreStartScreen(remainingMs: Long) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    val landscape = isLandscape()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animated background circles
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotateAngle)
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF9333EA).copy(alpha = glowAlpha * 0.3f),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.6f,
                center = center
            )
        }

        if (!landscape) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(Dimens.SpacerExtraLarge)
            ) {
                // Pulsing "Get Ready" badge
                Card(
                    modifier = Modifier
                        .scale(scale)
                        .shadow(
                            elevation = Dimens.SpacerLarge,
                            shape = RoundedCornerShape(Dimens.SpacerMediumLarge),
                            spotColor = Color(0xFFFBBF24)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(Dimens.SpacerMediumLarge)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFBBF24),
                                        Color(0xFFF59E0B)
                                    )
                                )
                            )
                            .padding(horizontal = Dimens.PaddingExtraLarge, vertical = Dimens.SpacerLarge)
                    ) {
                        Text(
                            text = "⚡ GET READY! ⚡",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            letterSpacing = Dimens.LetterSpaceMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))

                // "Challenge starts in" text
                Text(
                    text = "CHALLENGE STARTS IN",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = Dimens.LetterSpaceLarge
                )

                Spacer(modifier = Modifier.height(Dimens.SpacerExtraLarge))

                // Timer Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = Dimens.SpacerMediumLarge,
                            shape = RoundedCornerShape(Dimens.SpacerExtraLarge),
                            spotColor = Color(0xFF9333EA)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(Dimens.SpacerExtraLarge)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF9333EA).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(Dimens.PaddingExtraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = formatMs(remainingMs),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = Dimens.TextSizeUltraLarge
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpacerExtraLarge))

                // Animated progress indicator
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(glowAlpha)
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(Dimens.SpacerMedium)
                                .background(
                                    Color(0xFFEC4899),
                                    shape = CircleShape
                                )
                        )
                        if (index < 2) {
                            Spacer(modifier = Modifier.width(Dimens.SpacerMedium))
                        }
                    }
                }
            }
        } else {
            // Landscape Layout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(Dimens.PaddingExtraLarge)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pulsing "Get Ready" badge
                    Card(
                        modifier = Modifier
                            .scale(scale)
                            .shadow(
                                elevation = Dimens.SpacerLarge,
                                shape = RoundedCornerShape(Dimens.SpacerMediumLarge),
                                spotColor = Color(0xFFFBBF24)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(Dimens.SpacerMediumLarge)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFBBF24),
                                            Color(0xFFF59E0B)
                                        )
                                    )
                                )
                                .padding(horizontal = Dimens.SpacerExtraLarge, vertical = Dimens.SpacerMediumLarge)
                        ) {
                            Text(
                                text = "⚡ GET READY! ⚡",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                letterSpacing = Dimens.LetterSpaceMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.SpacerMediumLarge))

                    Text(
                        text = "CHALLENGE STARTS IN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = Dimens.LetterSpaceMedium
                    )

                    Spacer(modifier = Modifier.height(Dimens.SpacerLarge))

                    // Animated progress indicator
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.alpha(glowAlpha)
                    ) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .size(Dimens.SpacerSmall)
                                    .background(
                                        Color(0xFFEC4899),
                                        shape = CircleShape
                                    )
                            )
                            if (index < 2) {
                                Spacer(modifier = Modifier.width(Dimens.SpacerSmall))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(Dimens.PaddingUltraLarge))

                // Timer Card
                Card(
                    modifier = Modifier
                        .shadow(
                            elevation = Dimens.SpacerMediumLarge,
                            shape = RoundedCornerShape(Dimens.SpacerExtraLarge),
                            spotColor = Color(0xFF9333EA)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(Dimens.SpacerExtraLarge)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF9333EA).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(Dimens.PaddingExtraLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = formatMs(remainingMs),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = Dimens.TextSizeUltraExtraLarge
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBg, Color(0xFF1E1B4B), DarkBg)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .shadow(Dimens.SpacerLarge, RoundedCornerShape(Dimens.SpacerExtraLarge), spotColor = GamePurple)
                .background(CardBg, RoundedCornerShape(Dimens.SpacerExtraLarge))
                .padding(Dimens.PaddingUltraLarge)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(Dimens.OptionCardHeight),
                color = GamePurple,
                strokeWidth = Dimens.BorderWidthLarge
            )
            Spacer(modifier = Modifier.height(Dimens.SpacerExtraLarge))
            Text(
                text = "LOADING QUESTION...",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = Dimens.LetterSpaceMedium
            )
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
    var savedAnswer by remember { mutableStateOf<Answer?>(null) }

    LaunchedEffect(question.index) {
        try {
            val ans = viewModel.getSavedAnswer(question.index)
            savedAnswer = ans
        } catch (t: Throwable) { }
    }

    val options: List<Country> = remember(question.options) {
        val json = Json { ignoreUnknownKeys = true }
        try {
            val answerOptions = question.options

            // Deduplicate by ID
            answerOptions.distinctBy { it.id }
        } catch (t: Throwable) {
            emptyList()
        }
    }

    val isCorrectAnswer = savedAnswer?.isCorrect == true
    val landscape = isLandscape()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBg, Color(0xFF1E1B4B), DarkBg)
                )
            )
    ) {
        if (!landscape) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Dimens.SpacerMediumLarge)
            ) {
                // Result Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = Dimens.SpacerMediumLarge,
                            shape = RoundedCornerShape(Dimens.SpacerExtraLarge),
                            spotColor = if (isCorrectAnswer) GameGreen else GameRed
                        ),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(Dimens.SpacerExtraLarge)
                ) {
                    Box(
                        modifier = Modifier.background(
                            Brush.radialGradient(
                                colors = listOf(
                                    if (isCorrectAnswer) GameGreen.copy(alpha = 0.3f) else GameRed.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.SpacerExtraLarge),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (isCorrectAnswer) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.IconSizeUltra),
                                tint = if (isCorrectAnswer) GameGreen else GameRed
                            )

                            Spacer(modifier = Modifier.height(Dimens.SpacerLarge))

                            Text(
                                text = if (isCorrectAnswer) "CORRECT!" else "INCORRECT",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isCorrectAnswer) GameGreen else GameRed,
                                letterSpacing = Dimens.LetterSpaceMedium
                            )

                            Spacer(modifier = Modifier.height(Dimens.SpacerMedium))

                            Surface(
                                shape = RoundedCornerShape(Dimens.SpacerMedium),
                                color = GameBlue.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = Dimens.SpacerLarge, vertical = Dimens.SpacerSmall),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = GameBlue,
                                        modifier = Modifier.size(Dimens.SpacerMediumLarge)
                                    )
                                    Spacer(modifier = Modifier.width(Dimens.SpacerSmall))
                                    val isLastQuestion = question.index + 1 == totalQuestions

                                    Text(
                                        text = if (isLastQuestion) "Ends in ${formatMs(remainingMs)}"
                                        else "Next in ${formatMs(remainingMs)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = GameBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.SpacerExtraLarge))

                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacerMedium)) {
                    options.forEachIndexed { index, opt ->
                        val isCorrect = opt.id == question.correctAnswerId
                        val isSelected = savedAnswer?.selectedOptionId == opt.id

                        Column {
                            StyledReviewOptionCard(
                                text = opt.country_name,
                                index = index,
                                isSelected = isSelected,
                                isCorrect = isCorrect,
                                isWrong = isSelected && !isCorrect
                            )

                            // Show label below the option
                            if (isSelected && isCorrect) {
                                // User selected correct answer
                                Text(
                                    text = "Correct",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GameGreen,
                                    modifier = Modifier.padding(start = Dimens.SpacerLarge, top = Dimens.SpacerExtraSmall)
                                )
                            } else if (isSelected && !isCorrect) {
                                // User selected wrong answer
                                Text(
                                    text = "Wrong",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GameRed,
                                    modifier = Modifier.padding(start = Dimens.SpacerLarge, top = Dimens.SpacerExtraSmall)
                                )
                            } else if (!isSelected && isCorrect && !isCorrectAnswer) {
                                // Show correct answer when user was wrong
                                Text(
                                    text = "Correct",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GameGreen,
                                    modifier = Modifier.padding(start = Dimens.SpacerLarge, top = Dimens.SpacerExtraSmall)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Landscape layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.SpacerLarge),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacerLarge)
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .shadow(
                                elevation = Dimens.SpacerMediumLarge,
                                shape = RoundedCornerShape(Dimens.SpacerExtraLarge),
                                spotColor = if (isCorrectAnswer) GameGreen else GameRed
                            ),
                        colors = CardDefaults.cardColors(containerColor = CardBg)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            if (isCorrectAnswer) GameGreen.copy(alpha = 0.3f) else GameRed.copy(alpha = 0.3f),
                                            Color.Transparent
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isCorrectAnswer) Icons.Default.CheckCircle else Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(Dimens.IconSizeUltra),
                                    tint = if (isCorrectAnswer) GameGreen else GameRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.SpacerLarge))

                    Text(
                        text = if (isCorrectAnswer) "CORRECT!" else "INCORRECT",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCorrectAnswer) GameGreen else GameRed,
                        letterSpacing = Dimens.LetterSpaceMedium
                    )

                    Spacer(modifier = Modifier.height(Dimens.SpacerMedium))

                    val isLastQuestion = question.index + 1 == totalQuestions

                    Text(
                        text = if (isLastQuestion) "Ends in ${formatMs(remainingMs)}"
                        else "Next in ${formatMs(remainingMs)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GameBlue,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(0.55f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacerMedium)) {
                        options.forEachIndexed { index, opt ->
                            val isCorrect = opt.id == question.correctAnswerId
                            val isSelected = savedAnswer?.selectedOptionId == opt.id

                            Column {
                                StyledReviewOptionCard(
                                    text = opt.country_name,
                                    index = index,
                                    isSelected = isSelected,
                                    isCorrect = isCorrect,
                                    isWrong = isSelected && !isCorrect
                                )

                                // Show label below the option
                                if (isSelected && isCorrect) {
                                    // User selected correct answer
                                    Text(
                                        text = "Correct",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GameGreen,
                                        modifier = Modifier.padding(start = Dimens.SpacerLarge, top = Dimens.SpacerExtraSmall)
                                    )
                                } else if (isSelected && !isCorrect) {
                                    // User selected wrong answer
                                    Text(
                                        text = "Wrong",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GameRed,
                                        modifier = Modifier.padding(start = Dimens.SpacerLarge, top = Dimens.SpacerExtraSmall)
                                    )
                                } else if (!isSelected && isCorrect && !isCorrectAnswer) {
                                    // Show correct answer when user was wrong
                                    Text(
                                        text = "Correct",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GameGreen,
                                        modifier = Modifier.padding(start = Dimens.SpacerLarge, top = Dimens.SpacerExtraSmall)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntervalPlaceholder(remainingMs: Long) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBg, Color(0xFF1E1B4B), DarkBg)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(Dimens.PaddingExtraLarge)
                .shadow(Dimens.SpacerMediumLarge, RoundedCornerShape(Dimens.SpacerExtraLarge), spotColor = GameBlue),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(Dimens.SpacerExtraLarge)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GameBlue.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(Dimens.PaddingUltraLarge),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier
                            .size(Dimens.IconSizeUltra)
                            .scale(pulseScale),
                        tint = GameBlue
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacerExtraLarge))
                    Text(
                        text = "NEXT QUESTION",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = Dimens.LetterSpaceMedium
                    )
                    Spacer(modifier = Modifier.height(Dimens.SpacerLarge))
                    Text(
                        text = formatMs(remainingMs),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = GameBlue
                    )
                }
            }
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

    val options: List<Country> = remember(question.options) {
        try {
            val answerOptions = question.options
            // Deduplicate by ID
            answerOptions.distinctBy { it.id }
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
        } catch (_: Throwable) {}
    }

    val showResult = remainingMs <= 0L

    var initialDuration by remember(question.index) { mutableStateOf(0L) }
    var localDisplayRemaining by remember(question.index) { mutableStateOf(remainingMs) }

    LaunchedEffect(question.index, remainingMs) {
        if (initialDuration == 0L && remainingMs > 0L)
            initialDuration = remainingMs

        localDisplayRemaining = remainingMs

        while (
            viewModel.ui.value.phase == Phase.QUESTION &&
            viewModel.ui.value.remainingMs > 0L
        ) {
            delay(100L)
            localDisplayRemaining = viewModel.ui.value.remainingMs
        }
    }

    val timeProgress = if (initialDuration > 0)
        (localDisplayRemaining.toFloat() / initialDuration).coerceIn(0f, 1f)
    else 1f

    val progressColor = when {
        timeProgress > 0.5f -> GameBlue
        timeProgress > 0.25f -> GameYellow
        else -> GameRed
    }

    val landscape = isLandscape()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBg, Color(0xFF1E1B4B), DarkBg)
                )
            )
    ) {
        if (!landscape) {
            PortraitQuestionLayout(
                question = question,
                totalQuestions = totalQuestions,
                timeProgress = timeProgress,
                progressColor = progressColor,
                localDisplayRemaining = localDisplayRemaining,
                options = options,
                selectedId = selectedId,
                showResult = showResult,
                remainingMs = remainingMs,
                onOptionClick = { optId ->
                    selectedId = optId
                    scope.launch { viewModel.selectOption(question.index, optId) }
                }
            )
        } else {
            LandscapeQuestionLayout(
                question = question,
                totalQuestions = totalQuestions,
                timeProgress = timeProgress,
                progressColor = progressColor,
                localDisplayRemaining = localDisplayRemaining,
                options = options,
                selectedId = selectedId,
                showResult = showResult,
                remainingMs = remainingMs,
                onOptionClick = { optId ->
                    selectedId = optId
                    scope.launch { viewModel.selectOption(question.index, optId) }
                }
            )
        }
    }
}

@Composable
private fun PortraitQuestionLayout(
    question: UiQuestion,
    totalQuestions: Int,
    timeProgress: Float,
    progressColor: Color,
    localDisplayRemaining: Long,
    options: List<Country>,
    selectedId: Int?,
    showResult: Boolean,
    remainingMs: Long,
    onOptionClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.SpacerMediumLarge)
    ) {
        // Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(Dimens.SpacerMedium, RoundedCornerShape(Dimens.SpacerMediumLarge), spotColor = progressColor),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(Dimens.SpacerMediumLarge)
        ) {
            Box(
                modifier = Modifier.background(
                    Brush.linearGradient(
                        colors = listOf(
                            progressColor.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
            ) {
                Column(modifier = Modifier.padding(Dimens.SpacerMediumLarge)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUESTION ${question.index + 1}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = Dimens.LetterSpaceMediumSmall
                        )

                        Surface(
                            shape = RoundedCornerShape(Dimens.SpacerMedium),
                            color = progressColor.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Dimens.SpacerMedium, vertical = Dimens.PaddingExtraSmall),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${question.index + 1}/$totalQuestions",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = progressColor,
                                    letterSpacing = Dimens.LetterSpaceSmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.SpacerLarge))

                    LinearProgressIndicator(
                        progress = timeProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.SpacerSmall)
                            .clip(RoundedCornerShape(Dimens.BorderWidthLarge)),
                        color = progressColor,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )

                    Spacer(modifier = Modifier.height(Dimens.SpacerMedium))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = progressColor,
                            modifier = Modifier.size(Dimens.SpacerMediumLarge)
                        )
                        Spacer(modifier = Modifier.width(Dimens.SpacerSmall))
                        Text(
                            text = formatMs(localDisplayRemaining),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = progressColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacerExtraLarge))

        // Flag Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.FlagHeight)
                .shadow(Dimens.SpacerLarge, RoundedCornerShape(Dimens.SpacerExtraLarge), spotColor = GamePurple),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            shape = RoundedCornerShape(Dimens.SpacerExtraLarge)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                GamePurple.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val context = LocalContext.current
                val flagResName = "flag_${question.countryCode.lowercase()}"
                val flagResId = remember(question.countryCode) {
                    context.resources.getIdentifier(flagResName, "drawable", context.packageName)
                }
                if (flagResId != 0) {
                    Image(
                        painter = painterResource(id = flagResId),
                        contentDescription = "Flag",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(Dimens.IconSizeUltra),
                            tint = GamePurple
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpacerMediumLarge))
                        Text(
                            text = question.countryCode,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = Dimens.LetterSpaceLarge
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.SpacerExtraLarge))

        Text(
            text = "GUESS THE COUNTRY BY FLAG",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraBold,
            color = GameYellow,
            letterSpacing = Dimens.LetterSpaceMedium,
            modifier = Modifier.padding(bottom = Dimens.SpacerLarge)
        )

        val clickable = remainingMs > 0L

        Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacerMediumLarge)) {
            options.forEachIndexed { index, opt ->
                val isSelected = selectedId == opt.id
                val isCorrect = opt.id == question.correctAnswerId
                val showCorrectHighlight = showResult && isCorrect
                val showWrongHighlight = showResult && isSelected && !isCorrect

                StyledOptionCard(
                    text = opt.country_name,
                    index = index,
                    isSelected = isSelected,
                    isCorrect = showCorrectHighlight,
                    isWrong = showWrongHighlight,
                    isDisabled = !clickable,
                    showResult = showResult,
                    onClick = { onOptionClick(opt.id) }
                )
            }
        }
    }
}

@Composable
private fun StyledOptionCard(
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
        targetValue = if (isSelected && !showResult) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val containerColor = when {
        isCorrect -> GameGreen.copy(alpha = 0.2f)
        isWrong -> GameRed.copy(alpha = 0.2f)
        isSelected -> GamePurple.copy(alpha = 0.2f)
        else -> CardBg
    }

    val borderColor = when {
        isCorrect -> GameGreen
        isWrong -> GameRed
        isSelected -> GamePurple
        else -> Color.White.copy(alpha = 0.2f)
    }

    val gradientColors = when {
        isCorrect -> listOf(GameGreen.copy(alpha = 0.3f), GameGreen.copy(alpha = 0.1f))
        isWrong -> listOf(GameRed.copy(alpha = 0.3f), GameRed.copy(alpha = 0.1f))
        isSelected -> listOf(GamePurple.copy(alpha = 0.3f), GamePink.copy(alpha = 0.1f))
        else -> listOf(Color.Transparent, Color.Transparent)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .then(if (!isDisabled) Modifier.clickable { onClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(Dimens.SpacerLarge),
        border = BorderStroke(Dimens.SpacerExtraLargeSmall, borderColor)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(colors = gradientColors)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Dimens.SpacerMediumLarge, vertical = Dimens.SpacerSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = borderColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(Dimens.PaddingExtraLarge)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = ('A' + index).toString(),
                            fontWeight = FontWeight.ExtraBold,
                            color = borderColor,
                            fontSize = Dimens.TextSizeLargeMedium,
                            letterSpacing = Dimens.LetterSpaceSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Dimens.SpacerLarge))

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                AnimatedVisibility(
                    visible = showResult && (isCorrect || isWrong),
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isCorrect) GameGreen else GameRed,
                        modifier = Modifier.size(Dimens.PaddingExtraLarge)
                    )
                }
            }
        }
    }
}

@Composable
private fun LandscapeQuestionLayout(
    question: UiQuestion,
    totalQuestions: Int,
    timeProgress: Float,
    progressColor: Color,
    localDisplayRemaining: Long,
    options: List<Country>,
    selectedId: Int?,
    showResult: Boolean,
    remainingMs: Long,
    onOptionClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.SpacerLarge),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacerLarge)
    ) {
        // Left side - Flag and Timer
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        ) {
            LinearProgressIndicator(
                progress = timeProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.SpacerSmall)
                    .clip(RoundedCornerShape(Dimens.BorderWidthLarge)),
                color = progressColor,
                trackColor = Color.White.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(Dimens.SpacerLarge))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(Dimens.SpacerMedium, RoundedCornerShape(Dimens.SpacerMediumLarge), spotColor = progressColor),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(Dimens.SpacerMediumLarge)
            ) {
                Box(
                    modifier = Modifier.background(
                        Brush.linearGradient(
                            colors = listOf(
                                progressColor.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                ){
                    Column(modifier = Modifier.padding(Dimens.SpacerMediumLarge)) {
                        Text(
                            text = "QUESTION ${question.index + 1} / $totalQuestions",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = Dimens.LetterSpaceSmall
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpacerSmall))
                        Text(
                            text = formatMs(localDisplayRemaining),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = progressColor
                        )
                    }
                }
                  }

            Spacer(modifier = Modifier.height(Dimens.SpacerLarge))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(Dimens.SpacerLarge, RoundedCornerShape(Dimens.SpacerExtraLarge), spotColor = GamePurple),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    GamePurple.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val context = LocalContext.current
                    val flagResName = "flag_${question.countryCode.lowercase()}"
                    val flagResId = remember(question.countryCode) {
                        context.resources.getIdentifier(flagResName, "drawable", context.packageName)
                    }
                    if (flagResId != 0) {
                        Image(
                            painter = painterResource(id = flagResId),
                            contentDescription = "Flag",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }else{
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.IconSizeUltra),
                                tint = GamePurple
                            )
                            Spacer(modifier = Modifier.height(Dimens.SpacerMediumLarge))
                            Text(
                                text = question.countryCode,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Right side - Options
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "GUESS THE COUNTRY BY FLAG",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
                color = GameYellow,
                letterSpacing = Dimens.LetterSpaceMedium,
                modifier = Modifier.padding(bottom = Dimens.SpacerLarge)
            )

            val clickable = remainingMs > 0L

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacerMediumLarge)) {
                options.forEachIndexed { index, opt ->
                    val isSelected = selectedId == opt.id
                    val isCorrect = opt.id == question.correctAnswerId
                    val showCorrectHighlight = showResult && isCorrect
                    val showWrongHighlight = showResult && isSelected && !isCorrect

                    StyledOptionCard(
                        text = opt.country_name,
                        index = index,
                        isSelected = isSelected,
                        isCorrect = showCorrectHighlight,
                        isWrong = showWrongHighlight,
                        isDisabled = !clickable,
                        showResult = showResult,
                        onClick = { onOptionClick(opt.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StyledReviewOptionCard(
    text: String,
    index: Int,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean
) {
    val containerColor = when {
        isCorrect -> GameGreen.copy(alpha = 0.2f)
        isWrong -> GameRed.copy(alpha = 0.2f)
        isSelected -> GamePurple.copy(alpha = 0.2f)
        else -> Color.Transparent
    }

    val borderColor = when {
        isCorrect -> GameGreen
        isWrong -> GameRed
        isSelected -> GamePurple
        else -> Color.White.copy(alpha = 0.2f)
    }

    val gradientColors = when {
        isCorrect -> listOf(GameGreen.copy(alpha = 0.3f), GameGreen.copy(alpha = 0.1f))
        isWrong -> listOf(GameRed.copy(alpha = 0.3f), GameRed.copy(alpha = 0.1f))
        isSelected -> listOf(GamePurple.copy(alpha = 0.3f), GamePink.copy(alpha = 0.1f))
        else -> listOf(Color.Transparent, Color.Transparent)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),   // ← removed .shadow()
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(Dimens.SpacerLarge),
        border = BorderStroke(Dimens.SpacerExtraLargeSmall, borderColor)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(colors = gradientColors)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Dimens.SpacerMediumLarge, vertical = Dimens.SpacerSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = borderColor.copy(alpha = 0.3f),
                    modifier = Modifier.size(Dimens.PaddingExtraLarge)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = ('A' + index).toString(),
                            fontWeight = FontWeight.ExtraBold,
                            color = borderColor,
                            fontSize = Dimens.TextSizeLargeMedium,
                            letterSpacing = Dimens.LetterSpaceSmall
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Dimens.SpacerLarge))

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                if (isCorrect || isWrong) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isCorrect) GameGreen else GameRed,
                        modifier = Modifier.size(Dimens.PaddingExtraLarge)
                    )
                }
            }
        }
    }
}

@Composable
fun FinishedScreen(
    finalScore: Int,
    totalQuestions: Int,
    viewModel: ChallengeViewModel,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val safeTotal = if (totalQuestions > 0) totalQuestions else 1
    val percentage = ((finalScore.toFloat() / safeTotal) * 100).toInt().coerceIn(0, 100)
    val isPassed = percentage >= ChallengeConstants.SUCCESS_PERCENTAGE

    // Animations matching ScheduleScreen
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkBg,
                        Color(0xFF1E1B4B),
                        DarkBg
                    )
                )
            )
    ) {
        if (isLandscape) {
            LandscapeFinishedLayout(
                finalScore = finalScore,
                totalQuestions = totalQuestions,
                percentage = percentage,
                isPassed = isPassed,
                pulseScale = pulseScale,
                floatingOffset = floatingOffset,
                onTryAgain = { viewModel.resetChallenge() }
            )
        } else {
            PortraitFinishedLayout(
                finalScore = finalScore,
                totalQuestions = totalQuestions,
                percentage = percentage,
                isPassed = isPassed,
                pulseScale = pulseScale,
                floatingOffset = floatingOffset,
                onTryAgain = { viewModel.resetChallenge() }
            )
        }
    }
}

@Composable
private fun PortraitFinishedLayout(
    finalScore: Int,
    totalQuestions: Int,
    percentage: Int,
    isPassed: Boolean,
    pulseScale: Float,
    floatingOffset: Float,
    onTryAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.SpacerExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FinishedHeaderSection(
            isPassed = isPassed,
            pulseScale = pulseScale,
            floatingOffset = floatingOffset,
            isCompact = false
        )

        Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))

        ScoreCard(
            finalScore = finalScore,
            totalQuestions = totalQuestions,
            percentage = percentage,
            isPassed = isPassed,
            isCompact = false
        )

        Spacer(modifier = Modifier.height(Dimens.PaddingExtraLarge))

        TryAgainButton(onTryAgain = onTryAgain)
    }
}

@Composable
private fun LandscapeFinishedLayout(
    finalScore: Int,
    totalQuestions: Int,
    percentage: Int,
    isPassed: Boolean,
    pulseScale: Float,
    floatingOffset: Float,
    onTryAgain: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.PaddingExtraLarge, vertical = Dimens.SpacerLarge),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacerExtraLarge),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FinishedHeaderSection(
                isPassed = isPassed,
                pulseScale = pulseScale,
                floatingOffset = floatingOffset,
                isCompact = true
            )

            Spacer(modifier = Modifier.height(Dimens.SpacerExtraLarge))

            TryAgainButton(onTryAgain = onTryAgain)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ScoreCard(
                finalScore = finalScore,
                totalQuestions = totalQuestions,
                percentage = percentage,
                isPassed = isPassed,
                isCompact = true
            )
        }
    }
}

@Composable
private fun FinishedHeaderSection(
    isPassed: Boolean,
    pulseScale: Float,
    floatingOffset: Float,
    isCompact: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = if (isCompact) Dimens.PaddingZero else Dimens.PaddingExtraLarge)
    ) {
        // Animated icon
        Box(
            modifier = Modifier
                .offset(y = (-floatingOffset).dp)
                .shadow(
                    Dimens.SpacerLarge,
                    shape = RoundedCornerShape(50),
                    spotColor = if (isPassed) GameGreen else GameYellow
                )
                .background(
                    if (isPassed) GameGreen.copy(alpha = 0.2f) else GameYellow.copy(alpha = 0.2f),
                    RoundedCornerShape(50)
                )
                .padding(Dimens.SpacerLarge)
        ) {
            Icon(
                imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier
                    .size(if (isCompact) Dimens.IconSizeExtraLarge else Dimens.IconSizeUltra)
                    .scale(pulseScale),
                tint = if (isPassed) GameGreen else GameYellow
            )
        }

        Spacer(modifier = Modifier.height(if (isCompact) Dimens.SpacerMedium else Dimens.SpacerLarge))

        Text(
            text = "GAME OVER",
            style = if (isCompact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            letterSpacing = Dimens.LetterSpaceMedium,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.padding(top = if (isCompact) Dimens.PaddingExtraSmall else Dimens.SpacerSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier
                    .size(if (isCompact) Dimens.SpacerLarge else Dimens.SpacerMediumLarge)
                    .scale(pulseScale),
                tint = if (isPassed) GameGreen else GameYellow
            )
            Spacer(modifier = Modifier.width(Dimens.PaddingExtraSmall))
            Text(
                text = if (isPassed) "EXCELLENT WORK!" else "KEEP PRACTICING!",
                style = if (isCompact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                color = if (isPassed) GameGreen else GameYellow,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = Dimens.LetterSpaceSmall
            )
        }
    }
}

@Composable
private fun ScoreCard(
    finalScore: Int,
    totalQuestions: Int,
    percentage: Int,
    isPassed: Boolean,
    isCompact: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(Dimens.SpacerMedium, RoundedCornerShape(Dimens.SpacerMediumLarge), spotColor = GamePurple),
        colors = CardDefaults.cardColors(
            containerColor = CardBg
        ),
        shape = RoundedCornerShape(Dimens.SpacerMediumLarge)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            GamePurple.copy(alpha = 0.2f),
                            GamePink.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(if (isCompact) Dimens.SpacerMediumLarge else Dimens.SpacerExtraLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            if (isPassed) GameGreen.copy(alpha = 0.2f) else GameYellow.copy(alpha = 0.2f),
                            RoundedCornerShape(Dimens.SpacerSmall)
                        )
                        .padding(horizontal = Dimens.SpacerMedium, vertical = Dimens.PaddingExtraSmall)
                ) {
                    Icon(
                        imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Refresh,
                        contentDescription = null,
                        tint = if (isPassed) GameGreen else GameYellow,
                        modifier = Modifier.size(Dimens.SpacerLarge)
                    )
                    Spacer(modifier = Modifier.width(Dimens.PaddingExtraSmall))
                    Text(
                        text = if (isPassed) "PASSED" else "TRY AGAIN",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isPassed) GameGreen else GameYellow,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = Dimens.LetterSpaceSmall
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpacerLarge))

                // Score display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    GameBlue.copy(alpha = 0.3f),
                                    GamePurple.copy(alpha = 0.3f)
                                )
                            ),
                            RoundedCornerShape(Dimens.SpacerMedium)
                        )
                        .padding(Dimens.SpacerMediumLarge),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "YOUR SCORE",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = Dimens.LetterSpaceSmall
                        )
                        Spacer(modifier = Modifier.height(Dimens.SpacerSmall))
                        Text(
                            text = "$finalScore / $totalQuestions",
                            style = if (isCompact) MaterialTheme.typography.displayMedium else MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.SpacerLarge))

                // Percentage display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    GamePink.copy(alpha = 0.3f),
                                    GamePurple.copy(alpha = 0.3f)
                                )
                            ),
                            RoundedCornerShape(Dimens.SpacerMedium)
                        )
                        .padding(Dimens.SpacerLarge),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ACCURACY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$percentage%",
                        style = if (isCompact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPassed) GameGreen else GameYellow
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.SpacerMedium))

                // Progress bar
                LinearProgressIndicator(
                    progress = { (finalScore.toFloat() / totalQuestions).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.SpacerMedium)
                        .clip(RoundedCornerShape(Dimens.PaddingExtraSmall)),
                    color = if (isPassed) GameGreen else GameYellow,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun TryAgainButton(onTryAgain: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    Button(
        onClick = {
            isPressed = true
            onTryAgain()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.ButtonHeightExtraLarge)
            .shadow(
                elevation = if (isPressed) Dimens.SpacerExtraSmall else Dimens.SpacerMedium,
                shape = RoundedCornerShape(Dimens.SpacerMediumLarge),
                spotColor = GamePurple
            )
            .scale(if (isPressed) 0.98f else 1f),
        shape = RoundedCornerShape(Dimens.SpacerMediumLarge),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(Dimens.PaddingZero)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            GamePurple,
                            GamePink,
                            GameBlue
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.SpacerExtraLarge),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(Dimens.SpacerMedium))
                Text(
                    text = "TRY AGAIN",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = Dimens.LetterSpaceMediumSmall
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}