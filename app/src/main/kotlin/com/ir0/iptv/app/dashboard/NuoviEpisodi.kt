package com.ir0.iptv.app.dashboard

import com.ir0.iptv.app.content.ContentFetcher
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.customization.ContentCustomization
import com.ir0.iptv.domain.dashboard.RigaDashboard
import com.ir0.iptv.domain.dashboard.RilevatoreNuoviEpisodi
import com.ir0.iptv.domain.playback.Visto

/** Le Serie Xtream vanno interrogate una per una: un tetto evita di martellare il provider. */
private const val MAX_SERIE_CONTROLLATE = 30

class NuoviEpisodi(
    private val repository: NuoviEpisodiRepository,
    private val fetcher: ContentFetcher = ContentFetcher(),
    private val rilevatore: RilevatoreNuoviEpisodi = RilevatoreNuoviEpisodi()
) {

    suspend fun riga(
        catalogo: ContentCatalog,
        visti: List<Visto>,
        personalizzazioni: Map<String, ContentCustomization>
    ): RigaDashboard? {
        val seguite = rilevatore.serieSeguite(catalogo, visti, personalizzazioni).take(MAX_SERIE_CONTROLLATE)
        if (seguite.isEmpty()) return null

        val attuali = seguite.mapNotNull { card ->
            episodiDi(card)?.let { card.chiaveIdentita to it }
        }.toMap()

        val precedenti = repository.leggi()
        val nuovi = rilevatore.nuovi(attuali, precedenti)
        repository.salva(precedenti + attuali)
        return rilevatore.riga(seguite, nuovi)
    }

    /** Null quando gli Episodi non si riescono a leggere: registrarli come vuoti li farebbe
     * sembrare tutti nuovi al giro successivo. */
    private suspend fun episodiDi(card: ContentCard.SerieCard): Set<String>? = when (card) {
        is ContentCard.SerieCard.Pronta -> card.serie.seasons.flatMap { it.episodes }.map { it.url }.toSet()
        is ContentCard.SerieCard.DaCaricare ->
            fetcher.dettaglioSerie(card)?.seasons?.flatMap { it.episodes }?.map { it.url }?.toSet()
    }
}
