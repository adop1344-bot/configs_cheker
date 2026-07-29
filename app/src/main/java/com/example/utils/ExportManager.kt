package com.example.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.model.ProxyItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportManager {

    fun copyToClipboard(context: Context, text: String, label: String = "LetoVPN Configs"): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun saveConfigsToDownloads(context: Context, configs: List<ProxyItem>): String? {
        if (configs.isEmpty()) return null

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "letovpn_working_configs_$timeStamp.txt"
        val content = configs.joinToString("\n") { it.rawUri }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(context, fileName, content)
        } else {
            saveViaLegacyFile(fileName, content)
        }
    }

    private fun saveViaMediaStore(context: Context, fileName: String, content: String): String? {
        return try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return null

            resolver.openOutputStream(uri)?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            }

            "Загрузки/$fileName"
        } catch (e: Exception) {
            null
        }
    }

    private fun saveViaLegacyFile(fileName: String, content: String): String? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
