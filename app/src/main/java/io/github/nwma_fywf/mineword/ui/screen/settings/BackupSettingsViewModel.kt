package io.github.nwma_fywf.mineword.ui.screen.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.nwma_fywf.mineword.data.local.ThemePreferences
import io.github.nwma_fywf.mineword.data.worker.BackupScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File

class BackupSettingsViewModel(
    private val themePreferences: ThemePreferences,
    private val context: Context
) : ViewModel() {

    private val _backupSettings = MutableStateFlow(BackupSettingsState())
    val backupSettings: StateFlow<BackupSettingsState> = _backupSettings.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                themePreferences.autoBackupEnabled,
                themePreferences.autoBackupFrequency
            ) { enabled, frequency ->
                BackupSettingsState(
                    autoBackupEnabled = enabled,
                    autoBackupFrequency = frequency
                )
            }.collect { state ->
                _backupSettings.value = state
            }
        }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setAutoBackupEnabled(enabled)
            if (enabled) {
                val frequency = _backupSettings.value.autoBackupFrequency
                BackupScheduler.scheduleAutoBackup(context, frequency.intervalDays)
            } else {
                BackupScheduler.cancelAutoBackup(context)
            }
        }
    }

    fun setAutoBackupFrequency(frequency: ThemePreferences.BackupFrequency) {
        viewModelScope.launch {
            themePreferences.setAutoBackupFrequency(frequency)
            if (_backupSettings.value.autoBackupEnabled) {
                BackupScheduler.scheduleAutoBackup(context, frequency.intervalDays)
            }
        }
    }

    fun getBackupFiles(): List<BackupFileInfo> {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) return emptyList()

        return backupDir.listFiles { file ->
            file.name.startsWith("mineword_backup_") && file.name.endsWith(".json")
        }?.map { file ->
            BackupFileInfo(
                name = file.name,
                path = file.absolutePath,
                size = file.length(),
                lastModified = file.lastModified()
            )
        }?.sortedByDescending { it.lastModified } ?: emptyList()
    }

    companion object {
        fun provideFactory(
            themePreferences: ThemePreferences,
            context: Context
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BackupSettingsViewModel(themePreferences, context) as T
                }
            }
        }
    }
}

data class BackupSettingsState(
    val autoBackupEnabled: Boolean = false,
    val autoBackupFrequency: ThemePreferences.BackupFrequency = ThemePreferences.BackupFrequency.DAILY
)

data class BackupFileInfo(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long
)
