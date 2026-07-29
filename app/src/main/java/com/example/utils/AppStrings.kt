package com.example.utils

import com.example.model.AppLanguage

object AppStrings {

    fun get(language: AppLanguage): Strings = when (language) {
        AppLanguage.RU -> RuStrings
        AppLanguage.EN -> EnStrings
    }

    interface Strings {
        val appName: String
        val appSubtitle: String
        val tabMain: String
        val tabSettings: String
        
        val statusIdle: String
        val statusFetching: String
        val statusTesting: String
        val statusFinished: String
        val statusError: String
        val btnCheck: String
        val btnStop: String
        val btnRefresh: String
        val btnRepeat: String
        val tested: String
        val working: String
        
        val quickActionsTitle: String
        val copy5: String
        val copy10: String
        val copy20: String
        val copyAll: String
        val saveDownloads: String
        
        val searchPlaceholder: String
        val filterAll: String
        val workingConfigsTitle: String
        val emptyStateNoConfigs: String
        val emptyStateScanning: String
        val copySingleSuccess: String
        
        // Settings strings
        val settingsTitle: String
        val sectionAppearance: String
        val themeModeLabel: String
        val themeDark: String
        val themeLight: String
        val themeDynamic: String
        
        val languageLabel: String
        val langRu: String
        val langEn: String
        
        val sectionPerformance: String
        val threadsLabel: String
        val threadsDescription: String
        val maxConfigsLabel: String
        val maxConfigsDescription: String
        val maxConfigsZeroAll: String
        
        val sectionSources: String
        val sourcesDescription: String
        val defaultSourceLabel: String
        val addSourceBtn: String
        val addSourceDialogTitle: String
        val addSourceDialogPlaceholder: String
        val btnAdd: String
        val btnCancel: String
        val btnDelete: String
        
        val sectionCreator: String
        val creatorTelegramChannel: String
        val creatorChannelDesc: String
        val btnOpenTelegram: String
        val appVersionInfo: String
        
        val msgCopiedTopN: String
        val msgCopiedAll: String
        val msgCopyError: String
        val msgFileSaved: String
        val msgFileSaveError: String
        val msgNoWorkingToCopy: String
        val msgScanStopped: String
        val msgScanFinished: String
        val msgFetchError: String
        val msgInvalidUrl: String
        val msgSourceAdded: String
    }

    private object RuStrings : Strings {
        override val appName = "LetoVPN Checker"
        override val appSubtitle = "Милимистичный проверочник конфигов"
        override val tabMain = "Проверка"
        override val tabSettings = "Настройки"

        override val statusIdle = "Нажмите 'Проверить' для запуска сканирования"
        override val statusFetching = "Загрузка источников..."
        override val statusTesting = "Проверка конфигов"
        override val statusFinished = "Проверка завершена"
        override val statusError = "Ошибка при проверке"
        override val btnCheck = "Проверить"
        override val btnStop = "Остановить"
        override val btnRefresh = "Обновить"
        override val btnRepeat = "Повторить"
        override val tested = "Проверено"
        override val working = "Рабочих"

        override val quickActionsTitle = "Быстрые действия:"
        override val copy5 = "Скопировать 5"
        override val copy10 = "Скопировать 10"
        override val copy20 = "Скопировать 20"
        override val copyAll = "Скопировать все"
        override val saveDownloads = "Сохранить в Загрузки (.txt)"

        override val searchPlaceholder = "Поиск по названию или серверу..."
        override val filterAll = "Все"
        override val workingConfigsTitle = "Рабочие конфиги"
        override val emptyStateNoConfigs = "Рабочие конфиги не найдены"
        override val emptyStateScanning = "Сканирование и проверка серверов..."
        override val copySingleSuccess = "Конфиг скопирован"

        // Settings
        override val settingsTitle = "Настройки"
        override val sectionAppearance = "Внешний вид и язык"
        override val themeModeLabel = "Тема оформления:"
        override val themeDark = "Темная"
        override val themeLight = "Светлая"
        override val themeDynamic = "Динамическая"

        override val languageLabel = "Язык интерфейса:"
        override val langRu = "Русский"
        override val langEn = "English"

        override val sectionPerformance = "Параметры проверки"
        override val threadsLabel = "Многопоточность (потоков)"
        override val threadsDescription = "Количество одновременных сокет-соединений (по умолчанию 100)"
        override val maxConfigsLabel = "Лимит конфигов для проверки"
        override val maxConfigsDescription = "Укажите сколько конфигов проверять (если 0 — проверять все)"
        override val maxConfigsZeroAll = "0 = Все конфиги"

        override val sectionSources = "Источники подписок"
        override val sectionCreator = "Создатель и Сообщество"
        override val sourcesDescription = "Основной источник по умолчанию загружает свежие подписки. Вы можете добавить свои ссылки на подписки или raw-списки."
        override val defaultSourceLabel = "Основной источник GitHub (LetoVPN)"
        override val addSourceBtn = "Добавить источник"
        override val addSourceDialogTitle = "Новый источник"
        override val addSourceDialogPlaceholder = "https://example.com/sub.txt"
        override val btnAdd = "Добавить"
        override val btnCancel = "Отмена"
        override val btnDelete = "Удалить"

        override val creatorTelegramChannel = "Телеграм канал создателя"
        override val creatorChannelDesc = "Официальный канал @letovpn_free с бесплатными конфигами и обновлениями"
        override val btnOpenTelegram = "Открыть @letovpn_free"
        override val appVersionInfo = "LetoVPN Checker v1.2 • Material Design 3"

        override val msgCopiedTopN = "Скопировано %d конфиг(ов) в буфер!"
        override val msgCopiedAll = "Скопированы все %d рабочих конфигов!"
        override val msgCopyError = "Ошибка копирования в буфер"
        override val msgFileSaved = "Файл сохранен: %s"
        override val msgFileSaveError = "Не удалось сохранить файл"
        override val msgNoWorkingToCopy = "Нет рабочих конфигов для копирования"
        override val msgScanStopped = "Проверка остановлена"
        override val msgScanFinished = "Проверка завершена! Найдено рабочих: %d"
        override val msgFetchError = "Не удалось получить конфиги из источников"
        override val msgInvalidUrl = "Введите корректный URL (начинается с http:// или https://)"
        override val msgSourceAdded = "Источник успешно добавлен"
    }

    private object EnStrings : Strings {
        override val appName = "LetoVPN Checker"
        override val appSubtitle = "Minimalist M3 Config Tester"
        override val tabMain = "Checker"
        override val tabSettings = "Settings"

        override val statusIdle = "Tap 'Check' to start scanning"
        override val statusFetching = "Fetching subscription sources..."
        override val statusTesting = "Testing configs"
        override val statusFinished = "Scan finished"
        override val statusError = "Error during scan"
        override val btnCheck = "Check"
        override val btnStop = "Stop"
        override val btnRefresh = "Refresh"
        override val btnRepeat = "Retry"
        override val tested = "Tested"
        override val working = "Working"

        override val quickActionsTitle = "Quick Actions:"
        override val copy5 = "Copy 5"
        override val copy10 = "Copy 10"
        override val copy20 = "Copy 20"
        override val copyAll = "Copy All"
        override val saveDownloads = "Save to Downloads (.txt)"

        override val searchPlaceholder = "Search by name or server..."
        override val filterAll = "All"
        override val workingConfigsTitle = "Working Configs"
        override val emptyStateNoConfigs = "No working configs found"
        override val emptyStateScanning = "Scanning & pinging servers..."
        override val copySingleSuccess = "Config copied"

        // Settings
        override val settingsTitle = "Settings"
        override val sectionAppearance = "Appearance & Language"
        override val themeModeLabel = "Theme Mode:"
        override val themeDark = "Dark"
        override val themeLight = "Light"
        override val themeDynamic = "Dynamic"

        override val languageLabel = "Language:"
        override val langRu = "Русский"
        override val langEn = "English"

        override val sectionPerformance = "Scan Performance"
        override val threadsLabel = "Concurrency (Threads)"
        override val threadsDescription = "Simultaneous socket testing connections (default 100)"
        override val maxConfigsLabel = "Max Configs to Check"
        override val maxConfigsDescription = "Set limit of configs to test (if 0 — test all)"
        override val maxConfigsZeroAll = "0 = Test All"

        override val sectionSources = "Subscription Sources"
        override val sectionCreator = "Creator & Community"
        override val sourcesDescription = "Default GitHub source fetches fresh proxy subscriptions. You can add custom subscription URLs or raw config lists."
        override val defaultSourceLabel = "Default GitHub Source (LetoVPN)"
        override val addSourceBtn = "Add Source"
        override val addSourceDialogTitle = "New Subscription Source"
        override val addSourceDialogPlaceholder = "https://example.com/sub.txt"
        override val btnAdd = "Add"
        override val btnCancel = "Cancel"
        override val btnDelete = "Delete"

        override val creatorTelegramChannel = "Creator's Telegram Channel"
        override val creatorChannelDesc = "Official channel @letovpn_free for free configs and updates"
        override val btnOpenTelegram = "Open @letovpn_free"
        override val appVersionInfo = "LetoVPN Checker v1.2 • Material Design 3"

        override val msgCopiedTopN = "Copied %d config(s) to clipboard!"
        override val msgCopiedAll = "Copied all %d working configs!"
        override val msgCopyError = "Clipboard copy failed"
        override val msgFileSaved = "File saved: %s"
        override val msgFileSaveError = "Failed to save file"
        override val msgNoWorkingToCopy = "No working configs to copy"
        override val msgScanStopped = "Scan stopped"
        override val msgScanFinished = "Scan finished! Working configs: %d"
        override val msgFetchError = "Failed to fetch configs from sources"
        override val msgInvalidUrl = "Enter a valid URL (http:// or https://)"
        override val msgSourceAdded = "Source added successfully"
    }
}
