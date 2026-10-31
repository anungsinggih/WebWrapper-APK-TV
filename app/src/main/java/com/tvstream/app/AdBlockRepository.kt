package com.tvstream.app

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import android.os.Build

object AdBlockRepository {
    const val MAX_BYTES = 5L * 1024 * 1024
    const val MAX_TOTAL_BYTES = 20L * 1024 * 1024
    const val MAX_RULES = 100_000
    private const val FILE = "adblock-remote.txt"
    fun read(context: Context): String = runCatching { File(context.filesDir, FILE).readText() }.getOrDefault("")
    fun update(context: Context, source: String): Result<Int> = runCatching {
        updateAll(context, listOf(source)).getOrThrow()
    }
    fun updateAll(context: Context, sources: List<String>): Result<Int> = runCatching {
        require(sources.isNotEmpty()) { "Masukkan minimal satu URL filter" }
        require(sources.size <= 20) { "Maksimal 20 URL filter" }
        val combined = File(context.filesDir, "$FILE.tmp")
        var totalBytes = 0L
        var totalRules = 0
        combined.outputStream().use { output ->
            sources.forEachIndexed { index, source ->
                require(source.startsWith("https://")) { "URL filter #${index + 1} harus HTTPS" }
                val connection = (URL(source).openConnection() as HttpURLConnection).apply { connectTimeout = 10_000; readTimeout = 15_000; instanceFollowRedirects = false }
                require(connection.responseCode in 200..299) { "Filter #${index + 1}: HTTP ${connection.responseCode}" }
                val buffer = ByteArray(8192); var listBytes = 0L; var count: Int
                connection.inputStream.use { input -> while (input.read(buffer).also { count = it } != -1) {
                    listBytes += count; totalBytes += count
                    require(listBytes <= MAX_BYTES) { "Filter #${index + 1} terlalu besar" }
                    require(totalBytes <= MAX_TOTAL_BYTES) { "Total filter terlalu besar" }
                    output.write(buffer, 0, count)
                } }
                output.write("\n".toByteArray())
            }
        }
        val text = combined.readText()
        val supported = text.lineSequence().filter(::isSupportedRule).take(MAX_RULES).toList()
        totalRules = supported.size
        combined.writeText(supported.joinToString("\n"))
        val target = File(context.filesDir, FILE)
        if (Build.VERSION.SDK_INT >= 26) {
            Files.move(combined.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } else {
            require(combined.renameTo(File(context.filesDir, "$FILE.next"))) { "Gagal menyiapkan filter" }
            val committed = File(context.filesDir, "$FILE.next")
            target.delete()
            require(committed.renameTo(target)) { "Gagal menyimpan filter" }
        }
        totalRules
    }.also { if (it.isFailure) File(context.filesDir, "$FILE.tmp").delete() }

    private fun isSupportedRule(raw: String): Boolean {
        val line = raw.trim()
        return line.isNotEmpty() && !line.startsWith("!") && !line.startsWith("#") &&
            !line.startsWith("@@") && !line.startsWith("/") && !line.contains("##")
    }
    internal fun isSupportedForTest(raw: String) = isSupportedRule(raw)
}
