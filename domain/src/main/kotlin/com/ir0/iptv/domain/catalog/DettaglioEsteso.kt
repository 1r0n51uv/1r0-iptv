package com.ir0.iptv.domain.catalog

/**
 * Metadati opzionali di un Film, disponibili solo per le Sorgenti Xtream via get_vod_info
 * (le Sorgenti M3U non li espongono). Nullo quando il provider non li fornisce: la Testata
 * del Dettaglio mostra solo i campi presenti invece di lasciare spazi vuoti.
 */
data class DettaglioEsteso(
    val genere: String? = null,
    val cast: String? = null,
    val regista: String? = null,
    val durata: String? = null,
    val anno: String? = null,
    val valutazione: Double? = null
) {
    val isEmpty: Boolean
        get() = genere == null && cast == null && regista == null &&
            durata == null && anno == null && valutazione == null
}
