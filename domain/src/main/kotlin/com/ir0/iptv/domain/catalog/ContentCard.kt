package com.ir0.iptv.domain.catalog

import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.source.xtream.XtreamConnection

data class RiferimentoXtream(val streamId: Int, val connection: XtreamConnection)

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
        val categoria: String? = null,
        /** Presente solo per i Canali Xtream: e' l'unica Sorgente da cui si legge l'EPG. */
        val xtream: RiferimentoXtream? = null,
        /** `tvg-id` (M3U) / `epg_channel_id` (Xtream): un id assegnato dal provider stesso,
         * indipendente sia dall'URL sia dal formato con cui e' stata configurata la Sorgente -
         * l'unica Chiave che regge anche passando da un account Xtream a un M3U dello stesso
         * provider (vedi Fase 13). */
        val tvgId: String? = null
    ) : ContentCard {
        // In ordine di preferenza: il tvg-id/epg-id del provider (sopravvive a un cambio di
        // formato Sorgente), poi l'id dello stream Xtream (sopravvive a una rotazione di
        // host/credenziali, vedi ChiaveIdentitaXtream), infine l'URL grezzo se non c'e' nient'altro.
        override val chiaveIdentita: String
            get() = tvgId?.takeIf { it.isNotBlank() }
                ?: xtream?.let { chiaveIdentitaXtream("live", it.streamId) }
                ?: streamUrl
    }

    data class Film(
        override val title: String,
        override val imageUrl: String?,
        val streamUrl: String,
        val categoria: String? = null,
        val plot: String? = null,
        /** Presente solo per i Film Xtream: permette di caricare i dettagli estesi (cast,
         * regista, durata...) da get_vod_info. */
        val xtream: RiferimentoXtream? = null,
        /** Vedi [Canale.tvgId]: raro per un Film (niente EPG per i VOD) ma innocuo se il
         * provider lo manda comunque. */
        val tvgId: String? = null
    ) : ContentCard {
        override val chiaveIdentita: String
            get() = tvgId?.takeIf { it.isNotBlank() }
                ?: xtream?.let { chiaveIdentitaXtream("movie", it.streamId) }
                ?: streamUrl
    }

    sealed interface SerieCard : ContentCard {
        override val chiaveIdentita: String get() = chiaveSerie(title)

        /** Le Sorgenti M3U non hanno una categoria distinta dal nome della Serie (ADR 0002):
         * resta null e la Serie cade nel gruppo "Altro" nelle schermate organizzate per categoria. */
        val categoria: String?

        data class Pronta(
            override val title: String,
            override val imageUrl: String?,
            val serie: Serie,
            override val categoria: String? = null
        ) : SerieCard

        data class DaCaricare(
            override val title: String,
            override val imageUrl: String?,
            val seriesId: Int,
            val connection: XtreamConnection,
            val plot: String? = null,
            override val categoria: String? = null
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
