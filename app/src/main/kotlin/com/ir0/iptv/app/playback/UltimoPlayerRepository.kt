package com.ir0.iptv.app.playback

import android.content.Context
import com.ir0.iptv.domain.playback.TipoVisto
import java.io.File
import org.json.JSONObject

/**
 * L'ultimo Player aperto quando l'app e' andata in background: permette di riaprire l'app
 * direttamente li', invece che sulla Dashboard. Aggiornato solo quando l'app va in background
 * mentre un Player e' in cima allo stack (vedi PlayerScreen.onVaInBackground); svuotato appena
 * l'utente ne esce (Indietro, cambio di destinazione dalla Sidebar). Da quel Player, Indietro
 * torna alla Dashboard: non si ricostruisce l'intero stack di navigazione che l'aveva aperto.
 */
class UltimoPlayerRepository(context: Context) {
    private val file = File(context.applicationContext.filesDir, "ultimo-player.json")

    @Synchronized
    fun leggi(): Pair<RichiestaRiproduzione, Long>? {
        if (!file.exists()) return null
        return try {
            val json = JSONObject(file.readText())
            json.getJSONObject("richiesta").toRichiesta() to json.getLong("posizioneMs")
        } catch (e: Exception) {
            null
        }
    }

    @Synchronized
    fun salva(richiesta: RichiestaRiproduzione, posizioneMs: Long) {
        val json = JSONObject()
            .put("richiesta", richiesta.toJson())
            .put("posizioneMs", posizioneMs)
        file.writeText(json.toString())
    }

    @Synchronized
    fun pulisci() {
        if (file.exists()) file.delete()
    }
}

private fun RichiestaRiproduzione.toJson(): JSONObject = JSONObject()
    .put("titolo", titolo)
    .put("streamUrl", streamUrl)
    .put("tipo", tipo?.name)
    .put("serie", serie)
    .put("posterUrl", posterUrl)

private fun JSONObject.toRichiesta(): RichiestaRiproduzione = RichiestaRiproduzione(
    titolo = getString("titolo"),
    streamUrl = getString("streamUrl"),
    tipo = optStringOrNull("tipo")?.let { TipoVisto.valueOf(it) },
    serie = optStringOrNull("serie"),
    posterUrl = optStringOrNull("posterUrl")
)

private fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) getString(key) else null
