package io.github.nwma_fywf.mineword.ui.screen.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.Meaning
import io.github.nwma_fywf.mineword.data.local.Word
import io.github.nwma_fywf.mineword.data.repository.WordRepository
import io.github.nwma_fywf.mineword.ui.screen.quiz.QuizViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class ReviewWord(
    val word: Word,
    val meanings: List<Meaning>,
    val status: ReviewStatus,
    val daysUntil: Int = 0
)

enum class ReviewStatus {
    OVERDUE,
    DUE_TODAY,
    DUE_TOMORROW,
    DUE_DAYS
}

data class DailyGoalProgress(
    val newWordsToday: Int = 0,
    val reviewWordsToday: Int = 0,
    val newWordGoal: Int = 10,
    val reviewGoal: Int = 20
)

class ReviewViewModel(private val repository: WordRepository) : ViewModel() {

    private val _reviewWords = MutableStateFlow<List<ReviewWord>>(emptyList())
    val reviewWords: StateFlow<List<ReviewWord>> = _reviewWords

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _dailyGoalProgress = MutableStateFlow(DailyGoalProgress())
    val dailyGoalProgress: StateFlow<DailyGoalProgress> = _dailyGoalProgress

    init {
        loadReviewWords()
        loadDailyGoalProgress()
    }

    private fun loadDailyGoalProgress() {
        viewModelScope.launch {
            val today = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()) * TimeUnit.DAYS.toMillis(1)
            repository.getDailyStatsDao().getStatsForDateFlow(today).collect { stats ->
                val progress = DailyGoalProgress(
                    newWordsToday = stats?.newWordsCount ?: 0,
                    reviewWordsToday = stats?.reviewedWordsCount ?: 0
                )
                _dailyGoalProgress.value = progress
            }
        }
    }

    fun loadReviewWords() {
        viewModelScope.launch {
            _isLoading.value = true
            val words = repository.getWordsDueForReview()
            val currentTime = System.currentTimeMillis()
            val reviewWords = words.map { word ->
                val meanings = repository.getMeaningsByWordId(word.id).first()
                val status = calculateReviewStatus(word.nextReviewTime, currentTime)
                val daysUntil = calculateDaysUntil(word.nextReviewTime, currentTime)
                ReviewWord(word, meanings, status, daysUntil)
            }.sortedBy { it.status.ordinal }
            _reviewWords.value = reviewWords
            _isLoading.value = false
        }
    }

    private fun calculateReviewStatus(nextReviewTime: Long, currentTime: Long): ReviewStatus {
        val diff = nextReviewTime - currentTime
        val oneDay = 24 * 60 * 60 * 1000L

        return when {
            diff <= 0 -> ReviewStatus.OVERDUE
            diff < oneDay -> ReviewStatus.DUE_TODAY
            diff < 2 * oneDay -> ReviewStatus.DUE_TOMORROW
            else -> ReviewStatus.DUE_DAYS
        }
    }

    private fun calculateDaysUntil(nextReviewTime: Long, currentTime: Long): Int {
        val diff = nextReviewTime - currentTime
        val oneDay = 24 * 60 * 60 * 1000L
        return (diff / oneDay).toInt().coerceAtLeast(0)
    }

    fun skipWord(wordId: Long) {
        viewModelScope.launch {
            val word = repository.getWordById(wordId)
            word?.let {
                repository.recordReview(it.id, it.learningStage)
                loadReviewWords()
                loadDailyGoalProgress()
            }
        }
    }

    companion object {
        fun provideFactory(repository: WordRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReviewViewModel(repository) as T
                }
            }
        }
    }
}