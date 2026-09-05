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
}
