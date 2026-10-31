package com.tvstream.app

import java.net.URI

object UrlValidator {
    fun validate(raw: String): String? {
        val value = raw.trim()
        if (value.isEmpty() || value.contains("\n") || value.contains("\r")) return null
        val hasScheme = Regex("^[A-Za-z][A-Za-z0-9+.-]*:").containsMatchIn(value)
        val normalized = if (hasScheme) value else "https://$value"
        val uri = runCatching { URI(normalized) }.getOrNull()
        return if (uri != null && uri.scheme.equals("https", true) && !uri.host.isNullOrBlank() && uri.userInfo == null && uri.port <= 65535) normalized else null
    }
}
