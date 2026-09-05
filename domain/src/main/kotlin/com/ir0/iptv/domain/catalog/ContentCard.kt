package com.ir0.iptv.domain.catalog

import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.source.xtream.XtreamConnection

sealed interface ContentCard {
    val title: String
    val imageUrl: String?

    /**
     * Chiave di Identità della card. Canali e Film usano l'URL dello stream; una Serie non ha
     * un URL proprio, quindi usa il nome, che resta lo stesso quando una Serie Xtream passa da
     * [SerieCard.DaCaricare] a [SerieCard.Pronta].
     */
    val chiaveIdentita: String

    data class Canale(
        override val title: String,
        override val imageUrl: String?,
        val streamUrl: String,
        val categoria: String? = null
    ) : ContentCard {
        override val chiaveIdentita: String get() = streamUrl
    }

    data class Film(
        override val title: String,
        override val imageUrl: String?,
        val streamUrl: String,
        val categoria: String? = null,
        val plot: String? = null
    ) : ContentCard {
        override val chiaveIdentita: String get() = streamUrl
    }

    sealed interface SerieCard : ContentCard {
        override val chiaveIdentita: String get() = chiaveSerie(title)

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

    companion object {
        fun chiaveSerie(nome: String): String = "serie:$nome"
    }
}

data class ContentCatalog(
    val canali: List<ContentCard.Canale> = emptyList(),
    val film: List<ContentCard.Film> = emptyList(),
    val serie: List<ContentCard.SerieCard> = emptyList()
) {
    val isEmpty: Boolean get() = canali.isEmpty() && film.isEmpty() && serie.isEmpty()

    val tutti: List<ContentCard> get() = canali + film + serie
}
