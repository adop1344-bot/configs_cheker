package com.example.utils

import android.util.Base64
import com.example.model.ProxyItem
import com.example.model.ProxyProtocol
import org.json.JSONObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object ProxyParser {

    fun parseUri(rawUri: String, index: Int = 0): ProxyItem? {
        val trimmed = rawUri.trim()
        if (trimmed.isEmpty()) return null

        val protocol = ProxyProtocol.fromUri(trimmed)

        return try {
            when (protocol) {
                ProxyProtocol.VMESS -> parseVMess(trimmed, index)
                ProxyProtocol.SHADOWSOCKS -> parseShadowsocks(trimmed, index)
                ProxyProtocol.VLESS, ProxyProtocol.TROJAN, ProxyProtocol.HYSTERIA2, ProxyProtocol.TUIC -> parseStandardUri(trimmed, protocol, index)
                else -> parseGeneric(trimmed, index)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseVMess(uri: String, index: Int): ProxyItem? {
        val base64Data = uri.removePrefix("vmess://").trim()
        val jsonStr = decodeBase64Safe(base64Data) ?: return null
        val json = JSONObject(jsonStr)

        val server = json.optString("add", "").ifEmpty { json.optString("host", "") }
        val portStr = json.optString("port", "443")
        val port = portStr.toIntOrNull() ?: 443
        val ps = json.optString("ps", "VMess Node #$index")

        if (server.isEmpty()) return null

        return ProxyItem(
            id = "vmess_${index}_${server}_$port",
            rawUri = uri,
            protocol = ProxyProtocol.VMESS,
            server = server,
            port = port,
            remark = ps
        )
    }

    private fun parseShadowsocks(uri: String, index: Int): ProxyItem? {
        val remark = uri.substringAfter("#", "").let { decodeUrlSafe(it) }.ifEmpty { "Shadowsocks #$index" }
        val body = uri.removePrefix("ss://").removePrefix("shadowsocks://").substringBefore("#")

        var server = ""
        var port = 8388

        if (body.contains("@")) {
            val serverPart = body.substringAfter("@")
            val hostPort = serverPart.substringBefore("/").substringBefore("?")
            if (hostPort.contains(":")) {
                server = hostPort.substringBeforeLast(":")
                port = hostPort.substringAfterLast(":").toIntOrNull() ?: 8388
            } else {
                server = hostPort
            }
        } else {
            // Might be entire base64 userinfo@host:port
            val decoded = decodeBase64Safe(body)
            if (decoded != null && decoded.contains("@")) {
                val hostPort = decoded.substringAfter("@")
                if (hostPort.contains(":")) {
                    server = hostPort.substringBeforeLast(":")
                    port = hostPort.substringAfterLast(":").toIntOrNull() ?: 8388
                }
            }
        }

        if (server.isEmpty()) return null

        return ProxyItem(
            id = "ss_${index}_${server}_$port",
            rawUri = uri,
            protocol = ProxyProtocol.SHADOWSOCKS,
            server = server,
            port = port,
            remark = remark
        )
    }

    private fun parseStandardUri(uri: String, protocol: ProxyProtocol, index: Int): ProxyItem? {
        val remark = uri.substringAfter("#", "").let { decodeUrlSafe(it) }.ifEmpty { "${protocol.displayName} Node #$index" }
        val scheme = uri.substringBefore("://")
        val body = uri.removePrefix("$scheme://").substringBefore("#").substringBefore("?")

        val hostPortPart = if (body.contains("@")) body.substringAfter("@") else body

        var server = ""
        var port = 443

        if (hostPortPart.startsWith("[")) {
            // IPv6 address e.g. [2001:db8::1]:443
            server = hostPortPart.substringBefore("]") + "]"
            val afterBrackets = hostPortPart.substringAfter("]", "")
            if (afterBrackets.startsWith(":")) {
                port = afterBrackets.removePrefix(":").toIntOrNull() ?: 443
            }
        } else if (hostPortPart.contains(":")) {
            server = hostPortPart.substringBeforeLast(":")
            port = hostPortPart.substringAfterLast(":").toIntOrNull() ?: 443
        } else {
            server = hostPortPart
        }

        if (server.isEmpty()) return null

        return ProxyItem(
            id = "${protocol.name.lowercase()}_${index}_${server}_$port",
            rawUri = uri,
            protocol = protocol,
            server = server,
            port = port,
            remark = remark
        )
    }

    private fun parseGeneric(uri: String, index: Int): ProxyItem? {
        val remark = uri.substringAfter("#", "").let { decodeUrlSafe(it) }.ifEmpty { "Config #$index" }
        val protocol = ProxyProtocol.fromUri(uri)
        return ProxyItem(
            id = "node_${index}",
            rawUri = uri,
            protocol = protocol,
            server = "node",
            port = 443,
            remark = remark
        )
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

    private fun decodeUrlSafe(str: String): String {
        return try {
            URLDecoder.decode(str, "UTF-8")
        } catch (e: Exception) {
            str
        }
    }

    fun isProxyUri(line: String): Boolean {
        val lower = line.trim().lowercase()
        return lower.startsWith("vless://") ||
                lower.startsWith("trojan://") ||
                lower.startsWith("vmess://") ||
                lower.startsWith("ss://") ||
                lower.startsWith("shadowsocks://") ||
                lower.startsWith("hy2://") ||
                lower.startsWith("hysteria2://") ||
                lower.startsWith("hysteria://") ||
                lower.startsWith("tuic://") ||
                lower.startsWith("socks5://") ||
                lower.startsWith("http://") ||
                lower.startsWith("https://")
    }
}
