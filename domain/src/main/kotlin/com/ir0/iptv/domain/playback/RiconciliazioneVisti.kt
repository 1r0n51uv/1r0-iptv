package com.ir0.iptv.domain.playback

import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog

/**
 * Un Visto la cui Chiave di Identita' non esiste piu' nel catalogo appena sincronizzato (es. la
 * Sorgente e' passata da un account Xtream a un M3U dello stesso provider, o viceversa: formati
 * diversi, Chiave diversa anche per lo stesso identico contenuto - vedi Fase 13) sparirebbe da
 * "Continua a guardare" e perderebbe la posizione di ripresa. Qui si cerca un sostituto nel
 * catalogo fresco per titolo (lo stesso Film, o lo stesso Episodio dentro la stessa Serie): solo
 * se e' l'unico con quel titolo, per non rischiare di agganciare un Visto al contenuto sbagliato
 * quando il titolo non basta a distinguerlo.
 */
class RiconciliazioneVisti {

    fun riconcilia(visti: List<Visto>, catalogo: ContentCatalog): List<Visto> =
        visti.map { riconciliaUno(it, catalogo) }

    private fun riconciliaUno(visto: Visto, catalogo: ContentCatalog): Visto = when (visto.tipo) {
        TipoVisto.FILM -> riconciliaFilm(visto, catalogo)
        TipoVisto.EPISODIO -> riconciliaEpisodio(visto, catalogo)
    }

    private fun riconciliaFilm(visto: Visto, catalogo: ContentCatalog): Visto {
        if (catalogo.film.any { it.chiaveIdentita == visto.chiaveIdentita }) return visto
        val sostituto = catalogo.film.singleOrNull { it.title == visto.titolo } ?: return visto
        return visto.copy(chiaveIdentita = sostituto.chiaveIdentita, streamUrl = sostituto.streamUrl)
    }

    private fun riconciliaEpisodio(visto: Visto, catalogo: ContentCatalog): Visto {
        val nomeSerie = visto.serie ?: return visto
        // Solo le Serie gia' caricate (Pronta) hanno gli Episodi in memoria per un confronto.
        val serieCard = catalogo.serie.filterIsInstance<ContentCard.SerieCard.Pronta>()
            .singleOrNull { it.title == nomeSerie } ?: return visto
        val episodi = serieCard.serie.seasons.flatMap { it.episodes }
        if (episodi.any { it.chiaveIdentita == visto.chiaveIdentita }) return visto
        val sostituto = episodi.singleOrNull { it.title == visto.titolo } ?: return visto
        return visto.copy(chiaveIdentita = sostituto.chiaveIdentita, streamUrl = sostituto.url)
    }
}
