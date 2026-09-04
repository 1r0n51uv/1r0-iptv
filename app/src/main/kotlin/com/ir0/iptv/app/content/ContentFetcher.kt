package com.ir0.iptv.app.content

import com.ir0.iptv.domain.classification.ContentClassifier
import com.ir0.iptv.domain.classification.ContentType
import com.ir0.iptv.domain.source.Sorgente
import com.ir0.iptv.domain.source.m3u.M3uEntry
import com.ir0.iptv.domain.source.m3u.M3uParser
import com.ir0.iptv.domain.source.xtream.XtreamConnection
import com.ir0.iptv.domain.source.xtream.XtreamLiveStreamDto
import com.ir0.iptv.domain.source.xtream.XtreamMapper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class ContentFetcher(
    private val m3uParser: M3uParser = M3uParser(),
    private val contentClassifier: ContentClassifier = ContentClassifier(),
    private val xtreamMapper: XtreamMapper = XtreamMapper()
) {

    suspend fun canali(sorgenti: List<Sorgente>): List<M3uEntry> = withContext(Dispatchers.IO) {
        sorgenti.flatMap { sorgente ->
            try {
                when (sorgente) {
                    is Sorgente.M3u -> canaliDaM3u(sorgente)
                    is Sorgente.Xtream -> canaliDaXtream(sorgente)
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun canaliDaM3u(sorgente: Sorgente.M3u): List<M3uEntry> {
        val contenuto = scarica(sorgente.url)
        return m3uParser.parse(contenuto).filter { contentClassifier.classify(it) == ContentType.CANALE }
    }

    private fun canaliDaXtream(sorgente: Sorgente.Xtream): List<M3uEntry> {
        val connection = sorgente.connection
        val url = xtreamApiUrl(connection, "get_live_streams")
        val array = JSONArray(scarica(url))
        return (0 until array.length()).map { i ->
            xtreamMapper.toChannel(array.getJSONObject(i).toLiveStreamDto(), connection)
        }
    }

    private fun scarica(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        return try {
            BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}

private fun xtreamApiUrl(connection: XtreamConnection, action: String): String =
    "http://${connection.host}:${connection.port}/player_api.php" +
        "?username=${connection.username}&password=${connection.password}&action=$action"

private fun JSONObject.toLiveStreamDto(): XtreamLiveStreamDto = XtreamLiveStreamDto(
    name = getString("name"),
    streamId = getInt("stream_id"),
    streamIcon = optStringOrNull("stream_icon"),
    epgChannelId = optStringOrNull("epg_channel_id"),
    categoryName = optStringOrNull("category_name")
)

private fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) getString(key) else null
