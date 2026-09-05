package com.ir0.iptv.domain.playback

import com.ir0.iptv.domain.classification.Episodio
import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.classification.Stagione

class NavigazioneSerie(private val resolver: ProssimaVisioneResolver = ProssimaVisioneResolver()) {

    fun stagioneIniziale(serie: Serie, visti: List<Visto>): Stagione? {
        val episodio = when (val prossima = resolver.risolvi(serie, visti)) {
            is ProssimaVisione.Riprendi -> prossima.episodio
            is ProssimaVisione.Inizia -> prossima.episodio
            ProssimaVisione.Completata -> null
        }
        return episodio?.let { stagioneDi(serie, it) } ?: serie.seasons.firstOrNull()
    }

    fun stagioneDi(serie: Serie, episodio: Episodio): Stagione? =
        serie.seasons.firstOrNull { stagione -> stagione.episodes.any { it.url == episodio.url } }

    /**
     * Gli Episodi che vengono dopo quello dato, nell'ordine di visione: le Stagioni si
     * susseguono, quindi dopo l'ultimo Episodio di una Stagione c'e' il primo della successiva.
     * Serve a far partire da soli i prossimi Episodi quando la riproduzione arriva in fondo.
     * Vuota se l'Episodio non appartiene alla Serie o e' l'ultimo.
     */
    fun episodiSuccessivi(serie: Serie, episodioUrl: String): List<Episodio> {
        val episodi = serie.seasons.flatMap { it.episodes }
        val indice = episodi.indexOfFirst { it.url == episodioUrl }
        return if (indice < 0) emptyList() else episodi.drop(indice + 1)
    }
}
