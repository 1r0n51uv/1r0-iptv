package com.ir0.iptv.app.sport

import com.ir0.iptv.domain.sport.PartitaLive
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private const val ENDPOINT = "https://api.football-data.org/v4/matches"
private const val CONNECT_TIMEOUT_MS = 10_000
private const val READ_TIMEOUT_MS = 20_000

/**
 * football-data.org: piano gratuito con chiave via email, copre la Serie A e le altre
 * competizioni principali, 10 richieste al minuto - abbastanza per una riga di Dashboard.
 * Il parsing e' volutamente indulgente: se il provider risponde in un formato diverso da
 * quello atteso la riga sport sparisce, non rompe la Dashboard.
 */
class ClientSport {

    suspend fun partiteDiOggi(chiaveApi: String): List<PartitaLive> = withContext(Dispatchers.IO) {
        val connessione = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("X-Auth-Token", chiaveApi)
        }
        try {
            if (connessione.responseCode !in 200..299) return@withContext emptyList()
            val risposta = JSONObject(connessione.inputStream.bufferedReader().use { it.readText() })
            val partite = risposta.optJSONArray("matches") ?: return@withContext emptyList()
            (0 until partite.length()).mapNotNull { partite.getJSONObject(it).toPartita() }
        } catch (e: Exception) {
            emptyList()
        } finally {
            connessione.disconnect()
        }
    }
}

private val FORMATO_UTC = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

private fun JSONObject.toPartita(): PartitaLive? {
    val casa = optJSONObject("homeTeam")?.optStringOrNull("shortName")
        ?: optJSONObject("homeTeam")?.optStringOrNull("name")
        ?: return null
    val ospite = optJSONObject("awayTeam")?.optStringOrNull("shortName")
        ?: optJSONObject("awayTeam")?.optStringOrNull("name")
        ?: return null
    val punteggio = optJSONObject("score")?.optJSONObject("fullTime")
    val stato = optStringOrNull("status")
    return PartitaLive(
        casa = casa,
        ospite = ospite,
        competizione = optJSONObject("competition")?.optStringOrNull("name"),
        golCasa = punteggio?.optIntOrNull("home"),
        golOspite = punteggio?.optIntOrNull("away"),
        inCorso = stato == "IN_PLAY" || stato == "PAUSED",
        inizioMs = optStringOrNull("utcDate")?.let { data ->
            try {
                FORMATO_UTC.parse(data)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        } ?: 0L
    )
}

private fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) getString(key) else null

private fun JSONObject.optIntOrNull(key: String): Int? =
    if (has(key) && !isNull(key)) optInt(key) else null
