package com.tvstream.app

import android.app.*
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.os.Build
import android.view.*
import android.webkit.*
import android.widget.*
import java.net.URI

class MainActivity : Activity() {
    private lateinit var web: WebView; private lateinit var progress: ProgressBar; private var custom: View? = null; private var customCallback: WebChromeClient.CustomViewCallback? = null
    private lateinit var adBlock: AdBlockManager
    private var loadedUrl: String? = null
    private var mainHost: String? = null
    private val immersive = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    override fun onCreate(state: Bundle?) { super.onCreate(state); build(); val url = Prefs.getUrl(this); if (url == null) startActivity(Intent(this, SettingsActivity::class.java)) else load(url) }
    private fun build() {
        val root = FrameLayout(this); web = WebView(this); progress = ProgressBar(this).apply { isIndeterminate = true; visibility = View.GONE }; root.addView(web); root.addView(progress, FrameLayout.LayoutParams(72, 72, Gravity.CENTER))
        if (resources.configuration.smallestScreenWidthDp < 600) root.addView(Button(this).apply { text = "Menu"; textSize = 14f; setOnClickListener { startActivity(Intent(this@MainActivity, SettingsActivity::class.java)) } }, FrameLayout.LayoutParams(-2, 52, Gravity.TOP or Gravity.END).apply { topMargin = 12; rightMargin = 12 })
        setContentView(root); window.decorView.systemUiVisibility = immersive
        web.settings.javaScriptEnabled = true; web.settings.domStorageEnabled = true; web.settings.mediaPlaybackRequiresUserGesture = false; web.settings.allowFileAccess = false; web.settings.allowContentAccess = false; web.settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW; if (Build.VERSION.SDK_INT >= 26) web.settings.safeBrowsingEnabled = true; web.settings.setSupportMultipleWindows(false); web.settings.javaScriptCanOpenWindowsAutomatically = false
        adBlock = AdBlockManager(this).also { it.loadAsync() }
        web.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(v: WebView, request: WebResourceRequest): WebResourceResponse? = if (request.isForMainFrame) null else adBlock.intercept(request.url.toString())
            override fun shouldOverrideUrlLoading(v: WebView, request: WebResourceRequest): Boolean { val u = request.url; return if (!allowMainNavigation(u.toString())) { toast("Popup atau redirect eksternal diblokir"); true } else false }
            @Suppress("DEPRECATION")
            override fun shouldOverrideUrlLoading(v: WebView, url: String): Boolean = if (!allowMainNavigation(url)) { toast("Popup atau redirect eksternal diblokir"); true } else false
            override fun onPageStarted(v: WebView, url: String, icon: android.graphics.Bitmap?) { if (url.equals("about:blank", true)) { v.stopLoading(); return }; progress.visibility = View.VISIBLE }
            override fun onPageFinished(v: WebView, url: String) { progress.visibility = View.GONE }
            override fun onReceivedError(v: WebView, r: WebResourceRequest, e: WebResourceError) { if (r.isForMainFrame) errorState() }
            override fun onReceivedSslError(v: WebView, h: SslErrorHandler, e: android.net.http.SslError) { h.cancel(); errorState() }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message): Boolean = false
            override fun onShowCustomView(view: View, callback: CustomViewCallback) { if (custom != null) { callback.onCustomViewHidden(); return }; custom = view; customCallback = callback; (web.parent as ViewGroup).addView(view, ViewGroup.LayoutParams(-1, -1)); web.visibility = View.GONE; window.decorView.systemUiVisibility = immersive }
            override fun onHideCustomView() { closeCustom() }
        }
    }
    private fun load(url: String) { if (UrlValidator.validate(url) == null) { Prefs.clearUrl(this); startActivity(Intent(this, SettingsActivity::class.java)) } else { loadedUrl = url; mainHost = runCatching { URI(url).host?.lowercase() }.getOrNull(); web.loadUrl(url) } }
    private fun allowMainNavigation(url: String): Boolean {
        if (url.equals("about:blank", true) || !url.startsWith("https://", true)) return false
        val host = runCatching { URI(url).host?.lowercase() }.getOrNull() ?: return false
        val root = mainHost ?: return true
        return host == root || host.endsWith(".$root")
    }
    private fun errorState() { progress.visibility = View.GONE; AlertDialog.Builder(this).setTitle("Gagal memuat website").setMessage("Periksa koneksi internet atau URL HTTPS.").setPositiveButton("Coba Lagi") { _, _ -> web.reload() }.setNeutralButton("Pengaturan") { _, _ -> startActivity(Intent(this, SettingsActivity::class.java)) }.setNegativeButton("Keluar") { _, _ -> finish() }.show() }
    override fun onKeyDown(key: Int, event: KeyEvent): Boolean { if (key == KeyEvent.KEYCODE_MENU || key == KeyEvent.KEYCODE_SETTINGS) { startActivity(Intent(this, SettingsActivity::class.java)); return true }; if (key == KeyEvent.KEYCODE_BACK) { if (custom != null) { closeCustom(); return true }; if (web.canGoBack()) { web.goBack(); return true } }; return super.onKeyDown(key, event) }
    private fun closeCustom() { custom?.let { (it.parent as? ViewGroup)?.removeView(it) }; custom = null; customCallback?.onCustomViewHidden(); customCallback = null; web.visibility = View.VISIBLE; window.decorView.systemUiVisibility = immersive }
    private fun toast(t: String) = Toast.makeText(this, t, Toast.LENGTH_SHORT).show()
    override fun onResume() { super.onResume(); window.decorView.systemUiVisibility = immersive; web.onResume(); adBlock.loadAsync(); Prefs.getUrl(this)?.let { if (it != loadedUrl) load(it) } }
    override fun onPause() { web.onPause(); super.onPause() }
    override fun onDestroy() { closeCustom(); web.stopLoading(); web.webChromeClient = null; web.webViewClient = WebViewClient(); web.destroy(); adBlock.shutdown(); super.onDestroy() }
}
