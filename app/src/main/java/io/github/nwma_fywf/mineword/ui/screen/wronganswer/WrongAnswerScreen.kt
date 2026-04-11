package io.github.nwma_fywf.mineword.ui.screen.wronganswer

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WrongAnswerScreen(
    viewModel: WrongAnswerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    onNavigateToQuiz: () -> Unit,
) {
    val wrongAnswersWithWords by viewModel.wrongAnswersWithWords.collectAsState()
    val currentFilter by viewModel.modeFilter.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wrong_answer_book)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (wrongAnswersWithWords.isNotEmpty() || currentFilter != QuizModeFilter.ALL) {
                        Box {
                            IconButton(onClick = { showFilterMenu = true }) {
                                Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.filter))
                            }
                            DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filter_all)) },
                                    onClick = {
                                        viewModel.setModeFilter(QuizModeFilter.ALL)
                                        showFilterMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.quiz_en_to_cn)) },
                                    onClick = {
                                        viewModel.setModeFilter(QuizModeFilter.EN_TO_CN)
                                        showFilterMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.quiz_cn_to_en)) },
                                    onClick = {
                                        viewModel.setModeFilter(QuizModeFilter.CN_TO_EN)
                                        showFilterMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.quiz_choice_cn)) },
                                    onClick = {
                                        viewModel.setModeFilter(QuizModeFilter.CHOICE_EN_TO_CN)
                                        showFilterMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.quiz_choice_en)) },
                                    onClick = {
                                        viewModel.setModeFilter(QuizModeFilter.CHOICE_CN_TO_EN)
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                    }
                    if (wrongAnswersWithWords.isNotEmpty()) {
                        IconButton(onClick = onNavigateToQuiz) {
                            Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.quiz_wrong_answers))
                        }
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_wrong_answers))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (wrongAnswersWithWords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (currentFilter == QuizModeFilter.ALL) {
                        stringResource(R.string.no_wrong_answers)
                    } else {
                        stringResource(R.string.no_wrong_answers_for_mode)
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(wrongAnswersWithWords, key = { it.wrongAnswer.id }) { item ->
                    WrongAnswerCard(
                        item = item,
                        onDelete = { viewModel.deleteWrongAnswer(item.wrongAnswer.id) },
                        onClick = { item.word?.id?.let { onNavigateToWordDetail(it) } }
                    )
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_wrong_answer_title)) },
            text = { Text(stringResource(R.string.clear_wrong_answer_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllWrongAnswers()
                        showClearDialog = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun WrongAnswerCard(
    item: WrongAnswerWithWord,
    onDelete: () -> Unit,
    onClick: () -> Unit,
) {
    val wrongAnswer = item.wrongAnswer
    val word = item.word

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = word?.word ?: stringResource(R.string.word_deleted),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.your_answer_label, wrongAnswer.userAnswer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.correct_answer_label, wrongAnswer.correctAnswer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(formatQuizModeRes(wrongAnswer.quizMode)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            val timeText = try {
                val timestamp = wrongAnswer.timestamp
                if (timestamp > 0) {
                    val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                    date.format(java.util.Date(timestamp))
                } else ""
            } catch (e: Exception) { "" }

            if (timeText.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.wrong_time_label, timeText),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@StringRes
private fun formatQuizModeRes(mode: String): Int {
    return when (mode) {
        "EN_TO_CN" -> R.string.mode_en_to_cn
        "CN_TO_EN" -> R.string.mode_cn_to_en
        "CHOICE_EN_TO_CN" -> R.string.mode_choice_cn
        "CHOICE_CN_TO_EN" -> R.string.mode_choice_en
        "REVIEW" -> R.string.mode_review
        else -> R.string.mode_en_to_cn
    }
}
