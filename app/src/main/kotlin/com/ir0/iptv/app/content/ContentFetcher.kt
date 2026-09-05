package com.ir0.iptv.app.content

import android.util.Base64
import android.util.JsonReader
import android.util.JsonToken
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.catalog.DettaglioEsteso
import com.ir0.iptv.domain.catalog.RiferimentoXtream
import com.ir0.iptv.domain.classification.ContentClassifier
import com.ir0.iptv.domain.classification.ContentType
import com.ir0.iptv.domain.classification.SeriesGrouper
import com.ir0.iptv.domain.epg.Programma
import com.ir0.iptv.domain.epg.XtreamEpgListingDto
import com.ir0.iptv.domain.epg.XtreamEpgMapper
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
import org.json.JSONObject

private const val CONNECT_TIMEOUT_MS = 10_000
private const val READ_TIMEOUT_MS = 20_000

class ContentFetcher(
    private val m3uParser: M3uParser = M3uParser(),
    private val contentClassifier: ContentClassifier = ContentClassifier(),
    private val seriesGrouper: SeriesGrouper = SeriesGrouper(),
    private val xtreamMapper: XtreamMapper = XtreamMapper(),
    private val xtreamEpgMapper: XtreamEpgMapper = XtreamEpgMapper()
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

    suspend fun dettaglioSerie(card: ContentCard.SerieCard.DaCaricare): com.ir0.iptv.domain.classification.Serie? =
        withContext(Dispatchers.IO) {
            tryOrNull {
                val url = xtreamApiUrl(card.connection, "get_series_info") + "&series_id=${card.seriesId}"
                val serieDto = JSONObject(scarica(url)).toSeriesInfoDto()
                xtreamMapper.toSerie(serieDto, card.connection)
            }
        }

    /** Dettagli estesi di un Film Xtream (cast, regista, durata...); le Sorgenti M3U non li
     * espongono, quindi il Dettaglio mostra solo la trama gia' nel catalogo. */
    suspend fun dettaglioFilm(riferimento: RiferimentoXtream): DettaglioEsteso? = withContext(Dispatchers.IO) {
        tryOrNull {
            val url = xtreamApiUrl(riferimento.connection, "get_vod_info") + "&vod_id=${riferimento.streamId}"
            JSONObject(scarica(url)).optJSONObject("info")?.toDettaglioEsteso()
        }
    }

    /** Palinsesto di un Canale Xtream; le Sorgenti M3U non espongono un EPG (vedi ADR 0002). */
    suspend fun palinsesto(riferimento: RiferimentoXtream): List<Programma> = withContext(Dispatchers.IO) {
        tryOrNull {
            val url = xtreamApiUrl(riferimento.connection, "get_simple_data_table") +
                "&stream_id=${riferimento.streamId}"
            val listings = JSONObject(scarica(url)).optJSONArray("epg_listings") ?: return@tryOrNull emptyList()
            xtreamEpgMapper.toProgrammi(
                (0 until listings.length()).map { listings.getJSONObject(it).toEpgListingDto() }
            )
        }.orEmpty()
    }

    private fun catalogoDaM3u(sorgente: Sorgente.M3u): ContentCatalog {
        val entries = m3uParser.parse(scarica(sorgente.url))
        val perTipo = entries.groupBy { contentClassifier.classify(it) }

        val canali = perTipo[ContentType.CANALE].orEmpty().map { it.toCanaleCard() }
        val film = perTipo[ContentType.FILM].orEmpty().map { it.toFilmCard() }
        val serie = seriesGrouper.group(perTipo[ContentType.SERIE].orEmpty())
            .map { ContentCard.SerieCard.Pronta(title = it.name, imageUrl = it.poster, serie = it) }

        return ContentCatalog(canali = canali, film = film, serie = serie)
    }

    private fun catalogoDaXtream(sorgente: Sorgente.Xtream): ContentCatalog {
        val connection = sorgente.connection

        val canali = tryOrEmpty {
            streamJsonArray(xtreamApiUrl(connection, "get_live_streams")) { readLiveStreamDto() }
                .map { dto ->
                    xtreamMapper.toChannel(dto, connection).toCanaleCard()
                        .copy(xtream = RiferimentoXtream(dto.streamId, connection))
                }
        }

        val film = tryOrEmpty {
            streamJsonArray(xtreamApiUrl(connection, "get_vod_streams")) { readVodStreamDto() }
                .map { dto ->
                    val movie = xtreamMapper.toMovie(dto, connection)
                    ContentCard.Film(
                        title = movie.title,
                        imageUrl = movie.poster,
                        streamUrl = movie.url,
                        categoria = movie.categoryName,
                        plot = movie.plot,
                        xtream = RiferimentoXtream(dto.streamId, connection)
                    )
                }
        }

        val serie = tryOrEmpty {
            streamJsonArray(xtreamApiUrl(connection, "get_series")) { readSeriesListItem() }
                .map { item ->
                    ContentCard.SerieCard.DaCaricare(
                        title = item.name,
                        imageUrl = item.cover,
                        seriesId = item.seriesId,
                        connection = connection,
                        plot = item.plot,
                        categoria = item.categoryName
                    )
                }
        }

        return ContentCatalog(canali = canali, film = film, serie = serie)
    }

    /** Reads a large JSON array straight off the response stream, one object at a time, instead of
     * materializing the whole (often tens of MB) response body as a String first - real Xtream
     * catalogs are big enough that doing so risks an OutOfMemoryError on a TV's constrained heap. */
    private fun <T> streamJsonArray(url: String, parseItem: JsonReader.() -> T): List<T> {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        return try {
            JsonReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { reader ->
                reader.isLenient = true
                val results = mutableListOf<T>()
                reader.beginArray()
                while (reader.hasNext()) {
                    results += reader.parseItem()
                }
                reader.endArray()
                results
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun scarica(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
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

private fun M3uEntry.toCanaleCard() =
    ContentCard.Canale(title = title, imageUrl = tvgLogo, streamUrl = url, categoria = groupTitle)

private fun M3uEntry.toFilmCard() =
    ContentCard.Film(title = title, imageUrl = tvgLogo, streamUrl = url, categoria = groupTitle)

private fun xtreamApiUrl(connection: XtreamConnection, action: String): String =
    "http://${connection.host}:${connection.port}/player_api.php" +
        "?username=${connection.username}&password=${connection.password}&action=$action"

private data class SeriesListItem(
    val seriesId: Int,
    val name: String,
    val cover: String?,
    val plot: String?,
    val categoryName: String?
)

private fun JsonReader.readLiveStreamDto(): XtreamLiveStreamDto {
    var name = ""
    var streamId = 0
    var streamIcon: String? = null
    var epgChannelId: String? = null
    var categoryName: String? = null
    beginObject()
    while (hasNext()) {
        when (nextName()) {
            "name" -> name = nextStringFlexible()
            "stream_id" -> streamId = nextIntFlexible()
            "stream_icon" -> streamIcon = nextStringOrNull()
            "epg_channel_id" -> epgChannelId = nextStringOrNull()
            "category_name" -> categoryName = nextStringOrNull()
            else -> skipValue()
        }
    }
    endObject()
    return XtreamLiveStreamDto(name, streamId, streamIcon, epgChannelId, categoryName)
}

private fun JsonReader.readVodStreamDto(): XtreamVodStreamDto {
    var name = ""
    var streamId = 0
    var streamIcon: String? = null
    var plot: String? = null
    var categoryName: String? = null
    var containerExtension = "mp4"
    beginObject()
    while (hasNext()) {
        when (nextName()) {
            "name" -> name = nextStringFlexible()
            "stream_id" -> streamId = nextIntFlexible()
            "stream_icon" -> streamIcon = nextStringOrNull()
            "plot" -> plot = nextStringOrNull()
            "category_name" -> categoryName = nextStringOrNull()
            "container_extension" -> containerExtension = nextStringOrNull() ?: "mp4"
            else -> skipValue()
        }
    }
    endObject()
    return XtreamVodStreamDto(name, streamId, streamIcon, plot, categoryName, containerExtension)
}

private fun JsonReader.readSeriesListItem(): SeriesListItem {
    var seriesId = -1
    var name = ""
    var cover: String? = null
    var plot: String? = null
    var categoryName: String? = null
    beginObject()
    while (hasNext()) {
        when (nextName()) {
            "series_id" -> seriesId = nextIntFlexible()
            "name" -> name = nextStringFlexible()
            "cover" -> cover = nextStringOrNull()
            "plot" -> plot = nextStringOrNull()
            "category_name" -> categoryName = nextStringOrNull()
            else -> skipValue()
        }
    }
    endObject()
    return SeriesListItem(seriesId, name, cover, plot, categoryName)
}

private fun JsonReader.nextStringOrNull(): String? = when (peek()) {
    JsonToken.NULL -> {
        nextNull()
        null
    }

    else -> nextString()
}

private fun JsonReader.nextStringFlexible(): String = nextStringOrNull().orEmpty()

private fun JsonReader.nextIntFlexible(): Int = when (peek()) {
    JsonToken.NULL -> {
        nextNull()
        0
    }

    JsonToken.STRING -> nextString().toIntOrNull() ?: 0
    else -> nextInt()
}

private fun JSONObject.toSeriesInfoDto(): XtreamSeriesInfoDto {
    val info = optJSONObject("info")
    val name = info?.optStringOrNull("name") ?: optStringOrNull("name") ?: "Serie"
    val cover = info?.optStringOrNull("cover")
    val plot = info?.optStringOrNull("plot")
    val episodesJson = optJSONObject("episodes") ?: JSONObject()
    val episodesBySeason = episodesJson.keys().asSequence().mapNotNull { seasonKey ->
        val seasonNumber = seasonKey.toIntOrNull() ?: return@mapNotNull null
        val episodesArray = episodesJson.getJSONArray(seasonKey)
        val episodes = (0 until episodesArray.length()).map { i -> episodesArray.getJSONObject(i).toEpisodeDto() }
        seasonNumber to episodes
    }.toMap()
    return XtreamSeriesInfoDto(
        seriesName = name,
        episodesBySeason = episodesBySeason,
        cover = cover,
        plot = plot
    )
}

private fun JSONObject.toEpisodeDto(): XtreamEpisodeDto = XtreamEpisodeDto(
    id = optInt("id", 0),
    episodeNum = optInt("episode_num", 0),
    title = optStringOrNull("title") ?: "Episodio",
    containerExtension = optStringOrNull("container_extension") ?: "mp4",
    immagine = optJSONObject("info")?.optStringOrNull("movie_image")
)

private fun JSONObject.toEpgListingDto(): XtreamEpgListingDto = XtreamEpgListingDto(
    titolo = optStringOrNull("title").orEmpty().decodificaBase64(),
    descrizione = optStringOrNull("description")?.decodificaBase64(),
    inizioSecondi = optStringOrNull("start_timestamp")?.toLongOrNull() ?: 0,
    fineSecondi = optStringOrNull("stop_timestamp")?.toLongOrNull() ?: 0
)

/** Xtream manda titolo e descrizione in base64, ma non tutti i provider lo fanno:
 * se la decodifica non produce testo sensato si tiene il valore originale. */
private fun String.decodificaBase64(): String = try {
    String(Base64.decode(this, Base64.DEFAULT), Charsets.UTF_8).ifBlank { this }
} catch (e: IllegalArgumentException) {
    this
}

private fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) getString(key) else null

/** Xtream non concorda su quali campi mette in get_vod_info ne' sul loro formato: si prende
 * quel che c'e' senza pretendere una forma precisa, cosi' un provider "povero" mostra solo i
 * campi che ha invece di far sparire tutta la Testata del Dettaglio. */
private fun JSONObject.toDettaglioEsteso(): DettaglioEsteso {
    val durataSecondi = optStringOrNull("duration_secs")?.toLongOrNull()
    return DettaglioEsteso(
        genere = optStringOrNull("genre")?.takeIf { it.isNotBlank() },
        cast = optStringOrNull("cast")?.takeIf { it.isNotBlank() },
        regista = optStringOrNull("director")?.takeIf { it.isNotBlank() },
        durata = optStringOrNull("duration")?.takeIf { it.isNotBlank() }
            ?: durataSecondi?.let { formattaDurata(it) },
        anno = optStringOrNull("releasedate")?.takeIf { it.length >= 4 }?.take(4)
            ?: optStringOrNull("release_date")?.takeIf { it.length >= 4 }?.take(4),
        valutazione = optStringOrNull("rating")?.toDoubleOrNull()?.takeIf { it > 0 }
    )
}

private fun formattaDurata(secondi: Long): String {
    val ore = secondi / 3600
    val minuti = (secondi % 3600) / 60
    return if (ore > 0) "${ore}h ${minuti}min" else "${minuti}min"
}
