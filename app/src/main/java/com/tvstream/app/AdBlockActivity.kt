package com.tvstream.app

import android.app.*
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import kotlin.concurrent.thread

class AdBlockActivity : Activity() {
    private val isTv get() = resources.configuration.smallestScreenWidthDp >= 600
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun label(text: String) = TextView(this).apply { this.text = text; textSize = if (isTv) 20f else 16f; setTextColor(0xffffffff.toInt()); setPadding(0, dp(8), 0, dp(4)) }
    override fun onCreate(state: Bundle?) { super.onCreate(state); val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(if (isTv) 48 else 20), dp(18), dp(if (isTv) 48 else 20), dp(18)); setBackgroundColor(0xff101820.toInt()) }; val scroll = ScrollView(this); val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }; scroll.addView(body); root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        body.addView(label("Ad Blocker")); val enabled = CheckBox(this).apply { text = "Enable Ad Blocker"; textSize = 18f; isChecked = Prefs.adEnabled(this@AdBlockActivity); setTextColor(0xffffffff.toInt()) }; body.addView(enabled)
        body.addView(label("Filter Lists (snapshot ${AdBlockEngine.BUILTIN_SNAPSHOT_DATE})")); val checks = mapOf("easylist" to "EasyList subset", "easyprivacy" to "EasyPrivacy subset", "adguard" to "AdGuard DNS Filter subset").map { (key, text) -> key to CheckBox(this).apply { this.text = text; textSize = 16f; isChecked = key in Prefs.lists(this@AdBlockActivity); setTextColor(0xffffffff.toInt()) } }.toMap(); checks.values.forEach(body::addView)
        body.addView(label("Custom Filter URLs (HTTPS, satu URL per baris)")); val url = EditText(this).apply { setText(Prefs.customUrl(this@AdBlockActivity)); hint = "https://example.com/ads.txt\nhttps://example.org/privacy.txt"; minLines = 3; gravity = Gravity.TOP; setTextColor(0xffffffff.toInt()); setHintTextColor(0xffb0bec5.toInt()) }; body.addView(url)
        body.addView(label("My Filters (uBO subset / hosts)")); val rules = EditText(this).apply { setText(Prefs.customRules(this@AdBlockActivity)); hint = "||ads.example.com^\nexample-tracker.com"; minLines = 4; gravity = Gravity.TOP; setTextColor(0xffffffff.toInt()); setHintTextColor(0xffb0bec5.toInt()) }; body.addView(rules)
        body.addView(label("Whitelisted Websites (one hostname per line)")); val white = EditText(this).apply { setText(Prefs.whitelist(this@AdBlockActivity)); hint = "example.com"; minLines = 2; gravity = Gravity.TOP; setTextColor(0xffffffff.toInt()); setHintTextColor(0xffb0bec5.toInt()) }; body.addView(white)
        val save = Button(this).apply { text = "Simpan Ad Blocker" }; val update = Button(this).apply { text = "Update Filter URL" }; val stats = Button(this).apply { text = "Statistics: counter aktif di sesi browsing" }; val back = Button(this).apply { text = "Kembali" }; listOf(save, update, stats, back).forEach { body.addView(it, LinearLayout.LayoutParams(-1, dp(if (isTv) 58 else 52)).apply { topMargin = dp(8) }) }
        save.setOnClickListener { Prefs.setAdEnabled(this, enabled.isChecked); Prefs.setLists(this, checks.filterValues { it.isChecked }.keys); Prefs.setCustomUrl(this, url.text.toString().trim()); Prefs.setCustomRules(this, rules.text.toString()); Prefs.setWhitelist(this, white.text.toString()); finish() }
        update.setOnClickListener { val sources = url.text.toString().lines().map { it.trim() }.filter { it.isNotEmpty() }; if (sources.isEmpty() || sources.any { !it.startsWith("https://") }) dialog("URL filter harus HTTPS", "Masukkan satu atau beberapa URL HTTPS, satu URL per baris.") else { update.isEnabled = false; thread { val result = AdBlockRepository.updateAll(this, sources); runOnUiThread { update.isEnabled = true; result.fold({ count -> dialog("Update selesai", "${sources.size} list dan $count rule tervalidasi. Filter aktif setelah kembali ke website.") }, { error -> dialog("Update gagal", error.message ?: "Filter tidak dapat diunduh. Filter lama tetap dipakai.") }) } } } }
        stats.text = "Statistics: ${Prefs.blockedCount(this)} request diblokir"
        stats.setOnClickListener { dialog("Statistics", "${Prefs.blockedCount(this)} request diblokir sejak counter terakhir dihapus.") }; back.setOnClickListener { finish() }; setContentView(root)
    }
    private fun dialog(title: String, text: String) = AlertDialog.Builder(this).setTitle(title).setMessage(text).setPositiveButton("OK", null).show()
}
