package com.ir0.iptv.app.content

import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.source.xtream.XtreamConnection

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

    sealed interface SerieCard : ContentCard {
        data class Pronta(
            override val title: String,
            override val imageUrl: String?,
            val serie: Serie
        ) : SerieCard

        data class DaCaricare(
            override val title: String,
            override val imageUrl: String?,
            val seriesId: Int,
            val connection: XtreamConnection
        ) : SerieCard
    }
}

data class ContentCatalog(
    val canali: List<ContentCard.Canale> = emptyList(),
    val film: List<ContentCard.Film> = emptyList(),
    val serie: List<ContentCard.SerieCard> = emptyList()
) {
    val isEmpty: Boolean get() = canali.isEmpty() && film.isEmpty() && serie.isEmpty()
}
