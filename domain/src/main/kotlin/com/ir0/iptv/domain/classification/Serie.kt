package com.ir0.iptv.domain.classification

data class Serie(
    val name: String,
    val seasons: List<Stagione>,
    val poster: String? = null,
    val plot: String? = null
)

data class Stagione(
    val number: Int?,
    val episodes: List<Episodio>
)

data class Episodio(
    val title: String,
    val url: String,
    val episodeNumber: Int?,
    val immagine: String? = null
)
