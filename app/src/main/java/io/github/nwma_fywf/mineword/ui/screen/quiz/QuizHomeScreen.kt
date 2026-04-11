package io.github.nwma_fywf.mineword.ui.screen.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizHomeScreen(
    onSelectMode: (QuizViewModel.QuizMode, Int) -> Unit,
    onNavigateToWrongAnswer: () -> Unit,
    viewModel: QuizHomeViewModel? = null,
) {
    val dueReviewCount by viewModel?.dueReviewCount?.collectAsState(initial = 0) ?: remember { mutableStateOf(0) }
    var showCountDialog by remember { mutableStateOf(false) }
    var pendingMode by remember { mutableStateOf<QuizViewModel.QuizMode?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.mode_selection)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val modes = QuizViewModel.QuizMode.entries.filter { it != QuizViewModel.QuizMode.REVIEW && it != QuizViewModel.QuizMode.QUIZ_WRONG_ANSWERS }
            modes.chunked(2).forEach { rowModes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    rowModes.forEach { mode ->
                        ModeCard(
                            mode = mode,
                            onClick = {
                                pendingMode = mode
                                showCountDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowModes.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Card(
                onClick = onNavigateToWrongAnswer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = stringResource(R.string.wrong_answer_book),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }

    if (showCountDialog && pendingMode != null) {
        QuizCountDialog(
            onDismiss = {
                showCountDialog = false
                pendingMode = null
            },
            onConfirm = { count ->
                pendingMode?.let { mode ->
                    onSelectMode(mode, count)
                }
                showCountDialog = false
                pendingMode = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeCard(
    mode: QuizViewModel.QuizMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when (mode) {
                    QuizViewModel.QuizMode.EN_TO_CN -> stringResource(R.string.quiz_en_to_cn)
                    QuizViewModel.QuizMode.CN_TO_EN -> stringResource(R.string.quiz_cn_to_en)
                    QuizViewModel.QuizMode.CHOICE_EN_TO_CN -> stringResource(R.string.quiz_choice_cn)
                    QuizViewModel.QuizMode.CHOICE_CN_TO_EN -> stringResource(R.string.quiz_choice_en)
                    QuizViewModel.QuizMode.REVIEW -> stringResource(R.string.quiz_review_mode)
                    QuizViewModel.QuizMode.QUIZ_WRONG_ANSWERS -> ""
                },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (mode) {
                    QuizViewModel.QuizMode.EN_TO_CN -> stringResource(R.string.desc_en_to_cn)
                    QuizViewModel.QuizMode.CN_TO_EN -> stringResource(R.string.desc_cn_to_en)
                    QuizViewModel.QuizMode.CHOICE_EN_TO_CN -> stringResource(R.string.desc_choice_en_to_cn)
                    QuizViewModel.QuizMode.CHOICE_CN_TO_EN -> stringResource(R.string.desc_choice_cn_to_en)
                    QuizViewModel.QuizMode.REVIEW -> stringResource(R.string.desc_review_mode)
                    QuizViewModel.QuizMode.QUIZ_WRONG_ANSWERS -> ""
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuizCountDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    val countOptions = listOf(10, 20, 30, 50)
    var selectedCount by remember { mutableStateOf(20) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_quiz_count)) },
        text = {
            Column(Modifier.selectableGroup()) {
                countOptions.forEach { count ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = selectedCount == count,
                                onClick = { selectedCount = count },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCount == count,
                            onClick = null
                        )
                        Text(
                            text = "$count ${stringResource(R.string.questions)}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .selectable(
                            selected = selectedCount == -1,
                            onClick = { selectedCount = -1 },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedCount == -1,
                        onClick = null
                    )
                    Text(
                        text = stringResource(R.string.all_questions),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedCount) }) {
                Text(stringResource(R.string.start_quiz))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
