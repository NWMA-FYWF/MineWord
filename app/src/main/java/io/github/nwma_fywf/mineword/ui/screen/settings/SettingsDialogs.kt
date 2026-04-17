package io.github.nwma_fywf.mineword.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.R
import io.github.nwma_fywf.mineword.data.repository.DuplicateStrategy
import io.github.nwma_fywf.mineword.data.repository.ImportResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.set_reminder_time)) },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            Button(onClick = { onTimeSelected(timePickerState.hour, timePickerState.minute) }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun DuplicateStrategyDialog(
    duplicateCount: Int,
    duplicateWords: List<String>,
    onStrategySelected: (DuplicateStrategy) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedStrategy by remember { mutableStateOf(DuplicateStrategy.REPLACE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.duplicate_words_found)) },
        text = {
            Column {
                Text(stringResource(R.string.duplicate_words_detected, duplicateCount))
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.height(150.dp)
                ) {
                    items(duplicateWords.take(10)) { word ->
                        Text(
                            text = word,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    if (duplicateWords.size > 10) {
                        item {
                            Text(
                                text = stringResource(R.string.etc_count, duplicateWords.size),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.choose_strategy))
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStrategy = DuplicateStrategy.REPLACE }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selectedStrategy == DuplicateStrategy.REPLACE,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.strategy_replace), style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.strategy_replace_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStrategy = DuplicateStrategy.SKIP }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selectedStrategy == DuplicateStrategy.SKIP,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.strategy_skip), style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.strategy_skip_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStrategy = DuplicateStrategy.MERGE }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(
                            selected = selectedStrategy == DuplicateStrategy.MERGE,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(stringResource(R.string.strategy_merge), style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.strategy_merge_subtitle), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onStrategySelected(selectedStrategy) }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ImportResultDialog(
    result: ImportResult?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.import_result)) },
        text = {
            if (result == null || result.totalCount == 0) {
                Text(stringResource(R.string.import_failed))
            } else {
                Column {
                    Text(stringResource(R.string.import_total_count, result.totalCount))
                    if (result.successCount > 0) Text(stringResource(R.string.import_new_count, result.successCount))
                    if (result.replacedCount > 0) Text(stringResource(R.string.import_replaced_count, result.replacedCount))
                    if (result.mergedCount > 0) Text(stringResource(R.string.import_merged_count, result.mergedCount))
                    if (result.skipCount > 0) Text(stringResource(R.string.import_skipped_count, result.skipCount))
                    if (result.duplicateWords.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.import_duplicate_warning, result.duplicateWords.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.confirm))
            }
        }
    )
}

@Composable
fun CustomColorPickerDialog(
    onColorSelected: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var red by remember { mutableStateOf("25") }
    var green by remember { mutableStateOf("25") }
    var blue by remember { mutableStateOf("25") }

    val previewColor = Color(
        red = (red.toIntOrNull() ?: 0).coerceIn(0, 255) / 255f,
        green = (green.toIntOrNull() ?: 0).coerceIn(0, 255) / 255f,
        blue = (blue.toIntOrNull() ?: 0).coerceIn(0, 255) / 255f
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_rgb_color)) },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(previewColor, MaterialTheme.shapes.medium)
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.preview),
                        color = if ((red.toIntOrNull() ?: 0) > 127 ||
                            (green.toIntOrNull() ?: 0) > 127 ||
                            (blue.toIntOrNull() ?: 0) > 127) Color.Black else Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("R:", modifier = Modifier.width(24.dp))
                    TextField(value = red, onValueChange = { if (it.length <= 3) red = it.filter { c -> c.isDigit() } }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("G:", modifier = Modifier.width(24.dp))
                    TextField(value = green, onValueChange = { if (it.length <= 3) green = it.filter { c -> c.isDigit() } }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("B:", modifier = Modifier.width(24.dp))
                    TextField(value = blue, onValueChange = { if (it.length <= 3) blue = it.filter { c -> c.isDigit() } }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.range_0_255), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val r = (red.toIntOrNull() ?: 0).coerceIn(0, 255)
                    val g = (green.toIntOrNull() ?: 0).coerceIn(0, 255)
                    val b = (blue.toIntOrNull() ?: 0).coerceIn(0, 255)
                    val color = Color(r / 255f, g / 255f, b / 255f).toArgb()
                    onColorSelected(color, color, color)
                }
            ) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun ClearDataConfirmDialog(
    wordCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.confirm_clear_data)) },
        text = {
            Column {
                Text(stringResource(R.string.clear_data_description))
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.clear_data_word_count, wordCount))
                Text(stringResource(R.string.clear_data_records))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.clear_data_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.confirm_delete_all))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun NumberPickerDialog(
    title: String,
    currentValue: Int,
    minValue: Int = 0,
    maxValue: Int = 100,
    onValueSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var value by remember { mutableStateOf(currentValue.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                TextField(
                    value = value,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty()) {
                            value = ""
                        } else {
                            val intValue = newValue.filter { it.isDigit() }.take(3)
                            value = intValue
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "范围: $minValue - $maxValue",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val intValue = value.toIntOrNull() ?: currentValue
                    onValueSelected(intValue.coerceIn(minValue, maxValue))
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
