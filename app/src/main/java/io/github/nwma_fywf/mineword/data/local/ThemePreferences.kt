package io.github.nwma_fywf.mineword.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class FontStyle {
    DEFAULT,
    SERIF,
    SANS_SERIF,
    MONOSPACE,
    CUSTOM
}

class ThemePreferences(private val context: Context) {

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val USE_DYNAMIC_COLOR_KEY = booleanPreferencesKey("use_dynamic_color")
        private val FONT_STYLE_KEY = stringPreferencesKey("font_style")
        private val CUSTOM_FONT_PATH_KEY = stringPreferencesKey("custom_font_path")
private val FONT_SCALE_KEY = floatPreferencesKey("font_scale")
    private val CORNER_SCALE_KEY = floatPreferencesKey("corner_scale")
    private val CUSTOM_PRIMARY_KEY = intPreferencesKey("custom_primary")
        private val CUSTOM_SECONDARY_KEY = intPreferencesKey("custom_secondary")
        private val CUSTOM_TERTIARY_KEY = intPreferencesKey("custom_tertiary")
        private val REVIEW_REMINDER_ENABLED_KEY = booleanPreferencesKey("review_reminder_enabled")
        private val REVIEW_REMINDER_HOUR_KEY = intPreferencesKey("review_reminder_hour")
        private val REVIEW_REMINDER_MINUTE_KEY = intPreferencesKey("review_reminder_minute")
        private val STATS_TAG1_KEY = stringPreferencesKey("stats_tag1")
        private val STATS_TAG2_KEY = stringPreferencesKey("stats_tag2")
        private val DAILY_NEW_WORD_GOAL_KEY = intPreferencesKey("daily_new_word_goal")
        private val DAILY_REVIEW_GOAL_KEY = intPreferencesKey("daily_review_goal")
        private val AUTO_BACKUP_ENABLED_KEY = booleanPreferencesKey("auto_backup_enabled")
        private val AUTO_BACKUP_FREQUENCY_KEY = stringPreferencesKey("auto_backup_frequency")
    }

    enum class BackupFrequency(val displayName: String, val intervalDays: Int) {
        DAILY("每天", 1),
        EVERY_3_DAYS("每3天", 3),
        WEEKLY("每周", 7)
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val modeString = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(modeString)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    val useDynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[USE_DYNAMIC_COLOR_KEY] ?: true
    }

    suspend fun setUseDynamicColor(use: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLOR_KEY] = use
        }
    }

    val fontStyle: Flow<FontStyle> = context.dataStore.data.map { preferences ->
        val styleString = preferences[FONT_STYLE_KEY] ?: FontStyle.DEFAULT.name
        try {
            FontStyle.valueOf(styleString)
        } catch (e: IllegalArgumentException) {
            FontStyle.DEFAULT
        }
    }

    suspend fun setFontStyle(style: FontStyle) {
        context.dataStore.edit { preferences ->
            preferences[FONT_STYLE_KEY] = style.name
        }
    }

    val customFontPath: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_FONT_PATH_KEY]
    }

    val fontScale: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[FONT_SCALE_KEY] ?: 1.0f
    }

    val cornerScale: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[CORNER_SCALE_KEY] ?: 1.0f
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SCALE_KEY] = scale.coerceIn(0.5f, 2.0f)
        }
    }

    suspend fun setCornerScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[CORNER_SCALE_KEY] = scale.coerceIn(0.5f, 2.0f)
        }
    }

    suspend fun setCustomFontPath(path: String?) {
        context.dataStore.edit { preferences ->
            if (path != null) {
                preferences[CUSTOM_FONT_PATH_KEY] = path
            } else {
                preferences.remove(CUSTOM_FONT_PATH_KEY)
            }
        }
    }

    val customPrimaryColor: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_PRIMARY_KEY]
    }

    val customSecondaryColor: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_SECONDARY_KEY]
    }

    val customTertiaryColor: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_TERTIARY_KEY]
    }

    suspend fun setCustomThemeColor(primary: Int, secondary: Int, tertiary: Int) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_PRIMARY_KEY] = primary
            preferences[CUSTOM_SECONDARY_KEY] = secondary
            preferences[CUSTOM_TERTIARY_KEY] = tertiary
        }
    }

    suspend fun clearCustomThemeColor() {
        context.dataStore.edit { preferences ->
            preferences.remove(CUSTOM_PRIMARY_KEY)
            preferences.remove(CUSTOM_SECONDARY_KEY)
            preferences.remove(CUSTOM_TERTIARY_KEY)
        }
    }

    val reviewReminderEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[REVIEW_REMINDER_ENABLED_KEY] ?: false
    }

    val reviewReminderHour: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[REVIEW_REMINDER_HOUR_KEY] ?: 20
    }

    val reviewReminderMinute: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[REVIEW_REMINDER_MINUTE_KEY] ?: 0
    }

    suspend fun setReviewReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[REVIEW_REMINDER_ENABLED_KEY] = enabled
        }
        context.getSharedPreferences("mineword_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("review_reminder_enabled", enabled).apply()
    }

    suspend fun setReviewReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[REVIEW_REMINDER_HOUR_KEY] = hour
            preferences[REVIEW_REMINDER_MINUTE_KEY] = minute
        }
        context.getSharedPreferences("mineword_prefs", Context.MODE_PRIVATE)
            .edit()
            .putInt("review_reminder_hour", hour)
            .putInt("review_reminder_minute", minute)
            .apply()
    }
    
    val statsTag1: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[STATS_TAG1_KEY] ?: ""
    }
    
    val statsTag2: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[STATS_TAG2_KEY] ?: ""
    }
    
    suspend fun setStatsTags(tag1: String, tag2: String) {
        context.dataStore.edit { preferences ->
            preferences[STATS_TAG1_KEY] = tag1
            preferences[STATS_TAG2_KEY] = tag2
        }
    }

    val dailyNewWordGoal: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DAILY_NEW_WORD_GOAL_KEY] ?: 10
    }

    val dailyReviewGoal: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[DAILY_REVIEW_GOAL_KEY] ?: 20
    }

    suspend fun setDailyNewWordGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_NEW_WORD_GOAL_KEY] = goal.coerceIn(0, 100)
        }
    }

    suspend fun setDailyReviewGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[DAILY_REVIEW_GOAL_KEY] = goal.coerceIn(0, 100)
        }
    }

    val autoBackupEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_BACKUP_ENABLED_KEY] ?: false
    }

    val autoBackupFrequency: Flow<BackupFrequency> = context.dataStore.data.map { preferences ->
        val freqString = preferences[AUTO_BACKUP_FREQUENCY_KEY] ?: BackupFrequency.DAILY.name
        try {
            BackupFrequency.valueOf(freqString)
        } catch (e: IllegalArgumentException) {
            BackupFrequency.DAILY
        }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_BACKUP_ENABLED_KEY] = enabled
        }
    }

    suspend fun setAutoBackupFrequency(frequency: BackupFrequency) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_BACKUP_FREQUENCY_KEY] = frequency.name
        }
    }
}