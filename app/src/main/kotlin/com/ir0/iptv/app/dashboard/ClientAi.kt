package com.ir0.iptv.app.dashboard

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private const val ENDPOINT = "https://api.anthropic.com/v1/messages"
private const val VERSIONE_API = "2023-06-01"
private const val BETA_FALLBACK = "server-side-fallback-2026-07-01"
private const val MODELLO = "claude-opus-5"
private const val MAX_TOKEN = 1024
private const val CONNECT_TIMEOUT_MS = 10_000
private const val READ_TIMEOUT_MS = 60_000

/**
 * Chiamata diretta dall'app all'API di Claude con la chiave dell'utente (ADR 0003).
 * Niente SDK: il modulo :app non ha un client HTTP e l'SDK Java richiederebbe il desugaring
 * su minSdk 21; qui basta lo stesso HttpURLConnection gia' usato per le Sorgenti.
 */
class ClientAi {

    /** Null quando la chiamata fallisce o il modello declina: i Suggerimenti sono un extra. */
    suspend fun rispondi(chiaveApi: String, prompt: String): String? = withContext(Dispatchers.IO) {
        val connessione = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doOutput = true
            setRequestProperty("content-type", "application/json")
            setRequestProperty("x-api-key", chiaveApi)
            setRequestProperty("anthropic-version", VERSIONE_API)
            setRequestProperty("anthropic-beta", BETA_FALLBACK)
        }
        try {
            connessione.outputStream.use { it.write(corpo(prompt).toString().toByteArray()) }
            if (connessione.responseCode !in 200..299) return@withContext null
            val risposta = JSONObject(connessione.inputStream.bufferedReader().use { it.readText() })
            if (risposta.optString("stop_reason") == "refusal") return@withContext null
            testoDi(risposta)
        } catch (e: Exception) {
            null
        } finally {
            connessione.disconnect()
        }
    }

    private fun corpo(prompt: String) = JSONObject()
        .put("model", MODELLO)
        .put("max_tokens", MAX_TOKEN)
        .put("fallbacks", "default")
        .put(
            "messages",
            JSONArray().put(JSONObject().put("role", "user").put("content", prompt))
        )

    private fun testoDi(risposta: JSONObject): String? {
        val blocchi = risposta.optJSONArray("content") ?: return null
        return (0 until blocchi.length())
            .map { blocchi.getJSONObject(it) }
            .filter { it.optString("type") == "text" }
            .joinToString("\n") { it.optString("text") }
            .ifBlank { null }
    }
}
