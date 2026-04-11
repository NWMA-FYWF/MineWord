package io.github.nwma_fywf.mineword.ui.screen.wordlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.nwma_fywf.mineword.R
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.repository.SortBy
import io.github.nwma_fywf.mineword.ui.component.ConfirmDeleteDialog
import io.github.nwma_fywf.mineword.ui.component.WordCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WordListScreen(
    viewModel: WordListViewModel,
    onNavigateToAddWord: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
) {
    val words by viewModel.words.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedWordIds by viewModel.selectedWordIds.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }
    var showAddTagsDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val pendingDeletedWords by viewModel.pendingDeletedWords.collectAsState()

    LaunchedEffect(pendingDeletedWords) {
        if (pendingDeletedWords.isNotEmpty()) {
            val count = pendingDeletedWords.size
            val message = if (count == 1) {
                "已删除 \"${pendingDeletedWords.first().word}\""
            } else {
                "已删除 $count 个单词"
            }
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "撤销",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearPendingDelete()
            }
        }
    }

    LaunchedEffect(isSelectionMode) {
        if (!isSelectionMode) {
            showBatchDeleteDialog = false
            showAddTagsDialog = false
        }
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedWordIds.size}") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cancel))
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.selectAll(words) }) {
                            Icon(Icons.Filled.SelectAll, contentDescription = stringResource(R.string.select_all))
                        }
                        IconButton(onClick = { showBatchDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                        }
                        IconButton(onClick = { showAddTagsDialog = true }) {
                            Icon(Icons.Filled.Label, contentDescription = stringResource(R.string.add_tags))
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.nav_word_list)) },
                    actions = {
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.sort))
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_by_created)) },
                                    onClick = {
                                        viewModel.setSortBy(SortBy.CREATED_TIME)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_by_alphabet)) },
                                    onClick = {
                                        viewModel.setSortBy(SortBy.ALPHABETIC)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_by_review)) },
                                    onClick = {
                                        viewModel.setSortBy(SortBy.REVIEW_TIME)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.sort_by_mastery)) },
                                    onClick = {
                                        viewModel.setSortBy(SortBy.MASTERY)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(onClick = onNavigateToAddWord) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_words)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
                                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.clear_search))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { }),
                )
            }

            if (words.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) stringResource(R.string.no_matching_words) else stringResource(R.string.no_words_yet),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                items(
                    items = words,
                    key = { "word_${it.id}" },
                ) { word ->
                    val meanings by viewModel.getMeanings(word.id).collectAsState(initial = emptyList())
                    val dismissState = rememberSwipeToDismissBoxState()
                    val isSelected = selectedWordIds.contains(word.id)

                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart && !isSelectionMode) {
                            viewModel.deleteWord(word)
                            dismissState.reset()
                        }
                    }

                    if (isSelectionMode) {
                        WordCardSelectable(
                            word = word,
                            meanings = meanings,
                            isSelected = isSelected,
                            onClick = { viewModel.toggleSelection(word.id) },
                            onLongClick = { viewModel.toggleSelection(word.id) },
                        )
                    } else {
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {},
                            enableDismissFromStartToEnd = false,
                        ) {
                            WordCard(
                                word = word,
                                meanings = meanings,
                                onClick = { onNavigateToWordDetail(word.id) },
                                onLongClick = { viewModel.toggleSelection(word.id) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showBatchDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text(stringResource(R.string.confirm_delete_selected, selectedWordIds.size)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSelectedWords(words)
                        showBatchDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showAddTagsDialog) {
        var tagsInput by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddTagsDialog = false },
            title = { Text(stringResource(R.string.add_tags)) },
            text = {
                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.tags_placeholder)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tagsInput.isNotBlank()) {
                            viewModel.addTagsToSelected(tagsInput)
                            showAddTagsDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagsDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun WordCardSelectable(
    word: Word,
    meanings: List<io.github.nwma_fywf.mineword.data.local.Meaning>,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onClick() },
            modifier = Modifier.padding(end = 8.dp)
        )
        WordCard(
            word = word,
            meanings = meanings,
            onClick = onClick,
            modifier = Modifier.weight(1f)
        )
    }
}
