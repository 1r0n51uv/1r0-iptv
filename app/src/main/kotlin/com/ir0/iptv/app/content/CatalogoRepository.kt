package com.ir0.iptv.app.content

import android.content.Context
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.catalog.RiferimentoXtream
import com.ir0.iptv.domain.classification.Episodio
import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.classification.Stagione
import com.ir0.iptv.domain.source.xtream.XtreamConnection
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

/**
 * Copia locale su disco dell'ultimo catalogo scaricato dalle Sorgenti (ADR 0008): letta subito
 * all'avvio invece di aspettare la rete, poi sovrascritta ad ogni "Aggiorna catalogo" (automatico
 * all'avvio, o manuale dalla Sidebar). Solo i metadati testuali di Canali/Film/Serie: niente
 * immagini (gia' in cache su disco via Coil) ne' i file audio/video degli stream.
 */
class CatalogoRepository(context: Context) {
    private val file = File(context.applicationContext.filesDir, "catalogo.json")

    @Synchronized
    fun leggi(): ContentCatalog? {
        if (!file.exists()) return null
        return try {
            JSONObject(file.readText()).toContentCatalog()
        } catch (e: Exception) {
            null
        }
    }

    @Synchronized
    fun salva(catalogo: ContentCatalog) {
        file.writeText(catalogo.toJson().toString())
    }
}

private fun ContentCatalog.toJson(): JSONObject = JSONObject()
    .put("canali", JSONArray(canali.map { it.toJson() }))
    .put("film", JSONArray(film.map { it.toJson() }))
    .put("serie", JSONArray(serie.map { it.toJson() }))

private fun JSONObject.toContentCatalog(): ContentCatalog = ContentCatalog(
    canali = getJSONArray("canali").objects().map { it.toCanale() },
    film = getJSONArray("film").objects().map { it.toFilm() },
    serie = getJSONArray("serie").objects().map { it.toSerieCard() }
)

private fun ContentCard.Canale.toJson(): JSONObject = JSONObject()
    .put("title", title)
    .put("imageUrl", imageUrl)
    .put("streamUrl", streamUrl)
    .put("categoria", categoria)
    .put("xtream", xtream?.toJson())

private fun JSONObject.toCanale(): ContentCard.Canale = ContentCard.Canale(
    title = getString("title"),
    imageUrl = optStringOrNull("imageUrl"),
    streamUrl = getString("streamUrl"),
    categoria = optStringOrNull("categoria"),
    xtream = optJSONObject("xtream")?.toRiferimentoXtream()
)

private fun ContentCard.Film.toJson(): JSONObject = JSONObject()
    .put("title", title)
    .put("imageUrl", imageUrl)
    .put("streamUrl", streamUrl)
    .put("categoria", categoria)
    .put("plot", plot)
    .put("xtream", xtream?.toJson())

private fun JSONObject.toFilm(): ContentCard.Film = ContentCard.Film(
    title = getString("title"),
    imageUrl = optStringOrNull("imageUrl"),
    streamUrl = getString("streamUrl"),
    categoria = optStringOrNull("categoria"),
    plot = optStringOrNull("plot"),
    xtream = optJSONObject("xtream")?.toRiferimentoXtream()
)

private fun ContentCard.SerieCard.toJson(): JSONObject = when (this) {
    is ContentCard.SerieCard.Pronta -> JSONObject()
        .put("tipo", "pronta")
        .put("title", title)
        .put("imageUrl", imageUrl)
        .put("categoria", categoria)
        .put("serie", serie.toJson())

    is ContentCard.SerieCard.DaCaricare -> JSONObject()
        .put("tipo", "daCaricare")
        .put("title", title)
        .put("imageUrl", imageUrl)
        .put("categoria", categoria)
        .put("plot", plot)
        .put("seriesId", seriesId)
        .put("connection", connection.toJson())
}

private fun JSONObject.toSerieCard(): ContentCard.SerieCard = when (getString("tipo")) {
    "pronta" -> ContentCard.SerieCard.Pronta(
        title = getString("title"),
        imageUrl = optStringOrNull("imageUrl"),
        serie = getJSONObject("serie").toSerie(),
        categoria = optStringOrNull("categoria")
    )

    else -> ContentCard.SerieCard.DaCaricare(
        title = getString("title"),
        imageUrl = optStringOrNull("imageUrl"),
        seriesId = getInt("seriesId"),
        connection = getJSONObject("connection").toXtreamConnection(),
        plot = optStringOrNull("plot"),
        categoria = optStringOrNull("categoria")
    )
}

private fun Serie.toJson(): JSONObject = JSONObject()
    .put("name", name)
    .put("poster", poster)
    .put("plot", plot)
    .put("seasons", JSONArray(seasons.map { it.toJson() }))

private fun JSONObject.toSerie(): Serie = Serie(
    name = getString("name"),
    poster = optStringOrNull("poster"),
    plot = optStringOrNull("plot"),
    seasons = getJSONArray("seasons").objects().map { it.toStagione() }
)

private fun Stagione.toJson(): JSONObject = JSONObject()
    .put("number", number)
    .put("immagine", immagine)
    .put("episodes", JSONArray(episodes.map { it.toJson() }))

private fun JSONObject.toStagione(): Stagione = Stagione(
    number = optIntOrNull("number"),
    immagine = optStringOrNull("immagine"),
    episodes = getJSONArray("episodes").objects().map { it.toEpisodio() }
)

private fun Episodio.toJson(): JSONObject = JSONObject()
    .put("title", title)
    .put("url", url)
    .put("episodeNumber", episodeNumber)
    .put("immagine", immagine)

private fun JSONObject.toEpisodio(): Episodio = Episodio(
    title = getString("title"),
    url = getString("url"),
    episodeNumber = optIntOrNull("episodeNumber"),
    immagine = optStringOrNull("immagine")
)

private fun RiferimentoXtream.toJson(): JSONObject = JSONObject()
    .put("streamId", streamId)
    .put("connection", connection.toJson())

private fun JSONObject.toRiferimentoXtream(): RiferimentoXtream = RiferimentoXtream(
    streamId = getInt("streamId"),
    connection = getJSONObject("connection").toXtreamConnection()
)

private fun XtreamConnection.toJson(): JSONObject = JSONObject()
    .put("host", host)
    .put("port", port)
    .put("username", username)
    .put("password", password)

private fun JSONObject.toXtreamConnection(): XtreamConnection = XtreamConnection(
    host = getString("host"),
    port = getInt("port"),
    username = getString("username"),
    password = getString("password")
)

private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }

private fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) getString(key) else null

private fun JSONObject.optIntOrNull(key: String): Int? =
    if (has(key) && !isNull(key)) getInt(key) else null
