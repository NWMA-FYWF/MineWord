package io.github.nwma_fywf.mineword.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.ThemeMode
import io.github.nwma_fywf.mineword.data.local.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeSettingViewModel(
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeMode.SYSTEM
        )

    val useDynamicColor: StateFlow<Boolean> = themePreferences.useDynamicColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val customPrimaryColor: StateFlow<Int?> = themePreferences.customPrimaryColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val customSecondaryColor: StateFlow<Int?> = themePreferences.customSecondaryColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val customTertiaryColor: StateFlow<Int?> = themePreferences.customTertiaryColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    fun setUseDynamicColor(use: Boolean) {
        viewModelScope.launch {
            themePreferences.setUseDynamicColor(use)
        }
    }

    fun setCustomThemeColor(primary: Int, secondary: Int, tertiary: Int) {
        viewModelScope.launch {
            themePreferences.setCustomThemeColor(primary, secondary, tertiary)
            themePreferences.setUseDynamicColor(false)
        }
    }

    fun clearCustomThemeColor() {
        viewModelScope.launch {
            themePreferences.clearCustomThemeColor()
            themePreferences.setUseDynamicColor(true)
        }
    }

    companion object {
        fun provideFactory(
            themePreferences: ThemePreferences
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ThemeSettingViewModel(themePreferences) as T
                }
            }
        }
    }
}