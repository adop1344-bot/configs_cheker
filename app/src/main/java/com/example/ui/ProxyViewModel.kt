package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppLanguage
import com.example.model.AppSettings
import com.example.model.AppThemeMode
import com.example.model.ProxyItem
import com.example.model.ProxyProtocol
import com.example.utils.AppStrings
import com.example.utils.ExportManager
import com.example.utils.ProxyChecker
import com.example.utils.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CheckingState {
    object Idle : CheckingState()
    data class Fetching(val statusMessage: String) : CheckingState()
    data class Testing(
        val tested: Int,
        val total: Int,
        val workingCount: Int,
        val percentage: Float
    ) : CheckingState()
    data class Finished(val totalTested: Int, val workingCount: Int) : CheckingState()
    data class Error(val message: String) : CheckingState()
}

class ProxyViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    private val _appSettings = MutableStateFlow(settingsRepository.loadSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _checkingState = MutableStateFlow<CheckingState>(CheckingState.Idle)
    val checkingState: StateFlow<CheckingState> = _checkingState.asStateFlow()

    private val _workingProxies = MutableStateFlow<List<ProxyItem>>(emptyList())
    val workingProxies: StateFlow<List<ProxyItem>> = _workingProxies.asStateFlow()

    private val _rawConfigsCount = MutableStateFlow(0)
    val rawConfigsCount: StateFlow<Int> = _rawConfigsCount.asStateFlow()

    private val _selectedProtocol = MutableStateFlow<ProxyProtocol?>(null)
    val selectedProtocol: StateFlow<ProxyProtocol?> = _selectedProtocol.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private var scanJob: Job? = null

    // NOTE: Auto-start scan is purposefully omitted as per user requirement.
    // User must tap "Check" manually.

    fun strings(): AppStrings.Strings {
        return AppStrings.get(_appSettings.value.language)
    }

    fun startScan() {
        if (_checkingState.value is CheckingState.Fetching || _checkingState.value is CheckingState.Testing) {
            return
        }

        val strings = strings()
        val currentSettings = _appSettings.value

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            try {
                _checkingState.value = CheckingState.Fetching(strings.statusFetching)
                _workingProxies.value = emptyList()

                val rawConfigs = ProxyChecker.fetchAllSourcesAndConfigs(
                    customSources = currentSettings.customSources
                ) { status ->
                    _checkingState.value = CheckingState.Fetching(status)
                }

                _rawConfigsCount.value = rawConfigs.size

                if (rawConfigs.isEmpty()) {
                    _checkingState.value = CheckingState.Error(strings.msgFetchError)
                    return@launch
                }

                val totalToTest = if (currentSettings.maxConfigsToCheck > 0 && currentSettings.maxConfigsToCheck < rawConfigs.size) {
                    currentSettings.maxConfigsToCheck
                } else {
                    rawConfigs.size
                }

                _checkingState.value = CheckingState.Testing(
                    tested = 0,
                    total = totalToTest,
                    workingCount = 0,
                    percentage = 0f
                )

                val workingList = ProxyChecker.testAllProxies(
                    rawConfigs = rawConfigs,
                    concurrency = currentSettings.concurrency,
                    maxConfigs = currentSettings.maxConfigsToCheck,
                    timeoutMs = 2000
                ) { tested, total, workingCount, _ ->
                    val pct = if (total > 0) (tested.toFloat() / total.toFloat()) else 0f
                    _checkingState.value = CheckingState.Testing(
                        tested = tested,
                        total = total,
                        workingCount = workingCount,
                        percentage = pct
                    )
                }

                _workingProxies.value = workingList
                _checkingState.value = CheckingState.Finished(
                    totalTested = totalToTest,
                    workingCount = workingList.size
                )
                _userMessage.value = String.format(strings.msgScanFinished, workingList.size)

            } catch (e: Exception) {
                _checkingState.value = CheckingState.Error(e.message ?: strings.statusError)
            }
        }
    }

    fun cancelScan() {
        scanJob?.cancel()
        scanJob = null
        val currentWorking = _workingProxies.value
        _checkingState.value = CheckingState.Finished(
            totalTested = _rawConfigsCount.value,
            workingCount = currentWorking.size
        )
        _userMessage.value = strings().msgScanStopped
    }

    fun updateConcurrency(concurrency: Int) {
        val updated = _appSettings.value.copy(concurrency = concurrency.coerceIn(1, 500))
        _appSettings.value = updated
        settingsRepository.saveSettings(updated)
    }

    fun updateMaxConfigs(maxConfigs: Int) {
        val updated = _appSettings.value.copy(maxConfigsToCheck = maxConfigs.coerceAtLeast(0))
        _appSettings.value = updated
        settingsRepository.saveSettings(updated)
    }

    fun addCustomSource(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isBlank() || (!trimmed.startsWith("http://") && !trimmed.startsWith("https://"))) {
            _userMessage.value = strings().msgInvalidUrl
            return false
        }
        val currentList = _appSettings.value.customSources
        if (currentList.contains(trimmed)) {
            return true
        }
        val newList = currentList + trimmed
        val updated = _appSettings.value.copy(customSources = newList)
        _appSettings.value = updated
        settingsRepository.saveSettings(updated)
        _userMessage.value = strings().msgSourceAdded
        return true
    }

    fun removeCustomSource(url: String) {
        val newList = _appSettings.value.customSources.filter { it != url }
        val updated = _appSettings.value.copy(customSources = newList)
        _appSettings.value = updated
        settingsRepository.saveSettings(updated)
    }

    fun updateThemeMode(mode: AppThemeMode) {
        val updated = _appSettings.value.copy(themeMode = mode)
        _appSettings.value = updated
        settingsRepository.saveSettings(updated)
    }

    fun updateLanguage(lang: AppLanguage) {
        val updated = _appSettings.value.copy(language = lang)
        _appSettings.value = updated
        settingsRepository.saveSettings(updated)
    }

    fun setProtocolFilter(protocol: ProxyProtocol?) {
        _selectedProtocol.value = protocol
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun copyTopN(count: Int) {
        val working = _workingProxies.value
        val strings = strings()
        if (working.isEmpty()) {
            _userMessage.value = strings.msgNoWorkingToCopy
            return
        }

        val subset = working.take(count)
        val textToCopy = subset.joinToString("\n") { it.rawUri }
        val success = ExportManager.copyToClipboard(getApplication(), textToCopy, "LetoVPN Top $count")

        if (success) {
            _userMessage.value = String.format(strings.msgCopiedTopN, subset.size)
        } else {
            _userMessage.value = strings.msgCopyError
        }
    }

    fun copyAll() {
        val working = _workingProxies.value
        val strings = strings()
        if (working.isEmpty()) {
            _userMessage.value = strings.msgNoWorkingToCopy
            return
        }

        val textToCopy = working.joinToString("\n") { it.rawUri }
        val success = ExportManager.copyToClipboard(getApplication(), textToCopy, "LetoVPN All Working")

        if (success) {
            _userMessage.value = String.format(strings.msgCopiedAll, working.size)
        } else {
            _userMessage.value = strings.msgCopyError
        }
    }

    fun saveToDownloads() {
        val working = _workingProxies.value
        val strings = strings()
        if (working.isEmpty()) {
            _userMessage.value = strings.msgNoWorkingToCopy
            return
        }

        val filePath = ExportManager.saveConfigsToDownloads(getApplication(), working)
        if (filePath != null) {
            _userMessage.value = String.format(strings.msgFileSaved, filePath)
        } else {
            _userMessage.value = strings.msgFileSaveError
        }
    }

    fun copySingleProxy(item: ProxyItem) {
        val success = ExportManager.copyToClipboard(getApplication(), item.rawUri, item.remark)
        if (success) {
            _userMessage.value = "${strings().copySingleSuccess}: ${item.remark}"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
