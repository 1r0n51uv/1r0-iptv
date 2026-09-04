package com.ir0.iptv.app.content

import com.ir0.iptv.domain.classification.ContentClassifier
import com.ir0.iptv.domain.classification.ContentType
import com.ir0.iptv.domain.classification.SeriesGrouper
import com.ir0.iptv.domain.source.Sorgente
import com.ir0.iptv.domain.source.m3u.M3uEntry
import com.ir0.iptv.domain.source.m3u.M3uParser
import com.ir0.iptv.domain.source.xtream.XtreamConnection
import com.ir0.iptv.domain.source.xtream.XtreamEpisodeDto
import com.ir0.iptv.domain.source.xtream.XtreamLiveStreamDto
import com.ir0.iptv.domain.source.xtream.XtreamMapper
import com.ir0.iptv.domain.source.xtream.XtreamSeriesInfoDto
import com.ir0.iptv.domain.source.xtream.XtreamVodStreamDto
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
    private val seriesGrouper: SeriesGrouper = SeriesGrouper(),
    private val xtreamMapper: XtreamMapper = XtreamMapper()
) {

    suspend fun catalogo(sorgenti: List<Sorgente>): ContentCatalog = withContext(Dispatchers.IO) {
        val cataloghi = sorgenti.map { sorgente ->
            try {
                when (sorgente) {
                    is Sorgente.M3u -> catalogoDaM3u(sorgente)
                    is Sorgente.Xtream -> catalogoDaXtream(sorgente)
                }
            } catch (e: Exception) {
                ContentCatalog()
            }
        }
        ContentCatalog(
            canali = cataloghi.flatMap { it.canali },
            film = cataloghi.flatMap { it.film },
            serie = cataloghi.flatMap { it.serie }
        )
    }

    private fun catalogoDaM3u(sorgente: Sorgente.M3u): ContentCatalog {
        val entries = m3uParser.parse(scarica(sorgente.url))
        val perTipo = entries.groupBy { contentClassifier.classify(it) }

        val canali = perTipo[ContentType.CANALE].orEmpty().map { it.toCanaleCard() }
        val film = perTipo[ContentType.FILM].orEmpty().map { it.toFilmCard() }
        val serie = seriesGrouper.group(perTipo[ContentType.SERIE].orEmpty())
            .map { ContentCard.SerieCard(title = it.name, imageUrl = it.poster, serie = it) }

        return ContentCatalog(canali = canali, film = film, serie = serie)
    }

    private fun catalogoDaXtream(sorgente: Sorgente.Xtream): ContentCatalog {
        val connection = sorgente.connection

        val canali = tryOrEmpty {
            val array = JSONArray(scarica(xtreamApiUrl(connection, "get_live_streams")))
            (0 until array.length()).map { i ->
                xtreamMapper.toChannel(array.getJSONObject(i).toLiveStreamDto(), connection).toCanaleCard()
            }
        }

        val film = tryOrEmpty {
            val array = JSONArray(scarica(xtreamApiUrl(connection, "get_vod_streams")))
            (0 until array.length()).map { i ->
                val movie = xtreamMapper.toMovie(array.getJSONObject(i).toVodStreamDto(), connection)
                ContentCard.Film(title = movie.title, imageUrl = movie.poster, streamUrl = movie.url)
            }
        }

        val serie = tryOrEmpty {
            val array = JSONArray(scarica(xtreamApiUrl(connection, "get_series")))
            (0 until array.length()).mapNotNull { i ->
                val seriesId = array.getJSONObject(i).optInt("series_id", -1)
                if (seriesId < 0) return@mapNotNull null
                tryOrNull {
                    val infoUrl = xtreamApiUrl(connection, "get_series_info") + "&series_id=$seriesId"
                    val serieDto = JSONObject(scarica(infoUrl)).toSeriesInfoDto()
                    val serieDomain = xtreamMapper.toSerie(serieDto, connection)
                    ContentCard.SerieCard(title = serieDomain.name, imageUrl = serieDomain.poster, serie = serieDomain)
                }
            }
        }

        return ContentCatalog(canali = canali, film = film, serie = serie)
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

private inline fun <T> tryOrEmpty(block: () -> List<T>): List<T> = try {
    block()
} catch (e: Exception) {
    emptyList()
}

private inline fun <T> tryOrNull(block: () -> T): T? = try {
    block()
} catch (e: Exception) {
    null
}

private fun M3uEntry.toCanaleCard() = ContentCard.Canale(title = title, imageUrl = tvgLogo, streamUrl = url)

private fun M3uEntry.toFilmCard() = ContentCard.Film(title = title, imageUrl = tvgLogo, streamUrl = url)

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

private fun JSONObject.toVodStreamDto(): XtreamVodStreamDto = XtreamVodStreamDto(
    name = getString("name"),
    streamId = getInt("stream_id"),
    streamIcon = optStringOrNull("stream_icon"),
    plot = optStringOrNull("plot"),
    categoryName = optStringOrNull("category_name"),
    containerExtension = optStringOrNull("container_extension") ?: "mp4"
)

private fun JSONObject.toSeriesInfoDto(): XtreamSeriesInfoDto {
    val info = optJSONObject("info")
    val name = info?.optStringOrNull("name") ?: optStringOrNull("name") ?: "Serie"
    val cover = info?.optStringOrNull("cover")
    val episodesJson = optJSONObject("episodes") ?: JSONObject()
    val episodesBySeason = episodesJson.keys().asSequence().mapNotNull { seasonKey ->
        val seasonNumber = seasonKey.toIntOrNull() ?: return@mapNotNull null
        val episodesArray = episodesJson.getJSONArray(seasonKey)
        val episodes = (0 until episodesArray.length()).map { i -> episodesArray.getJSONObject(i).toEpisodeDto() }
        seasonNumber to episodes
    }.toMap()
    return XtreamSeriesInfoDto(seriesName = name, episodesBySeason = episodesBySeason, cover = cover)
}

private fun JSONObject.toEpisodeDto(): XtreamEpisodeDto = XtreamEpisodeDto(
    id = optInt("id", 0),
    episodeNum = optInt("episode_num", 0),
    title = optStringOrNull("title") ?: "Episodio",
    containerExtension = optStringOrNull("container_extension") ?: "mp4"
)

private fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) getString(key) else null
