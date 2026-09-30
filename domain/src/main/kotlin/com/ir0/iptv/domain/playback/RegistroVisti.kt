package com.ir0.iptv.domain.playback

private const val SOGLIA_COMPLETAMENTO = 0.95
private const val POSIZIONE_MINIMA_MS = 15_000L
private const val DURATA_SIMBOLICA_MS = 1L

const val MAX_VISTI = 200

class RegistroVisti {

    fun registra(visti: List<Visto>, visto: Visto): List<Visto> =
        (listOf(visto) + visti.filterNot { it.chiaveIdentita == visto.chiaveIdentita }).take(MAX_VISTI)

    /** Segna [visto] come completato senza averlo riprodotto fino in fondo. Se c'e' gia' un Visto
     * con una durata vera la posizione va alla fine di quella; altrimenti (mai riprodotto, durata
     * ignota) si usa una durata simbolica, quanto basta perche' risulti completato al 100%. */
    fun segnaComeVisto(visti: List<Visto>, visto: Visto): List<Visto> {
        val durata = visti.firstOrNull { it.chiaveIdentita == visto.chiaveIdentita }
            ?.durataMs?.takeIf { it > 0 }
            ?: DURATA_SIMBOLICA_MS
        return registra(visti, visto.copy(posizioneMs = durata, durataMs = durata))
    }

    fun completato(visto: Visto): Boolean =
        visto.durataMs > 0 && visto.posizioneMs >= visto.durataMs * SOGLIA_COMPLETAMENTO

    fun posizioneDiRipresa(visti: List<Visto>, chiaveIdentita: String): Long? {
        val visto = visti.firstOrNull { it.chiaveIdentita == chiaveIdentita } ?: return null
        if (completato(visto) || visto.posizioneMs < POSIZIONE_MINIMA_MS) return null
        return visto.posizioneMs
    }

    fun percentuale(visti: List<Visto>, chiaveIdentita: String): Int {
        val visto = visti.firstOrNull { it.chiaveIdentita == chiaveIdentita } ?: return 0
        if (visto.durataMs <= 0) return 0
        return (visto.posizioneMs * 100 / visto.durataMs).toInt().coerceIn(0, 100)
    }

    /** Un Film completato non ha piu' nulla da riprendere e sparisce da Continua a guardare; un
     * Episodio completato invece vi resta (mostrera' la sua Serie): chi lo apre da li' arriva a
     * [ProssimaVisioneResolver], che gia' avanza da solo all'Episodio successivo. Filtrarlo qui
     * come un Film farebbe sparire la Serie dalla card di ripresa nell'istante stesso in cui si
     * finisce un Episodio, al posto di mostrare che c'e' un seguito da continuare. */
    fun continuaAGuardare(visti: List<Visto>): List<Visto> =
        visti.filterNot { it.tipo == TipoVisto.FILM && completato(it) }
            .sortedByDescending { it.aggiornatoIl }
            .distinctBy { it.serie ?: it.chiaveIdentita }
}
