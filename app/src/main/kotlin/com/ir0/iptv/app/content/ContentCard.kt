package com.ir0.iptv.app.content

import com.ir0.iptv.domain.classification.Serie

sealed interface ContentCard {
    val title: String
    val imageUrl: String?

    data class Canale(
        override val title: String,
        override val imageUrl: String?,
        val streamUrl: String
    ) : ContentCard

    data class Film(
        override val title: String,
        override val imageUrl: String?,
        val streamUrl: String
    ) : ContentCard

    data class SerieCard(
        override val title: String,
        override val imageUrl: String?,
        val serie: Serie
    ) : ContentCard
}

data class ContentCatalog(
    val canali: List<ContentCard.Canale> = emptyList(),
    val film: List<ContentCard.Film> = emptyList(),
    val serie: List<ContentCard.SerieCard> = emptyList()
) {
    val isEmpty: Boolean get() = canali.isEmpty() && film.isEmpty() && serie.isEmpty()
}
