package com.tvstream.app

import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger

data class AdBlockConfig(val enabled: Boolean, val lists: Set<String>, val customRules: String, val remoteRules: String, val whitelist: String)

class AdBlockEngine private constructor(private val config: AdBlockConfig, private val onBlocked: () -> Unit = {}) {
    private val blocked = AtomicInteger(0)
    private val allow = config.whitelist.lines().asSequence().map { it.trim().lowercase().removePrefix("||").removeSuffix("^") }.filter { it.isNotBlank() }.toSet()
    private val rules = buildList {
        if ("easylist" in config.lists) addAll(BUILTIN_EASYLIST)
        if ("easyprivacy" in config.lists) addAll(BUILTIN_EASYPRIVACY)
        if ("adguard" in config.lists) addAll(BUILTIN_ADGUARD)
        addAll(parse(config.remoteRules)); addAll(parse(config.customRules))
    }.asSequence().mapNotNull(::parseRule).take(MAX_RULES).toList()
    private val hostRules = rules.filter { it.hostOnly }.map { it.token }.toSet()
    private val urlRules = rules.filterNot { it.hostOnly }.map { it.token }.toSet()

    fun blockedCount() = blocked.get()
    fun resetCount() = blocked.set(0)
    fun intercept(url: String): WebResourceResponse? {
        if (!config.enabled) return null
        val host = runCatching { URI(url).host?.lowercase() }.getOrNull() ?: return null
        if (allow.any { host == it || host.endsWith(".$it") }) return null
        val lowerUrl = url.lowercase()
        if (hostRules.any { host == it || host.endsWith(".$it") } || urlRules.any(lowerUrl::contains)) {
            blocked.incrementAndGet()
            onBlocked()
            return WebResourceResponse("text/plain", "UTF-8", 204, "No Content", emptyMap(), ByteArrayInputStream(ByteArray(0)))
        }
        return null
    }
    private fun parse(text: String) = text.lineSequence().filter { it.isNotBlank() && !it.trim().startsWith("!") && !it.trim().startsWith("#") }.toList()
    private fun parseRule(raw: String): Rule? {
        var line = raw.trim(); if (line.isEmpty() || line.startsWith("@@") || line.startsWith("/") || line.contains("##")) return null
        if (line.startsWith("||")) line = line.removePrefix("||")
        line = line.substringBefore("^").substringBefore("$").trim('/'); if (line.isEmpty()) return null
        val token = line.lowercase()
        return Rule(token, !token.contains("/") && !token.contains("?"))
    }
    private data class Rule(val token: String, val hostOnly: Boolean)
    companion object {
        const val MAX_RULES = 100_000
        const val BUILTIN_SNAPSHOT_DATE = "2026-11-01"
        const val EASYLIST_SOURCE = "https://easylist.to/easylist/easylist.txt"
        const val EASYPRIVACY_SOURCE = "https://easylist.to/easylist/easyprivacy.txt"
        const val ADGUARD_SOURCE = "https://filters.adtidy.org/extension/chromium/filters/2.txt"
        fun create(config: AdBlockConfig, onBlocked: () -> Unit = {}) = AdBlockEngine(config, onBlocked)
        val BUILTIN_EASYLIST = listOf("||doubleclick.net^", "||googlesyndication.com^", "||adservice.google.com^", "||adnxs.com^", "||adsrvr.org^", "||taboola.com^")
        val BUILTIN_EASYPRIVACY = listOf("||google-analytics.com^", "||googletagmanager.com^", "||facebook.net^", "||hotjar.com^", "||scorecardresearch.com^")
        val BUILTIN_ADGUARD = listOf("||adguard.com^", "||advertising.com^", "||criteo.com^", "||outbrain.com^", "||quantserve.com^")
    }
}
