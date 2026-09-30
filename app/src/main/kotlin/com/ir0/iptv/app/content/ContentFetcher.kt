package com.ir0.iptv.app.content

import android.util.Base64
import android.util.JsonReader
import android.util.JsonToken
import com.ir0.iptv.app.logging.RegistroApp
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
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
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

    suspend fun catalogo(
        sorgenti: List<Sorgente>,
        /** Chiamato prima di contattare ogni Sorgente: pilota la schermata di caricamento del
         * primissimo avvio e l'anello di avanzamento sull'icona "Aggiorna catalogo" in Sidebar. */
        onProgresso: (indice: Int, totale: Int, sorgente: Sorgente) -> Unit = { _, _, _ -> },
        /** Da 0f a 1f, quanto e' avanzata la Sorgente in corso (azzerato all'inizio di ognuna):
         * riempie il bordo dell'icona "Aggiorna catalogo" in Sidebar invece di limitarsi a farla
         * ruotare. Basato sui byte scaricati quando il server dichiara Content-Length (M3U) o sui
         * passi completati (Xtream: tre liste di categorie e tre di contenuti). */
        onProgressoFrazionale: (Float) -> Unit = {},
        /** Chiamato dopo ogni Sorgente con l'esito (null = riuscita): pilota lo stato mostrato nel
         * Pannello Web, cosi' una Sorgente irraggiungibile non fallisce piu' in silenzio. */
        onEsito: (sorgente: Sorgente, erroreMessaggio: String?) -> Unit = { _, _ -> }
    ): ContentCatalog = withContext(Dispatchers.IO) {
        val cataloghi = sorgenti.mapIndexed { indice, sorgente ->
            onProgresso(indice, sorgenti.size, sorgente)
            onProgressoFrazionale(0f)
            try {
                val risultato = when (sorgente) {
                    is Sorgente.M3u -> catalogoDaM3u(sorgente, onProgressoFrazionale)
                    is Sorgente.Xtream -> catalogoDaXtream(sorgente, onProgressoFrazionale)
                }
                onEsito(sorgente, null)
                risultato
            } catch (e: Exception) {
                // Il Registro dell'app resta locale al dispositivo: qui si tiene lo stack trace
                // completo (username/password incluse nell'URL, se e' quello il messaggio) perche'
                // e' l'unico posto dove serve davvero per capire cosa e' andato storto.
                RegistroApp.errore("Sorgente", "Sincronizzazione fallita per '${sorgente.nome}'", e)
                onEsito(sorgente, e.messaggioSenzaCredenziali())
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

    private fun catalogoDaM3u(sorgente: Sorgente.M3u, onProgressoFrazionale: (Float) -> Unit): ContentCatalog {
        // Non "scarica(...)": una playlist M3U reale puo' pesare decine o centinaia di MB di
        // testo (un provider con panel puo' includere decine di migliaia di Canali/Film/Serie).
        // Materializzarla come un'unica String, come faceva prima, e' esattamente il genere di
        // allocazione che manda in OutOfMemoryError l'heap ristretto di una TV - confermato da un
        // crash reale sul dispositivo passando da una Sorgente Xtream a M3U dello stesso account.
        val entries = scaricaRighe(sorgente.url, onProgressoFrazionale) { righe -> m3uParser.parse(righe) }
        val perTipo = entries.groupBy { contentClassifier.classify(it) }

        val canali = perTipo[ContentType.CANALE].orEmpty().map { it.toCanaleCard() }
        val film = perTipo[ContentType.FILM].orEmpty().map { it.toFilmCard() }
        val serie = seriesGrouper.group(perTipo[ContentType.SERIE].orEmpty())
            .map { ContentCard.SerieCard.Pronta(title = it.name, imageUrl = it.poster, serie = it) }

        return ContentCatalog(canali = canali, film = film, serie = serie)
    }

    private fun catalogoDaXtream(sorgente: Sorgente.Xtream, onProgressoFrazionale: (Float) -> Unit): ContentCatalog {
        val connection = sorgente.connection

        // Sei passi in tutto (tre liste di categorie, tre di contenuti): l'avanzamento complessivo
        // e' il passo gia' completato piu' l'eventuale avanzamento a byte del passo in corso, cosi'
        // il bordo dell'icona in Sidebar si riempie con continuita' invece di saltare a scatti.
        val passiTotali = 6
        var passiCompletati = 0
        val progressoPasso: (Float) -> Unit = { frazionePasso ->
            onProgressoFrazionale(((passiCompletati + frazionePasso) / passiTotali).coerceIn(0f, 1f))
        }
        fun completaPasso() {
            passiCompletati++
            onProgressoFrazionale((passiCompletati.toFloat() / passiTotali).coerceIn(0f, 1f))
        }

        // Non avvolta in tryOrEmpty a differenza delle altre chiamate qui sotto: se il provider e'
        // irraggiungibile (server giu', credenziali scadute...) deve saltare fuori subito e risalire
        // fino a "catalogo()", che la registra e la segnala come esito di sincronizzazione fallito.
        // Prima di questo fix un provider giu' produceva tre liste vuote in silenzio (ognuna
        // avvolta nel proprio tryOrEmpty) senza che nulla, ne' il Registro ne' il Pannello Web,
        // lo segnalasse: sembrava un catalogo vuoto per davvero, non una Sorgente irraggiungibile.
        val categorieCanali = streamJsonArray(xtreamApiUrl(connection, "get_live_categories"), progressoPasso) { readCategoria() }
            .associate { it.id to it.name }
        completaPasso()
        // Molti provider non mandano category_name sui singoli Canali/Film/Serie: le tre liste
        // di categorie risolvono category_id -> nome. Se una lista fallisce si prosegue senza,
        // la categoria resta null e il contenuto cade nel gruppo di ripiego invece di sparire.
        val categorieFilm = categorie(connection, "get_vod_categories", progressoPasso)
        completaPasso()
        val categorieSerie = categorie(connection, "get_series_categories", progressoPasso)
        completaPasso()

        val canali = tryOrEmpty {
            streamJsonArray(xtreamApiUrl(connection, "get_live_streams"), progressoPasso) { readLiveStreamDto() }
                .map { dto ->
                    xtreamMapper.toChannel(dto, connection).toCanaleCard()
                        .copy(
                            categoria = dto.categoryName ?: categorieCanali[dto.categoryId],
                            xtream = RiferimentoXtream(dto.streamId, connection)
                        )
                }
        }
        completaPasso()

        val film = tryOrEmpty {
            streamJsonArray(xtreamApiUrl(connection, "get_vod_streams"), progressoPasso) { readVodStreamDto() }
                .map { dto ->
                    val movie = xtreamMapper.toMovie(dto, connection)
                    ContentCard.Film(
                        title = movie.title,
                        imageUrl = movie.poster,
                        streamUrl = movie.url,
                        categoria = dto.categoryName ?: categorieFilm[dto.categoryId],
                        plot = movie.plot,
                        xtream = RiferimentoXtream(dto.streamId, connection)
                    )
                }
        }
        completaPasso()

        val serie = tryOrEmpty {
            streamJsonArray(xtreamApiUrl(connection, "get_series"), progressoPasso) { readSeriesListItem() }
                .map { item ->
                    ContentCard.SerieCard.DaCaricare(
                        title = item.name,
                        imageUrl = item.cover,
                        seriesId = item.seriesId,
                        connection = connection,
                        plot = item.plot,
                        categoria = item.categoryName ?: categorieSerie[item.categoryId]
                    )
                }
        }
        completaPasso()

        return ContentCatalog(canali = canali, film = film, serie = serie)
    }

    private fun categorie(connection: XtreamConnection, azione: String, onProgresso: (Float) -> Unit = {}): Map<String, String> =
        tryOrEmpty { streamJsonArray(xtreamApiUrl(connection, azione), onProgresso) { readCategoria() } }
            .associate { it.id to it.name }

    /** Reads a large JSON array straight off the response stream, one object at a time, instead of
     * materializing the whole (often tens of MB) response body as a String first - real Xtream
     * catalogs are big enough that doing so risks an OutOfMemoryError on a TV's constrained heap. */
    private fun <T> streamJsonArray(url: String, onProgresso: (Float) -> Unit = {}, parseItem: JsonReader.() -> T): List<T> {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        return try {
            val stream = StreamConProgresso(connection.inputStream, connection.contentLengthLong, onProgresso)
            JsonReader(InputStreamReader(stream, Charsets.UTF_8)).use { reader ->
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

    /** Come [scarica], ma passa le righe della risposta una alla volta invece di materializzarle
     * tutte insieme in un'unica String: per una risposta potenzialmente enorme (una playlist M3U
     * reale) e' l'unico modo per non rischiare un OutOfMemoryError sull'heap ristretto di una TV. */
    private fun <T> scaricaRighe(url: String, onProgresso: (Float) -> Unit = {}, azione: (Sequence<String>) -> T): T {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = CONNECT_TIMEOUT_MS
        connection.readTimeout = READ_TIMEOUT_MS
        return try {
            val stream = StreamConProgresso(connection.inputStream, connection.contentLengthLong, onProgresso)
            BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).useLines(azione)
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

/** Avvolge lo stream della risposta per riportare quanto letto rispetto al Content-Length
 * dichiarato dal server: pilota il bordo di avanzamento sull'icona "Aggiorna catalogo" in
 * Sidebar. Non essenziale al parsing (che continua a leggere da questo stream come se fosse
 * quello originale), quindi se il server non dichiara la lunghezza semplicemente non riporta
 * nulla invece di fallire o inventare un numero. */
private class StreamConProgresso(
    private val delegato: InputStream,
    contentLength: Long,
    private val onProgresso: (Float) -> Unit
) : InputStream() {
    private val lunghezzaAttesa = contentLength.takeIf { it > 0 }
    private var letti = 0L
    private var ultimaPercentualeInviata = -1

    override fun read(): Int {
        val b = delegato.read()
        if (b >= 0) segnala(1)
        return b
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val n = delegato.read(b, off, len)
        if (n > 0) segnala(n)
        return n
    }

    private fun segnala(n: Int) {
        val lunghezza = lunghezzaAttesa ?: return
        letti += n
        // Un aggiornamento per punto percentuale basta: niente ricomposizione ad ogni singola read().
        val percentuale = ((letti * 100) / lunghezza).toInt().coerceIn(0, 100)
        if (percentuale != ultimaPercentualeInviata) {
            ultimaPercentualeInviata = percentuale
            onProgresso(percentuale / 100f)
        }
    }

    override fun close() = delegato.close()
}

/** Il messaggio di una `FileNotFoundException` di rete e' l'URL della richiesta per intero: per
 * una Sorgente Xtream questo include username e password nella query string. Qui si taglia via la
 * query string prima che il messaggio finisca nel Registro dell'app o nel Pannello Web. */
private fun Exception.messaggioSenzaCredenziali(): String =
    (message ?: toString()).substringBefore("?")

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
    ContentCard.Canale(title = title, imageUrl = tvgLogo, streamUrl = url, categoria = groupTitle, tvgId = tvgId)

private fun M3uEntry.toFilmCard() =
    ContentCard.Film(title = title, imageUrl = tvgLogo, streamUrl = url, categoria = groupTitle, tvgId = tvgId)

private fun xtreamApiUrl(connection: XtreamConnection, action: String): String =
    "http://${connection.host}:${connection.port}/player_api.php" +
        "?username=${connection.username}&password=${connection.password}&action=$action"

private data class SeriesListItem(
    val seriesId: Int,
    val name: String,
    val cover: String?,
    val plot: String?,
    val categoryName: String?,
    val categoryId: String? = null
)

private data class XtreamCategoria(val id: String, val name: String)

private fun JsonReader.readLiveStreamDto(): XtreamLiveStreamDto {
    var name = ""
    var streamId = 0
    var streamIcon: String? = null
    var epgChannelId: String? = null
    var categoryName: String? = null
    var categoryId: String? = null
    beginObject()
    while (hasNext()) {
        when (nextName()) {
            "name" -> name = nextStringFlexible()
            "stream_id" -> streamId = nextIntFlexible()
            "stream_icon" -> streamIcon = nextStringOrNull()
            "epg_channel_id" -> epgChannelId = nextStringOrNull()
            "category_name" -> categoryName = nextStringOrNull()
            "category_id" -> categoryId = nextStringOrNull()
            else -> skipValue()
        }
    }
    endObject()
    return XtreamLiveStreamDto(name, streamId, streamIcon, epgChannelId, categoryName, categoryId)
}

private fun JsonReader.readVodStreamDto(): XtreamVodStreamDto {
    var name = ""
    var streamId = 0
    var streamIcon: String? = null
    var plot: String? = null
    var categoryName: String? = null
    var categoryId: String? = null
    var containerExtension = "mp4"
    beginObject()
    while (hasNext()) {
        when (nextName()) {
            "name" -> name = nextStringFlexible()
            "stream_id" -> streamId = nextIntFlexible()
            "stream_icon" -> streamIcon = nextStringOrNull()
            "plot" -> plot = nextStringOrNull()
            "category_name" -> categoryName = nextStringOrNull()
            "category_id" -> categoryId = nextStringOrNull()
            "container_extension" -> containerExtension = nextStringOrNull() ?: "mp4"
            else -> skipValue()
        }
    }
    endObject()
    return XtreamVodStreamDto(name, streamId, streamIcon, plot, categoryName, containerExtension, categoryId)
}

private fun JsonReader.readSeriesListItem(): SeriesListItem {
    var seriesId = -1
    var name = ""
    var cover: String? = null
    var plot: String? = null
    var categoryName: String? = null
    var categoryId: String? = null
    beginObject()
    while (hasNext()) {
        when (nextName()) {
            "series_id" -> seriesId = nextIntFlexible()
            "name" -> name = nextStringFlexible()
            "cover" -> cover = nextStringOrNull()
            "plot" -> plot = nextStringOrNull()
            "category_name" -> categoryName = nextStringOrNull()
            "category_id" -> categoryId = nextStringOrNull()
            else -> skipValue()
        }
    }
    endObject()
    return SeriesListItem(seriesId, name, cover, plot, categoryName, categoryId)
}

private fun JsonReader.readCategoria(): XtreamCategoria {
    var id = ""
    var name = ""
    beginObject()
    while (hasNext()) {
        when (nextName()) {
            "category_id" -> id = nextStringFlexible()
            "category_name" -> name = nextStringFlexible()
            else -> skipValue()
        }
    }
    endObject()
    return XtreamCategoria(id, name)
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
    val seasonsArray = optJSONArray("seasons") ?: JSONArray()
    val coverPerStagione = (0 until seasonsArray.length()).mapNotNull { i ->
        val stagione = seasonsArray.optJSONObject(i) ?: return@mapNotNull null
        val numero = stagione.opt("season_number")?.toString()?.toIntOrNull() ?: return@mapNotNull null
        val copertina = stagione.optStringOrNull("cover_big") ?: stagione.optStringOrNull("cover")
        copertina?.let { numero to it }
    }.toMap()
    return XtreamSeriesInfoDto(
        seriesName = name,
        episodesBySeason = episodesBySeason,
        cover = cover,
        plot = plot,
        coverPerStagione = coverPerStagione
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
        trama = optStringOrNull("plot")?.takeIf { it.isNotBlank() },
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
