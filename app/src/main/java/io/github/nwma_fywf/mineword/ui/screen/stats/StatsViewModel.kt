package io.github.nwma_fywf.mineword.ui.screen.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.DailyStats
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class StatsOverview(
    val totalWordCount: Int,
    val totalNewWords: Int,
    val totalReviewedWords: Int,
    val totalCorrect: Int,
    val totalWrong: Int,
    val accuracyRate: Int
)

data class StatsDay(
    val date: Long,
    val dateLabel: String,
    val newWordsCount: Int,
    val reviewedWordsCount: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val accuracyRate: Int,
    val accuracyChange: Int?
)

class StatsViewModel(private val repository: WordRepository) : ViewModel() {

    private val _statsOverview = MutableStateFlow<StatsOverview>(
        StatsOverview(0, 0, 0, 0, 0, 0)
    )
    val statsOverview: StateFlow<StatsOverview> = _statsOverview

    private val _recentStats = MutableStateFlow<List<StatsDay>>(emptyList())
    val recentStats: StateFlow<List<StatsDay>> = _recentStats

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            combine(
                repository.getWordCountFlow(),
                repository.getTotalNewWordsFlow(),
                repository.getTotalReviewedWordsFlow(),
                repository.getTotalCorrectFlow(),
                repository.getTotalWrongFlow()
            ) { wordCount, newWords, reviewedWords, correct, wrong ->
                val total = correct + wrong
                val accuracyRate = if (total > 0) (correct * 100 / total) else 0
                StatsOverview(
                    totalWordCount = wordCount,
                    totalNewWords = newWords,
                    totalReviewedWords = reviewedWords,
                    totalCorrect = correct,
                    totalWrong = wrong,
                    accuracyRate = accuracyRate
                )
            }.collect { overview ->
                _statsOverview.value = overview
            }
        }

        viewModelScope.launch {
            repository.getRecentStats(7).collect { statsList ->
                val statsDays = statsList.mapIndexed { index, stats ->
                    val total = stats.correctCount + stats.wrongCount
                    val accuracyRate = if (total > 0) (stats.correctCount * 100 / total) else 0
                    
                    val previousAccuracy = if (index < statsList.size - 1) {
                        val prevTotal = statsList[index + 1].correctCount + statsList[index + 1].wrongCount
                        if (prevTotal > 0) (statsList[index + 1].correctCount * 100 / prevTotal) else 0
                    } else null
                    
                    val accuracyChange = previousAccuracy?.let { accuracyRate - it }
                    
                    StatsDay(
                        date = stats.date,
                        dateLabel = formatDate(stats.date),
                        newWordsCount = stats.newWordsCount,
                        reviewedWordsCount = stats.reviewedWordsCount,
                        correctCount = stats.correctCount,
                        wrongCount = stats.wrongCount,
                        accuracyRate = accuracyRate,
                        accuracyChange = accuracyChange
                    )
                }
                _recentStats.value = statsDays
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp)
        val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()) * TimeUnit.DAYS.toMillis(1)
        val yesterday = today - TimeUnit.DAYS.toMillis(1)

        return when (timestamp) {
            today -> "今天"
            yesterday -> "昨天"
            else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(date)
        }
    }

    companion object {
        fun provideFactory(repository: WordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StatsViewModel(repository) as T
                }
            }
        }
    }
}
