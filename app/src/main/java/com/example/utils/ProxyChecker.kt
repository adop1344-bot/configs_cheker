package com.example.utils

import android.util.Base64
import com.example.model.ProxyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

object ProxyChecker {

    private const val SOURCES_URL = "https://raw.githubusercontent.com/adop1344-bot/LetoVPN_free/refs/heads/main/sources.txt"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetchAllSourcesAndConfigs(
        customSources: List<String> = emptyList(),
        onProgressUpdate: (statusText: String) -> Unit
    ): List<String> = withContext(Dispatchers.IO) {
        onProgressUpdate("Загрузка источников...")

        val allSourceUrls = mutableListOf(SOURCES_URL)
        allSourceUrls.addAll(customSources)

        val collectedConfigs = mutableSetOf<String>()
        val directLinesToFetch = mutableListOf<String>()

        for (sourceUrl in allSourceUrls) {
            val text = try {
                val request = Request.Builder().url(sourceUrl).build()
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) response.body?.string() ?: "" else ""
                }
            } catch (e: Exception) {
                ""
            }

            if (text.isNotEmpty()) {
                text.lines().map { it.trim() }.forEach { line ->
                    if (line.isNotEmpty() && !line.startsWith("#")) {
                        if (line.startsWith("http://") || line.startsWith("https://")) {
                            directLinesToFetch.add(line)
                        } else if (ProxyParser.isProxyUri(line)) {
                            collectedConfigs.add(line)
                        }
                    }
                }
            }
        }

        onProgressUpdate("Обработка ${directLinesToFetch.size} подписок/источников...")

        val semaphore = Semaphore(10)

        coroutineScope {
            val jobs = directLinesToFetch.distinct().map { line ->
                async {
                    semaphore.withPermit {
                        fetchSubscriptionContent(line)
                    }
                }
            }

            val results = jobs.awaitAll()
            results.forEach { configList ->
                collectedConfigs.addAll(configList)
            }
        }

        onProgressUpdate("Собрано ${collectedConfigs.size} уникальных конфигов")
        collectedConfigs.toList()
    }

    private fun fetchSubscriptionContent(url: String): List<String> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "v2rayN/6.23 LetoVPN-Checker/1.0")
                .build()

            val bodyText = httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string()?.trim() else null
            } ?: return emptyList()

            val lines = mutableListOf<String>()

            // Try decoding base64 if it looks like subscription base64 payload
            var textToParse = bodyText
            if (!bodyText.contains("://") && bodyText.length > 20) {
                val decoded = decodeBase64Safe(bodyText)
                if (decoded != null && decoded.contains("://")) {
                    textToParse = decoded
                }
            }

            textToParse.lines().forEach { line ->
                val trimmed = line.trim()
                if (ProxyParser.isProxyUri(trimmed)) {
                    lines.add(trimmed)
                }
            }

            lines
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun testAllProxies(
        rawConfigs: List<String>,
        concurrency: Int = 100,
        maxConfigs: Int = 0,
        timeoutMs: Int = 2000,
        onProgress: (testedCount: Int, totalCount: Int, workingCount: Int, latestWorkingItem: ProxyItem?) -> Unit
    ): List<ProxyItem> = withContext(Dispatchers.IO) {
        if (rawConfigs.isEmpty()) return@withContext emptyList()

        val configsToTest = if (maxConfigs > 0 && maxConfigs < rawConfigs.size) {
            rawConfigs.take(maxConfigs)
        } else {
            rawConfigs
        }

        val parsedItems = configsToTest.mapIndexedNotNull { index, uri ->
            ProxyParser.parseUri(uri, index + 1)
        }

        val total = parsedItems.size
        var testedCount = 0
        var workingCount = 0
        val workingResults = mutableListOf<ProxyItem>()
        val lock = Any()

        val semaphore = Semaphore(concurrency)

        coroutineScope {
            val jobs = parsedItems.map { item ->
                async {
                    semaphore.withPermit {
                        val (isSuccess, latency) = testSingleSocket(item.server, item.port, timeoutMs)
                        var updatedItem: ProxyItem? = null

                        synchronized(lock) {
                            testedCount++
                            if (isSuccess && latency > 0) {
                                workingCount++
                                updatedItem = item.copy(latencyMs = latency, isWorking = true)
                                workingResults.add(updatedItem!!)
                            }
                            onProgress(testedCount, total, workingCount, updatedItem)
                        }
                    }
                }
            }
            jobs.awaitAll()
        }

        // Sort working proxies from lowest latency (fastest) to highest (slowest)
        val sortedList = workingResults.sortedBy { it.latencyMs }
        sortedList
    }

    private fun testSingleSocket(server: String, port: Int, timeoutMs: Int): Pair<Boolean, Long> {
        if (server.isEmpty() || port <= 0 || port > 65535) return Pair(false, -1L)
        var socket: Socket? = null
        val startTime = System.currentTimeMillis()
        return try {
            socket = Socket()
            socket.connect(InetSocketAddress(server, port), timeoutMs)
            val latency = System.currentTimeMillis() - startTime
            socket.close()
            Pair(true, latency)
        } catch (e: Exception) {
            try { socket?.close() } catch (_: Exception) {}
            Pair(false, -1L)
        }
    }

    private fun decodeBase64Safe(str: String): String? {
        return try {
            val clean = str.trim().replace("-", "+").replace("_", "/")
            val padded = when (clean.length % 4) {
                2 -> "$clean=="
                3 -> "$clean="
                else -> clean
            }
            val bytes = Base64.decode(padded, Base64.NO_WRAP or Base64.DEFAULT)
            String(bytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }
}
