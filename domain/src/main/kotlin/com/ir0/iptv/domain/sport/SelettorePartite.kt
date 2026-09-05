package com.ir0.iptv.domain.sport

import com.ir0.iptv.domain.catalog.ContentCard

private const val QUANTE_IN_EVIDENZA = 2

data class PartitaLive(
    val casa: String,
    val ospite: String,
    val competizione: String?,
    val golCasa: Int?,
    val golOspite: Int?,
    val inCorso: Boolean,
    val inizioMs: Long
)

class SelettorePartite {

    fun inEvidenza(partite: List<PartitaLive>, quante: Int = QUANTE_IN_EVIDENZA): List<PartitaLive> {
        val (inCorso, daGiocare) = partite.partition { it.inCorso }
        return (inCorso.sortedByDescending { it.inizioMs } + daGiocare.sortedBy { it.inizioMs }).take(quante)
    }

    /**
     * Molti provider IPTV creano un Canale per il singolo evento ("Serie A: Inter - Milan"):
     * quando c'e', la partita in evidenza diventa apribile. Altrimenti resta solo il risultato.
     */
    fun canalePer(partita: PartitaLive, canali: List<ContentCard.Canale>): ContentCard.Canale? =
        canali.firstOrNull { canale ->
            val titolo = canale.title.lowercase()
            titolo.contains(partita.casa.lowercase()) && titolo.contains(partita.ospite.lowercase())
        }
}
