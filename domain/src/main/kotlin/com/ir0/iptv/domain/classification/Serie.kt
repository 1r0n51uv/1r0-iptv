package com.ir0.iptv.domain.classification

import com.ir0.iptv.domain.catalog.chiaveIdentitaXtream

data class Serie(
    val name: String,
    val seasons: List<Stagione>,
    val poster: String? = null,
    val plot: String? = null
)

data class Stagione(
    val number: Int?,
    val episodes: List<Episodio>,
    /** La copertina della Stagione, quando la Sorgente la espone (le Sorgenti Xtream nel blocco
     * `seasons`); null per le Sorgenti M3U. */
    val immagine: String? = null
)

data class Episodio(
    val title: String,
    val url: String,
    val episodeNumber: Int?,
    val immagine: String? = null,
    /** Presente solo per gli Episodi Xtream: l'id numerico dello stream, stabile anche se il
     * provider ruota host/credenziali (vedi ChiaveIdentitaXtream) - l'URL, che le incorpora, no. */
    val xtreamId: Int? = null
) {
    val chiaveIdentita: String get() = xtreamId?.let { chiaveIdentitaXtream("series", it) } ?: url
}
