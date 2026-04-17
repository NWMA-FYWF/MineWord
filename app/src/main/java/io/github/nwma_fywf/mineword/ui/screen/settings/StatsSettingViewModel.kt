package io.github.nwma_fywf.mineword.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.ThemePreferences
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class WordStats(
    val wordCount: Int = 0,
    val meaningCount: Int = 0,
    val exampleSentenceCount: Int = 0
)

data class TagPairStats(
    val tag1: String = "",
    val tag2: String = "",
    val count: Int = 0
)

class StatsSettingViewModel(
    private val repository: WordRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    private val _wordStats = MutableStateFlow(WordStats())
    val wordStats: StateFlow<WordStats> = _wordStats.asStateFlow()

    private val _tagPairStats = MutableStateFlow(TagPairStats())
    val tagPairStats: StateFlow<TagPairStats> = _tagPairStats.asStateFlow()

    private val _availableTags = MutableStateFlow<List<String>>(emptyList())
    val availableTags: StateFlow<List<String>> = _availableTags.asStateFlow()

    init {
        loadWordStats()
        loadSavedTagPairStats()
        loadAvailableTags()
    }

    private fun loadWordStats() {
        viewModelScope.launch {
            val wordCount = repository.getWordCount()
            _wordStats.value = WordStats(wordCount = wordCount)
        }
    }

    private fun loadAvailableTags() {
        viewModelScope.launch {
            repository.getAllTagsRaw().collect { rawTags ->
                val allTags = rawTags
                    .flatMap { it.split(",") }
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .sorted()
                _availableTags.value = allTags
            }
        }
    }

    private fun loadSavedTagPairStats() {
        viewModelScope.launch {
            val tag1 = themePreferences.statsTag1.first()
            val tag2 = themePreferences.statsTag2.first()
            
            val count = if (tag1.isNotBlank() && tag2.isNotBlank()) {
                repository.getWordsByTwoTagsCount(tag1, tag2)
            } else {
                0
            }
            
            _tagPairStats.value = TagPairStats(
                tag1 = tag1,
                tag2 = tag2,
                count = count
            )
        }
    }

    fun updateTagPairStats(tag1: String, tag2: String) {
        viewModelScope.launch {
            val count = if (tag1.isNotBlank() && tag2.isNotBlank()) {
                repository.getWordsByTwoTagsCount(tag1, tag2)
            } else {
                0
            }
            
            themePreferences.setStatsTags(tag1, tag2)
            
            _tagPairStats.value = TagPairStats(
                tag1 = tag1,
                tag2 = tag2,
                count = count
            )
        }
    }

    companion object {
        fun provideFactory(repository: WordRepository, themePreferences: ThemePreferences): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatsSettingViewModel(repository, themePreferences) as T
                }
            }
        }
    }
}