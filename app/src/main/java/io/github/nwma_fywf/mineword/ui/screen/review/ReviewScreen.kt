package io.github.nwma_fywf.mineword.ui.screen.review

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.R
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.ui.screen.quiz.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onStartReview: (QuizViewModel.QuizMode) -> Unit,
) {
    val reviewWords by viewModel.reviewWords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val dailyGoalProgress by viewModel.dailyGoalProgress.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_review)) }
            )
        },
        floatingActionButton = {
            if (reviewWords.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { onStartReview(QuizViewModel.QuizMode.REVIEW) },
                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                    text = { Text(stringResource(R.string.start_review)) }
                )
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (reviewWords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_words_to_review),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    DailyGoalProgressCard(
                        newWordsToday = dailyGoalProgress.newWordsToday,
                        newWordGoal = dailyGoalProgress.newWordGoal,
                        reviewWordsToday = dailyGoalProgress.reviewWordsToday,
                        reviewGoal = dailyGoalProgress.reviewGoal
                    )
                }

                val overdueCount = reviewWords.count { it.status == ReviewStatus.OVERDUE }
                val todayCount = reviewWords.count { it.status == ReviewStatus.DUE_TODAY }
                val tomorrowCount = reviewWords.count { it.status == ReviewStatus.DUE_TOMORROW }

                item {
                    ReviewSummaryCard(
                        overdueCount = overdueCount,
                        todayCount = todayCount,
                        tomorrowCount = tomorrowCount,
                        totalCount = reviewWords.size
                    )
                }

                items(
                    items = reviewWords,
                    key = { it.word.id }
                ) { reviewWord ->
                    ReviewWordCard(
                        word = reviewWord.word,
                        meanings = reviewWord.meanings,
                        status = reviewWord.status,
                        daysUntil = reviewWord.daysUntil,
                        onSkip = { viewModel.skipWord(reviewWord.word.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun DailyGoalProgressCard(
    newWordsToday: Int,
    newWordGoal: Int,
    reviewWordsToday: Int,
    reviewGoal: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoalProgressItem(
                current = newWordsToday,
                goal = newWordGoal,
                label = stringResource(R.string.stats_new_words_short),
                color = MaterialTheme.colorScheme.primary
            )
            
            GoalProgressItem(
                current = reviewWordsToday,
                goal = reviewGoal,
                label = stringResource(R.string.stats_reviewed_short),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun GoalProgressItem(
    current: Int,
    goal: Int,
    label: String,
    color: Color
) {
    val progress = if (goal > 0) (current.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val isCompleted = current >= goal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(56.dp)
        ) {
            CircularProgressIndicator(
                progress = progress,
                color = if (isCompleted) MaterialTheme.colorScheme.primary else color,
                backgroundColor = color.copy(alpha = 0.2f),
                strokeWidth = 4.dp,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = "$current",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCompleted) MaterialTheme.colorScheme.primary else color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = "/ $goal",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun CircularProgressIndicator(
    progress: Float,
    color: Color,
    backgroundColor: Color,
    strokeWidth: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val sweepAngle = 360f * progress
        drawArc(
            color = backgroundColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun ReviewSummaryCard(
    overdueCount: Int,
    todayCount: Int,
    tomorrowCount: Int,
    totalCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.review_summary_total, totalCount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(count = overdueCount, label = stringResource(R.string.status_overdue), color = MaterialTheme.colorScheme.error)
                SummaryItem(count = todayCount, label = stringResource(R.string.status_today), color = MaterialTheme.colorScheme.primary)
                SummaryItem(count = tomorrowCount, label = stringResource(R.string.status_tomorrow), color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun SummaryItem(count: Int, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ReviewWordCard(
    word: Word,
    meanings: List<Meaning>,
    status: ReviewStatus,
    daysUntil: Int,
    onSkip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusIndicator(status = status)
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = word.word,
                    style = MaterialTheme.typography.titleMedium
                )
                if (meanings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = meanings.first().definition,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (status) {
                        ReviewStatus.OVERDUE -> stringResource(R.string.status_overdue)
                        ReviewStatus.DUE_TODAY -> stringResource(R.string.status_due_today)
                        ReviewStatus.DUE_TOMORROW -> stringResource(R.string.status_due_tomorrow)
                        ReviewStatus.DUE_DAYS -> stringResource(R.string.status_days_until_due, daysUntil)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when (status) {
                        ReviewStatus.OVERDUE -> MaterialTheme.colorScheme.error
                        ReviewStatus.DUE_TODAY -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator(status: ReviewStatus) {
    val (color, icon) = when (status) {
        ReviewStatus.OVERDUE -> MaterialTheme.colorScheme.error to Icons.Filled.Warning
        ReviewStatus.DUE_TODAY -> MaterialTheme.colorScheme.primary to Icons.Filled.CheckCircle
        ReviewStatus.DUE_TOMORROW -> MaterialTheme.colorScheme.secondary to Icons.Filled.CheckCircle
        ReviewStatus.DUE_DAYS -> MaterialTheme.colorScheme.onSurfaceVariant to Icons.Filled.CheckCircle
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
        tint = color
    )
}
