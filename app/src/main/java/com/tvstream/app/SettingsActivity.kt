package com.tvstream.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Build
import android.webkit.WebView
import android.view.Gravity
import android.view.View
import android.widget.*
import android.util.TypedValue

class SettingsActivity : Activity() {
    private lateinit var input: EditText
    private val isTv get() = resources.configuration.smallestScreenWidthDp >= 600
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun button(text: String) = Button(this).apply { this.text = text; textSize = if (isTv) 18f else 16f; isFocusable = true; minHeight = dp(if (isTv) 56 else 48) }
    override fun onCreate(state: Bundle?) { super.onCreate(state); build(); if (isTv) window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY }
    private fun build() {
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(if (isTv) 48 else 20), dp(if (isTv) 30 else 18), dp(if (isTv) 48 else 20), dp(if (isTv) 24 else 18)); gravity = Gravity.CENTER_VERTICAL; setBackgroundColor(0xff101820.toInt()) }
        content.addView(TextView(this).apply { text = "TV Stream\nPengaturan website"; textSize = if (isTv) 28f else 24f; setTextColor(0xffffffff.toInt()); setPadding(0, 0, 0, dp(if (isTv) 20 else 12)) })
        input = EditText(this).apply { hint = "https://contoh.tld"; setText(Prefs.getUrl(this@SettingsActivity).orEmpty()); textSize = if (isTv) 20f else 16f; setSingleLine(true); setTextColor(0xffffffff.toInt()); setHintTextColor(0xffb0bec5.toInt()); inputType = 33; isFocusable = true }
        content.addView(input, LinearLayout.LayoutParams(-1, dp(if (isTv) 64 else 56)))
        val save = button("Simpan dan Buka"); val reload = button("Reload Website"); val cache = button("Clear Cache"); val delete = button("Hapus URL Tersimpan"); val blocker = button("Ad Blocker"); val about = button("Tentang"); val cancel = button("Batal / Kembali")
        listOf(save, reload, cache, delete, blocker, about, cancel).forEach { content.addView(it, LinearLayout.LayoutParams(-1, dp(if (isTv) 62 else 54)).apply { topMargin = dp(8) }) }
        save.setOnClickListener { val url = UrlValidator.validate(input.text.toString()); if (url == null) message("URL ditolak", "Masukkan domain atau URL HTTPS valid tanpa username/password.") else { Prefs.putUrl(this, url); input.setText(url); finish() } }
        reload.setOnClickListener { if (Prefs.getUrl(this) != null) finish() else message("Belum ada URL", "Simpan URL HTTPS terlebih dahulu.") }
        cache.setOnClickListener { confirm("Clear Cache", "Cache WebView akan dihapus. Data login tetap tersimpan.") { WebView(this).clearCache(true); message("Selesai", "Cache WebView telah dihapus.") } }
        delete.setOnClickListener { confirm("Hapus URL", "URL tersimpan akan dihapus.") { Prefs.clearUrl(this); input.setText("") } }
        blocker.setOnClickListener { startActivity(android.content.Intent(this, AdBlockActivity::class.java)) }
        about.setOnClickListener { message("TV Stream 1.0", "WebView: ${if (Build.VERSION.SDK_INT >= 26) WebView.getCurrentWebViewPackage()?.versionName ?: "provider tidak diketahui" else "provider perangkat"}\nHTTPS only. Tidak ada telemetry.") }
        cancel.setOnClickListener { finish() }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(content) }); input.requestFocus()
    }
    private fun message(title: String, text: String, action: (() -> Unit)? = null) { AlertDialog.Builder(this).setTitle(title).setMessage(text).setPositiveButton("OK") { _, _ -> action?.invoke() }.show() }
    private fun confirm(title: String, text: String, action: () -> Unit) { AlertDialog.Builder(this).setTitle(title).setMessage(text).setNegativeButton("Batal", null).setPositiveButton("Ya") { _, _ -> action() }.show() }
}
