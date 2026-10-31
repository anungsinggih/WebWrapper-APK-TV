package com.tvstream.app

import android.content.Context

object Prefs {
    private const val FILE = "tv_stream"; private const val URL = "url"; private const val AD_ENABLED = "ad_enabled"; private const val LISTS = "ad_lists"; private const val CUSTOM = "ad_custom"; private const val CUSTOM_URL = "ad_custom_url"; private const val REMOTE = "ad_remote"; private const val WHITE = "ad_white"; private const val BLOCKED = "ad_blocked"
    fun getUrl(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(URL, null)
    fun putUrl(c: Context, url: String) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(URL, url).apply()
    fun clearUrl(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().remove(URL).apply()
    private fun p(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    fun adEnabled(c: Context) = p(c).getBoolean(AD_ENABLED, true)
    fun setAdEnabled(c: Context, value: Boolean) = p(c).edit().putBoolean(AD_ENABLED, value).apply()
    fun lists(c: Context) = p(c).getStringSet(LISTS, setOf("easylist", "easyprivacy", "adguard")) ?: emptySet()
    fun setLists(c: Context, value: Set<String>) = p(c).edit().putStringSet(LISTS, value).apply()
    fun customRules(c: Context) = p(c).getString(CUSTOM, "") ?: ""
    fun setCustomRules(c: Context, value: String) = p(c).edit().putString(CUSTOM, value).apply()
    fun customUrl(c: Context) = p(c).getString(CUSTOM_URL, "") ?: ""
    fun setCustomUrl(c: Context, value: String) = p(c).edit().putString(CUSTOM_URL, value).apply()
    fun remoteRules(c: Context) = p(c).getString(REMOTE, "") ?: ""
    fun setRemoteRules(c: Context, value: String) = p(c).edit().putString(REMOTE, value).apply()
    fun whitelist(c: Context) = p(c).getString(WHITE, "") ?: ""
    fun setWhitelist(c: Context, value: String) = p(c).edit().putString(WHITE, value).apply()
    fun blockedCount(c: Context) = p(c).getInt(BLOCKED, 0)
    @Synchronized fun addBlocked(c: Context) = p(c).edit().putInt(BLOCKED, blockedCount(c) + 1).apply()
    fun clearBlocked(c: Context) = p(c).edit().remove(BLOCKED).apply()
}
