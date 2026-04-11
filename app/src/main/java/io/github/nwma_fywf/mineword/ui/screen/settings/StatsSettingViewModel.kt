package io.github.nwma_fywf.mineword.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WordStats(
    val wordCount: Int = 0,
    val meaningCount: Int = 0,
    val exampleSentenceCount: Int = 0
)

class StatsSettingViewModel(
    private val repository: WordRepository
) : ViewModel() {

    private val _wordStats = MutableStateFlow(WordStats())
    val wordStats: StateFlow<WordStats> = _wordStats.asStateFlow()

    init {
        loadWordStats()
    }

    private fun loadWordStats() {
        viewModelScope.launch {
            val wordCount = repository.getWordCount()
            _wordStats.value = WordStats(wordCount = wordCount)
        }
    }

    companion object {
        fun provideFactory(repository: WordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatsSettingViewModel(repository) as T
                }
            }
        }
    }
}