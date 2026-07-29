package com.example.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.model.AppLanguage
import com.example.model.AppSettings
import com.example.model.AppThemeMode
import org.json.JSONArray

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("letovpn_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CONCURRENCY = "concurrency"
        private const val KEY_MAX_CONFIGS = "max_configs"
        private const val KEY_CUSTOM_SOURCES = "custom_sources"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LANGUAGE = "language"
    }

    fun loadSettings(): AppSettings {
        val concurrency = prefs.getInt(KEY_CONCURRENCY, 100)
        val maxConfigs = prefs.getInt(KEY_MAX_CONFIGS, 0)
        val themeModeStr = prefs.getString(KEY_THEME_MODE, AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        val languageStr = prefs.getString(KEY_LANGUAGE, AppLanguage.RU.name) ?: AppLanguage.RU.name
        val customSourcesJson = prefs.getString(KEY_CUSTOM_SOURCES, "[]") ?: "[]"

        val customSources = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(customSourcesJson)
            for (i in 0 until jsonArray.length()) {
                val url = jsonArray.optString(i)
                if (url.isNotBlank()) {
                    customSources.add(url)
                }
            }
        } catch (_: Exception) {}

        val themeMode = try {
            AppThemeMode.valueOf(themeModeStr)
        } catch (_: Exception) {
            AppThemeMode.DARK
        }

        val language = try {
            AppLanguage.valueOf(languageStr)
        } catch (_: Exception) {
            AppLanguage.RU
        }

        return AppSettings(
            concurrency = concurrency,
            maxConfigsToCheck = maxConfigs,
            customSources = customSources,
            themeMode = themeMode,
            language = language
        )
    }

    fun saveSettings(settings: AppSettings) {
        val jsonArray = JSONArray()
        settings.customSources.forEach { jsonArray.put(it) }

        prefs.edit()
            .putInt(KEY_CONCURRENCY, settings.concurrency)
            .putInt(KEY_MAX_CONFIGS, settings.maxConfigsToCheck)
            .putString(KEY_THEME_MODE, settings.themeMode.name)
            .putString(KEY_LANGUAGE, settings.language.name)
            .putString(KEY_CUSTOM_SOURCES, jsonArray.toString())
            .apply()
    }
}
