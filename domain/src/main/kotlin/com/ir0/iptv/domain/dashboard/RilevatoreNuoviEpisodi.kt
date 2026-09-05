package com.ir0.iptv.domain.dashboard

import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.customization.ContentCustomization
import com.ir0.iptv.domain.playback.TipoVisto
import com.ir0.iptv.domain.playback.Visto

class RilevatoreNuoviEpisodi {

    /**
     * Solo le Serie seguite entrano nel confronto: su un catalogo M3U da migliaia di voci,
     * segnalare i nuovi episodi di Serie mai aperte sarebbe rumore (ADR 0006).
     */
    fun serieSeguite(
        catalogo: ContentCatalog,
        visti: List<Visto>,
        personalizzazioni: Map<String, ContentCustomization>
    ): List<ContentCard.SerieCard> {
        val serieViste = visti.filter { it.tipo == TipoVisto.EPISODIO }
            .mapNotNull { it.serie }
            .map { ContentCard.chiaveSerie(it) }
            .toSet()
        return catalogo.serie.filter { serie ->
            personalizzazioni[serie.chiaveIdentita]?.favorite == true ||
                serie.chiaveIdentita in serieViste
        }
    }

    /**
     * Una Serie mai vista prima non ha episodi "nuovi": al primo avvio l'intero catalogo
     * sarebbe nuovo, e la riga diventerebbe inutile.
     */
    fun nuovi(
        attuali: Map<String, Set<String>>,
        precedenti: Map<String, Set<String>>
    ): Map<String, Set<String>> = attuali.mapNotNull { (chiaveSerie, episodi) ->
        val prima = precedenti[chiaveSerie] ?: return@mapNotNull null
        (episodi - prima).takeIf { it.isNotEmpty() }?.let { chiaveSerie to it }
    }.toMap()

    fun riga(
        serie: List<ContentCard.SerieCard>,
        nuovi: Map<String, Set<String>>
    ): RigaDashboard? {
        val conNovita = serie.filter { it.chiaveIdentita in nuovi.keys }
            .map { it.conImmagineDiEpisodio(nuovi[it.chiaveIdentita].orEmpty()) }
        return conNovita.takeIf { it.isNotEmpty() }?.let { RigaDashboard(TipoRiga.NUOVI_EPISODI, it) }
    }
}
