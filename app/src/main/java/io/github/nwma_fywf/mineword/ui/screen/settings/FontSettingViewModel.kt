package io.github.nwma_fywf.mineword.ui.screen.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.FontStyle
import io.github.nwma_fywf.mineword.data.local.ThemePreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FontSettingViewModel(
    private val themePreferences: ThemePreferences,
    private val context: Context
) : ViewModel() {

    val fontStyle: StateFlow<FontStyle> = themePreferences.fontStyle
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FontStyle.DEFAULT
        )

    val customFontPath: StateFlow<String?> = themePreferences.customFontPath
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val fontScale: StateFlow<Float> = themePreferences.fontScale
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1.0f
        )

    val cornerScale: StateFlow<Float> = themePreferences.cornerScale
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 1.0f
        )

    fun setFontStyle(style: FontStyle) {
        viewModelScope.launch {
            themePreferences.setFontStyle(style)
        }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            themePreferences.setFontScale(scale)
        }
    }

    fun setCornerScale(scale: Float) {
        viewModelScope.launch {
            themePreferences.setCornerScale(scale)
        }
    }

    fun setCustomFontFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
            }
            themePreferences.setCustomFontPath(uri.toString())
            themePreferences.setFontStyle(FontStyle.CUSTOM)
        }
    }

    fun clearCustomFont() {
        viewModelScope.launch {
            themePreferences.setCustomFontPath(null)
            themePreferences.setFontStyle(FontStyle.DEFAULT)
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
                    return FontSettingViewModel(themePreferences, context) as T
                }
            }
        }
    }
}