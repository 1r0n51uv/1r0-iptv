package com.ir0.iptv.app.playback

import android.content.Context
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.playback.RegistroVisti
import com.ir0.iptv.domain.playback.TipoVisto
import com.ir0.iptv.domain.playback.Visto
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

class VistoRepository(
    context: Context,
    private val registro: RegistroVisti = RegistroVisti(),
    private val orologio: () -> Long = System::currentTimeMillis
) {
    private val file = File(context.applicationContext.filesDir, "visti.json")

    @Synchronized
    fun elenco(): List<Visto> {
        if (!file.exists()) return emptyList()
        // Il file viene riscritto durante la riproduzione: se l'app muore a metà scrittura
        // resta troncato, e senza questa rete l'app non ripartirebbe più.
        return try {
            val array = JSONArray(file.readText())
            (0 until array.length()).map { array.getJSONObject(it).toVisto() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun posizioneDiRipresa(chiaveIdentita: String): Long? =
        registro.posizioneDiRipresa(elenco(), chiaveIdentita)

    @Synchronized
    fun continuaAGuardare(): List<Visto> = registro.continuaAGuardare(elenco())

    /** Toglie un contenuto dai Visti (quindi da "Continua a guardare"): per una Serie elimina i
     * Visti di tutti i suoi Episodi, per Film/Canale il Visto con quella Chiave di Identita'. */
    @Synchronized
    fun rimuoviDaiVisti(card: ContentCard) {
        val restanti = when (card) {
            is ContentCard.SerieCard -> elenco().filterNot { it.serie == card.title }
            else -> elenco().filterNot { it.chiaveIdentita == card.chiaveIdentita }
        }
        file.writeText(JSONArray(restanti.map { it.toJson() }).toString())
    }

    @Synchronized
    fun registraProgresso(richiesta: RichiestaRiproduzione, posizioneMs: Long, durataMs: Long) {
        val tipo = richiesta.tipo ?: return
        val visto = Visto(
            chiaveIdentita = richiesta.chiaveIdentita,
            tipo = tipo,
            titolo = richiesta.titolo,
            streamUrl = richiesta.streamUrl,
            posizioneMs = posizioneMs,
            durataMs = durataMs,
            aggiornatoIl = orologio(),
            serie = richiesta.serie,
            posterUrl = richiesta.posterUrl
        )
        file.writeText(JSONArray(registro.registra(elenco(), visto).map { it.toJson() }).toString())
    }
}

private fun Visto.toJson(): JSONObject = JSONObject()
    .put("chiaveIdentita", chiaveIdentita)
    .put("tipo", tipo.name)
    .put("titolo", titolo)
    .put("streamUrl", streamUrl)
    .put("posizioneMs", posizioneMs)
    .put("durataMs", durataMs)
    .put("aggiornatoIl", aggiornatoIl)
    .put("serie", serie)
    .put("posterUrl", posterUrl)

private fun JSONObject.toVisto(): Visto = Visto(
    chiaveIdentita = getString("chiaveIdentita"),
    tipo = TipoVisto.valueOf(getString("tipo")),
    titolo = getString("titolo"),
    streamUrl = getString("streamUrl"),
    posizioneMs = getLong("posizioneMs"),
    durataMs = getLong("durataMs"),
    aggiornatoIl = getLong("aggiornatoIl"),
    serie = optStringOrNull("serie"),
    posterUrl = optStringOrNull("posterUrl")
)

private fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) getString(key) else null
