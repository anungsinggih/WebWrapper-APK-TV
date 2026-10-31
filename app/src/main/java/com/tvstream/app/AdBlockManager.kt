package com.tvstream.app

import android.content.Context
import android.webkit.WebResourceResponse
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

class AdBlockManager(private val context: Context) {
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val active = AtomicReference<AdBlockEngine?>(null)
    fun loadAsync() {
        executor.execute { active.set(AdBlockEngine.create(config()) { Prefs.addBlocked(context) }) }
    }
    fun intercept(url: String): WebResourceResponse? = active.get()?.intercept(url)
    fun reloadAsync() = loadAsync()
    fun shutdown() = executor.shutdownNow()
    private fun config() = AdBlockConfig(Prefs.adEnabled(context), Prefs.lists(context), Prefs.customRules(context), AdBlockRepository.read(context), Prefs.whitelist(context))
}
