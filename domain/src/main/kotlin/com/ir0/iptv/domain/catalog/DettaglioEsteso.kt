package com.ir0.iptv.domain.catalog

/**
 * Metadati opzionali di un Film, disponibili solo per le Sorgenti Xtream via get_vod_info
 * (le Sorgenti M3U non li espongono). Nullo quando il provider non li fornisce: la Testata
 * del Dettaglio mostra solo i campi presenti invece di lasciare spazi vuoti.
 */
data class DettaglioEsteso(
    /** Molti provider non mettono la trama nella lista get_vod_streams (solo qui, in
     * get_vod_info): il Dettaglio la usa quando il catalogo non ne ha gia' una. */
    val trama: String? = null,
    val genere: String? = null,
    val cast: String? = null,
    val regista: String? = null,
    val durata: String? = null,
    val anno: String? = null,
    val valutazione: Double? = null
) {
    /** Non conta trama: quella si usa a parte come ripiego per il campo plot della Testata,
     * indipendentemente dal pannello cast/regista/genere. */
    val isEmpty: Boolean
        get() = genere == null && cast == null && regista == null &&
            durata == null && anno == null && valutazione == null
}
