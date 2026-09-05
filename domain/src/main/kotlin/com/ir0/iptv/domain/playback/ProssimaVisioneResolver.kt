package com.ir0.iptv.domain.playback

import com.ir0.iptv.domain.classification.Episodio
import com.ir0.iptv.domain.classification.Serie

sealed interface ProssimaVisione {
    data class Riprendi(val episodio: Episodio, val posizioneMs: Long) : ProssimaVisione
    data class Inizia(val episodio: Episodio) : ProssimaVisione
    data object Completata : ProssimaVisione
}

class ProssimaVisioneResolver(private val registro: RegistroVisti = RegistroVisti()) {

    fun risolvi(serie: Serie, visti: List<Visto>): ProssimaVisione {
        val episodi = serie.seasons.flatMap { it.episodes }
        if (episodi.isEmpty()) return ProssimaVisione.Completata

        val perChiave = visti.associateBy { it.chiaveIdentita }
        val ultimo = episodi.withIndex()
            .mapNotNull { (indice, episodio) -> perChiave[episodio.url]?.let { indice to it } }
            .maxWithOrNull(compareBy({ it.second.aggiornatoIl }, { it.first }))
            ?: return ProssimaVisione.Inizia(episodi.first())

        val (indice, visto) = ultimo
        if (registro.completato(visto)) {
            val successivo = episodi.getOrNull(indice + 1) ?: return ProssimaVisione.Completata
            return ProssimaVisione.Inizia(successivo)
        }
        val posizione = registro.posizioneDiRipresa(visti, visto.chiaveIdentita)
            ?: return ProssimaVisione.Inizia(episodi[indice])
        return ProssimaVisione.Riprendi(episodi[indice], posizione)
    }
}
