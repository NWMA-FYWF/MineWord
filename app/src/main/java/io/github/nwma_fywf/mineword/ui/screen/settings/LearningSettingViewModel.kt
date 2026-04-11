package io.github.nwma_fywf.mineword.ui.screen.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LearningSettingViewModel(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val reviewReminderEnabled: StateFlow<Boolean> = themePreferences.reviewReminderEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val reviewReminderHour: StateFlow<Int> = themePreferences.reviewReminderHour
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 20
        )

    val reviewReminderMinute: StateFlow<Int> = themePreferences.reviewReminderMinute
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun setReviewReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setReviewReminderEnabled(enabled)
        }
    }

    fun setReviewReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            themePreferences.setReviewReminderTime(hour, minute)
        }
    }

    companion object {
        fun provideFactory(
            themePreferences: ThemePreferences,
            context: Context
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LearningSettingViewModel(themePreferences) as T
                }
            }
        }
    }
}