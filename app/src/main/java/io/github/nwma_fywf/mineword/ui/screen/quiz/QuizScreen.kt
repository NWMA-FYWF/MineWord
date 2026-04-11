package io.github.nwma_fywf.mineword.ui.screen.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.R
import io.github.nwma_fywf.mineword.data.local.Word

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onNavigateBack: () -> Unit,
) {
    val currentWord by viewModel.currentWord.collectAsState()
    val currentMeanings by viewModel.currentMeanings.collectAsState()
    val userInput by viewModel.userInput.collectAsState()
    val quizState by viewModel.quizState.collectAsState()
    val quizMode by viewModel.quizMode.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val quizCountLimit by viewModel.quizCountLimit.collectAsState()
    var showExitConfirm by remember { mutableStateOf(false) }

    val showProgress = quizCountLimit > 0 && quizState !is QuizViewModel.QuizState.Idle && quizState !is QuizViewModel.QuizState.Finished

    BackHandler(enabled = quizState !is QuizViewModel.QuizState.Finished && quizState !is QuizViewModel.QuizState.Idle) {
        showExitConfirm = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (quizMode) {
                            QuizViewModel.QuizMode.EN_TO_CN -> stringResource(R.string.quiz_en_to_cn)
                            QuizViewModel.QuizMode.CN_TO_EN -> stringResource(R.string.quiz_cn_to_en)
                            QuizViewModel.QuizMode.CHOICE_EN_TO_CN -> stringResource(R.string.quiz_choice_cn)
                            QuizViewModel.QuizMode.CHOICE_CN_TO_EN -> stringResource(R.string.quiz_choice_en)
                            QuizViewModel.QuizMode.REVIEW -> stringResource(R.string.quiz_review_mode)
                            QuizViewModel.QuizMode.QUIZ_WRONG_ANSWERS -> stringResource(R.string.quiz_wrong_answers)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showExitConfirm = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showProgress) {
                    val progress = totalCount.toFloat() / quizCountLimit.toFloat()
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    Text(
                        text = stringResource(R.string.quiz_progress, totalCount, quizCountLimit),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    when (val state = quizState) {
                        is QuizViewModel.QuizState.Idle -> {
                            Text(
                                text = stringResource(R.string.no_words_for_quiz),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        is QuizViewModel.QuizState.WaitingInput -> {
                            currentWord?.let { word ->
                                WordQuizContent(
                                    word = word,
                                    meanings = currentMeanings,
                                    mode = quizMode,
                                    userInput = userInput,
                                    totalCount = totalCount,
                                    onUserInputChanged = viewModel::onUserInputChanged,
                                    onSubmit = viewModel::submitAnswer,
                                    onSkip = viewModel::skipQuestion,
                                    onFinish = { showExitConfirm = true },
                                )
                            }
                        }
                        is QuizViewModel.QuizState.WaitingInputDegraded -> {
                            WordQuizContent(
                                word = state.word,
                                meanings = currentMeanings,
                                mode = state.mode,
                                userInput = userInput,
                                totalCount = totalCount,
                                onUserInputChanged = viewModel::onUserInputChanged,
                                onSubmit = viewModel::submitAnswer,
                                onSkip = viewModel::skipQuestion,
                                onFinish = { showExitConfirm = true },
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(state.messageResId),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        is QuizViewModel.QuizState.UserJudgment -> {
                            UserJudgmentContent(
                                word = state.word,
                                userInput = state.userInput,
                                existingMeanings = state.existingMeanings,
                                onCorrect = viewModel::userJudgmentCorrect,
                                onIncorrect = viewModel::userJudgmentIncorrect,
                            )
                        }
                        is QuizViewModel.QuizState.WaitingChoice -> {
                            ChoiceQuizContent(
                                state = state,
                                mode = quizMode,
                                meanings = currentMeanings,
                                totalCount = totalCount,
                                onSelectOption = viewModel::selectChoiceOption,
                                onNext = viewModel::nextChoiceWord,
                                onFinish = { showExitConfirm = true },
                            )
                        }
                        is QuizViewModel.QuizState.Incorrect -> {
                            currentWord?.let { word ->
                                IncorrectContent(
                                    word = word,
                                    mode = quizMode,
                                    userInput = state.userInput,
                                    onNext = viewModel::nextWord,
                                )
                            }
                        }
                        is QuizViewModel.QuizState.Finished -> {
                            QuizFinishedContent(
                                correctCount = state.correctCount,
                                wrongCount = state.wrongCount,
                                onRestart = viewModel::resetQuiz,
                                onExit = onNavigateBack,
                            )
                        }
                    }
                }
            }
        }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text(stringResource(R.string.exit_quiz_confirm_title)) },
            text = { Text(stringResource(R.string.exit_quiz_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirm = false
                    onNavigateBack()
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun WordQuizContent(
    word: Word,
    meanings: List<io.github.nwma_fywf.mineword.data.local.Meaning>,
    mode: QuizViewModel.QuizMode,
    userInput: String,
    totalCount: Int,
    onUserInputChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit,
) {
    when (mode) {
        QuizViewModel.QuizMode.EN_TO_CN -> {
            Text(
                text = word.word,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )
        }
        QuizViewModel.QuizMode.CN_TO_EN -> {
            if (meanings.isNotEmpty()) {
                Text(
                    text = meanings.first().definition,
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                )
            } else {
                Text(
                    text = stringResource(R.string.no_meaning_short),
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        else -> {
            Text(
                text = stringResource(R.string.choice_mode_label),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.answered_count, totalCount),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(24.dp))
    OutlinedTextField(
        value = userInput,
        onValueChange = onUserInputChanged,
        label = {
            Text(
                when (mode) {
                    QuizViewModel.QuizMode.EN_TO_CN -> stringResource(R.string.input_meaning)
                    QuizViewModel.QuizMode.CN_TO_EN -> stringResource(R.string.input_word)
                    else -> stringResource(R.string.input_answer)
                }
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = onSubmit,
        enabled = userInput.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.submit))
    }
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedButton(
        onClick = onSkip,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.skip_question))
    }
    }

@Composable
private fun UserJudgmentContent(
    word: Word,
    userInput: String,
    existingMeanings: List<io.github.nwma_fywf.mineword.data.local.Meaning>,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
) {
    Text(
        text = stringResource(R.string.meaning_not_match),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.secondary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.your_meaning, userInput),
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.recorded_meaning),
        style = MaterialTheme.typography.bodyLarge,
    )
    existingMeanings.forEach { meaning ->
        Text(
            text = meaning.definition,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.judge_meaning),
        style = MaterialTheme.typography.bodyLarge,
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onCorrect,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.correct_add_meaning))
    }
    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = onIncorrect,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.incorrect))
    }
}

@Composable
private fun IncorrectContent(
    word: Word,
    mode: QuizViewModel.QuizMode,
    userInput: String,
    onNext: () -> Unit,
) {
    Text(
        text = stringResource(R.string.answer_wrong),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.error,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.your_answer, userInput),
        style = MaterialTheme.typography.bodyMedium,
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = stringResource(R.string.correct_answer),
        style = MaterialTheme.typography.bodyLarge,
    )
    Text(
        text = word.word,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = onNext,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.next_word))
    }
}

@Composable
private fun ChoiceQuizContent(
    state: QuizViewModel.QuizState.WaitingChoice,
    mode: QuizViewModel.QuizMode,
    meanings: List<io.github.nwma_fywf.mineword.data.local.Meaning>,
    totalCount: Int,
    onSelectOption: (QuizViewModel.ChoiceOption) -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
) {
    Text(
        text = when (mode) {
            QuizViewModel.QuizMode.CHOICE_EN_TO_CN -> state.word.word
            QuizViewModel.QuizMode.CHOICE_CN_TO_EN -> meanings.firstOrNull()?.definition ?: ""
            else -> ""
        },
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.answered_count, totalCount),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(24.dp))

    state.options.forEach { option ->
        val buttonColors = when {
            state.isCorrectAnswered == null -> ButtonDefaults.outlinedButtonColors()
            option.isCorrect -> ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            else -> ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        OutlinedButton(
            onClick = { onSelectOption(option) },
            enabled = state.isCorrectAnswered == null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = buttonColors,
        ) {
            Text(
                text = option.text,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }

    if (state.isCorrectAnswered != null) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (state.isCorrectAnswered) stringResource(R.string.answer_correct) else stringResource(R.string.answer_incorrect),
            style = MaterialTheme.typography.titleMedium,
            color = if (state.isCorrectAnswered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.next_word))
        }
    }
}

@Composable
private fun QuizFinishedContent(
    correctCount: Int,
    wrongCount: Int,
    onRestart: () -> Unit,
    onExit: () -> Unit,
) {
    val total = correctCount + wrongCount
    val percentage = if (total > 0) (correctCount * 100 / total) else 0

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.quiz_finished),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "$correctCount / $total",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.accuracy_rate, percentage),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.retry_quiz))
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.exit))
        }
    }
}
