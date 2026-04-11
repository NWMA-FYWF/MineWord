package io.github.nwma_fywf.mineword.ui.screen.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.R
import io.github.nwma_fywf.mineword.data.local.ExportData
import io.github.nwma_fywf.mineword.data.local.ExportWord
import io.github.nwma_fywf.mineword.data.local.ExportWrongAnswer
import io.github.nwma_fywf.mineword.data.repository.DuplicateStrategy
import io.github.nwma_fywf.mineword.data.repository.ImportResult
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import io.github.nwma_fywf.mineword.data.local.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DataManagementViewModel(
    private val themePreferences: ThemePreferences,
    private val repository: WordRepository,
    private val context: Context
) : ViewModel() {

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _importDialogState = MutableStateFlow(ImportDialogState())
    val importDialogState: StateFlow<ImportDialogState> = _importDialogState.asStateFlow()

    private val _importResultDialogState = MutableStateFlow(ImportResultDialogState())
    val importResultDialogState: StateFlow<ImportResultDialogState> = _importResultDialogState.asStateFlow()

    private val _exportResult = MutableStateFlow(ExportResultState())
    val exportResult: StateFlow<ExportResultState> = _exportResult.asStateFlow()

    private val _clearDataDialogState = MutableStateFlow(ClearDataDialogState())
    val clearDataDialogState: StateFlow<ClearDataDialogState> = _clearDataDialogState.asStateFlow()

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val exportData = repository.exportAllData()
                val json = Json { prettyPrint = true }
                val jsonString = json.encodeToString(exportData)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                val wordCount = exportData.words.size
                _exportResult.value = ExportResultState(
                    isSuccess = true,
                    message = if (wordCount > 0) context.getString(R.string.export_success, wordCount) else context.getString(R.string.export_no_data)
                )
            } catch (e: Exception) {
                _exportResult.value = ExportResultState(
                    isSuccess = false,
                    message = context.getString(R.string.export_failed, e.message)
                )
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun processImportFiles(uris: List<Uri>) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val json = Json { ignoreUnknownKeys = true }
                val allWords = mutableListOf<ExportWord>()
                val allWrongAnswers = mutableListOf<ExportWrongAnswer>()

                for (uri in uris) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val jsonString = inputStream?.bufferedReader()?.use { it.readText() }
                    if (jsonString != null) {
                        try {
                            val exportData = json.decodeFromString<ExportData>(jsonString)
                            allWords.addAll(exportData.words)
                            allWrongAnswers.addAll(exportData.wrongAnswers)
                        } catch (e: Exception) {
                        }
                    }
                }

                if (allWords.isEmpty() && allWrongAnswers.isEmpty()) {
                    _importResultDialogState.value = ImportResultDialogState(
                        isVisible = true,
                        result = ImportResult(
                            totalCount = 0,
                            successCount = 0,
                            skipCount = 0,
                            replacedCount = 0,
                            mergedCount = 0,
                            duplicateWords = emptyList()
                        )
                    )
                    return@launch
                }

                val duplicateWords = mutableListOf<String>()
                for (word in allWords) {
                    val existing = repository.getWordByWord(word.word)
                    if (existing != null && word.word !in duplicateWords) {
                        duplicateWords.add(word.word)
                    }
                }

                if (duplicateWords.isNotEmpty()) {
                    _importDialogState.value = ImportDialogState(
                        isVisible = true,
                        pendingWords = allWords,
                        pendingWrongAnswers = allWrongAnswers,
                        duplicateWords = duplicateWords
                    )
                } else {
                    val wordResult = if (allWords.isNotEmpty()) {
                        repository.importWords(allWords, DuplicateStrategy.SKIP)
                    } else {
                        ImportResult(0, 0, 0, 0, 0, emptyList())
                    }
                    if (allWrongAnswers.isNotEmpty()) {
                        repository.importWrongAnswers(allWrongAnswers)
                    }
                    _importResultDialogState.value = ImportResultDialogState(
                        isVisible = true,
                        result = wordResult
                    )
                }
            } catch (e: Exception) {
                _importResultDialogState.value = ImportResultDialogState(
                    isVisible = true,
                    result = ImportResult(
                        totalCount = 0,
                        successCount = 0,
                        skipCount = 0,
                        replacedCount = 0,
                        mergedCount = 0,
                        duplicateWords = emptyList()
                    )
                )
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun onDuplicateStrategySelected(strategy: DuplicateStrategy) {
        viewModelScope.launch {
            val state = _importDialogState.value
            _importDialogState.value = state.copy(isVisible = false)

            val wordResult = if (state.pendingWords.isNotEmpty()) {
                repository.importWords(state.pendingWords, strategy)
            } else {
                ImportResult(0, 0, 0, 0, 0, emptyList())
            }
            if (state.pendingWrongAnswers.isNotEmpty()) {
                repository.importWrongAnswers(state.pendingWrongAnswers)
            }
            _importResultDialogState.value = ImportResultDialogState(
                isVisible = true,
                result = wordResult
            )
        }
    }

    fun dismissImportDialog() {
        _importDialogState.value = _importDialogState.value.copy(isVisible = false)
    }

    fun dismissImportResultDialog() {
        _importResultDialogState.value = _importResultDialogState.value.copy(isVisible = false)
    }

    fun dismissExportResult() {
        _exportResult.value = ExportResultState()
    }

    fun showClearDataDialog() {
        viewModelScope.launch {
            val words = repository.getAllWordsList()
            _clearDataDialogState.value = ClearDataDialogState(
                isVisible = true,
                wordCount = words.size
            )
        }
    }

    fun confirmClearData() {
        viewModelScope.launch {
            try {
                repository.deleteAllWords()
                repository.clearAllWrongAnswers()
                Toast.makeText(context, context.getString(R.string.data_cleared), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.clear_failed, e.message), Toast.LENGTH_SHORT).show()
            }
            _clearDataDialogState.value = ClearDataDialogState()
        }
    }

    fun dismissClearDataDialog() {
        _clearDataDialogState.value = ClearDataDialogState()
    }

    companion object {
        fun provideFactory(
            themePreferences: ThemePreferences,
            repository: WordRepository,
            context: Context
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DataManagementViewModel(themePreferences, repository, context) as T
                }
            }
        }
    }
}