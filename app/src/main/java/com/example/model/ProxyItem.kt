package com.example.model

enum class ProxyProtocol(val displayName: String) {
    VLESS("VLESS"),
    TROJAN("Trojan"),
    VMESS("VMess"),
    SHADOWSOCKS("SS"),
    HYSTERIA2("Hy2"),
    TUIC("TUIC"),
    OTHER("Proxy");

    companion object {
        fun fromUri(uri: String): ProxyProtocol {
            val lower = uri.trim().lowercase()
            return when {
                lower.startsWith("vless://") -> VLESS
                lower.startsWith("trojan://") -> TROJAN
                lower.startsWith("vmess://") -> VMESS
                lower.startsWith("ss://") || lower.startsWith("shadowsocks://") -> SHADOWSOCKS
                lower.startsWith("hy2://") || lower.startsWith("hysteria2://") || lower.startsWith("hysteria://") -> HYSTERIA2
                lower.startsWith("tuic://") -> TUIC
                else -> OTHER
            }
        }
    }
}

data class ProxyItem(
    val id: String,
    val rawUri: String,
    val protocol: ProxyProtocol,
    val server: String,
    val port: Int,
    val remark: String,
    val latencyMs: Long = -1L,
    val isWorking: Boolean = false
)
