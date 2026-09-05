package com.ir0.iptv.domain.dashboard

import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.catalog.ElencoPreferiti
import com.ir0.iptv.domain.customization.ContentCustomization
import com.ir0.iptv.domain.playback.RegistroVisti
import com.ir0.iptv.domain.playback.TipoVisto
import com.ir0.iptv.domain.playback.Visto

enum class TipoRiga { CONTINUA, NUOVI_EPISODI, SUGGERITI, SPORT, PREFERITI, CANALI, FILM, SERIE }

data class RigaDashboard(val tipo: TipoRiga, val contenuti: List<ContentCard>)

class CostruttoreDashboard(
    private val registro: RegistroVisti = RegistroVisti(),
    private val elencoPreferiti: ElencoPreferiti = ElencoPreferiti()
) {

    fun costruisci(
        catalogo: ContentCatalog,
        visti: List<Visto>,
        personalizzazioni: Map<String, ContentCustomization>,
        righeExtra: List<RigaDashboard> = emptyList()
    ): List<RigaDashboard> {
        val righe = listOf(
            RigaDashboard(TipoRiga.CONTINUA, continua(catalogo, visti))
        ) + righeExtra + listOf(
            RigaDashboard(TipoRiga.PREFERITI, elencoPreferiti.preferiti(catalogo, personalizzazioni)),
            RigaDashboard(TipoRiga.CANALI, catalogo.canali),
            RigaDashboard(TipoRiga.FILM, catalogo.film),
            RigaDashboard(TipoRiga.SERIE, catalogo.serie)
        )
        return righe.filter { it.contenuti.isNotEmpty() }
    }

    private fun continua(catalogo: ContentCatalog, visti: List<Visto>): List<ContentCard> =
        registro.continuaAGuardare(visti).mapNotNull { visto -> cardDi(catalogo, visto) }

    /** Un Episodio ripreso si mostra come la sua Serie: e' li' che si sceglie da dove ripartire. */
    private fun cardDi(catalogo: ContentCatalog, visto: Visto): ContentCard? = when (visto.tipo) {
        TipoVisto.FILM -> catalogo.film.firstOrNull { it.chiaveIdentita == visto.chiaveIdentita }
        TipoVisto.EPISODIO -> visto.serie?.let { nome ->
            catalogo.serie.firstOrNull { it.chiaveIdentita == ContentCard.chiaveSerie(nome) }
        }
    }
}
