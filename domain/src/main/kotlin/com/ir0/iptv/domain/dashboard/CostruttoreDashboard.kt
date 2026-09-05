package com.ir0.iptv.domain.dashboard

import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.catalog.ElencoPreferiti
import com.ir0.iptv.domain.customization.ContentCustomization
import com.ir0.iptv.domain.playback.RegistroVisti
import com.ir0.iptv.domain.playback.TipoVisto
import com.ir0.iptv.domain.playback.Visto

enum class TipoRiga { CONTINUA, NUOVI_EPISODI, SUGGERITI, PREFERITI }

data class RigaDashboard(val tipo: TipoRiga, val contenuti: List<ContentCard>)

/**
 * Le righe curate della Dashboard: sempre le stesse quattro, nello stesso ordine, anche quando
 * sono vuote (l'utente deve capire cosa la Dashboard puo' mostrare, non solo cosa mostra ora).
 * Canali/Film/Serie hanno le loro schermate dedicate raggiungibili dalla Sidebar, quindi non
 * compaiono piu' qui come righe di ripiego.
 */
class CostruttoreDashboard(
    private val registro: RegistroVisti = RegistroVisti(),
    private val elencoPreferiti: ElencoPreferiti = ElencoPreferiti()
) {

    fun costruisci(
        catalogo: ContentCatalog,
        visti: List<Visto>,
        personalizzazioni: Map<String, ContentCustomization>,
        righeExtra: List<RigaDashboard> = emptyList()
    ): List<RigaDashboard> = listOf(
        RigaDashboard(TipoRiga.CONTINUA, continua(catalogo, visti))
    ) + righeExtra + listOf(
        RigaDashboard(TipoRiga.PREFERITI, elencoPreferiti.preferiti(catalogo, personalizzazioni))
    )

    private fun continua(catalogo: ContentCatalog, visti: List<Visto>): List<ContentCard> =
        registro.continuaAGuardare(visti).mapNotNull { visto -> cardDi(catalogo, visto) }

    /** Un Episodio ripreso si mostra come la sua Serie: e' li' che si sceglie da dove ripartire.
     * La card pero' porta l'immagine dell'Episodio ripreso quando c'e', non la locandina. */
    private fun cardDi(catalogo: ContentCatalog, visto: Visto): ContentCard? = when (visto.tipo) {
        TipoVisto.FILM -> catalogo.film.firstOrNull { it.chiaveIdentita == visto.chiaveIdentita }
        TipoVisto.EPISODIO -> visto.serie?.let { nome ->
            catalogo.serie.firstOrNull { it.chiaveIdentita == ContentCard.chiaveSerie(nome) }
                ?.conImmagineDiEpisodio(listOf(visto.chiaveIdentita))
        }
    }
}
