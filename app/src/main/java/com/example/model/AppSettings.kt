package com.example.model

enum class AppThemeMode {
    DARK,
    LIGHT,
    DYNAMIC
}

enum class AppLanguage {
    RU,
    EN
}

data class AppSettings(
    val concurrency: Int = 100,
    val maxConfigsToCheck: Int = 0, // 0 = check all
    val customSources: List<String> = emptyList(),
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val language: AppLanguage = AppLanguage.RU
)
