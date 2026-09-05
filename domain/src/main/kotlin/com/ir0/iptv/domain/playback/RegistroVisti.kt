package com.ir0.iptv.domain.playback

private const val SOGLIA_COMPLETAMENTO = 0.95
private const val POSIZIONE_MINIMA_MS = 15_000L

class RegistroVisti {

    fun registra(visti: List<Visto>, visto: Visto): List<Visto> =
        listOf(visto) + visti.filterNot { it.chiaveIdentita == visto.chiaveIdentita }

    fun completato(visto: Visto): Boolean =
        visto.durataMs > 0 && visto.posizioneMs >= visto.durataMs * SOGLIA_COMPLETAMENTO

    fun posizioneDiRipresa(visti: List<Visto>, chiaveIdentita: String): Long? {
        val visto = visti.firstOrNull { it.chiaveIdentita == chiaveIdentita } ?: return null
        if (completato(visto) || visto.posizioneMs < POSIZIONE_MINIMA_MS) return null
        return visto.posizioneMs
    }

    fun continuaAGuardare(visti: List<Visto>): List<Visto> =
        visti.filterNot { completato(it) }
            .sortedByDescending { it.aggiornatoIl }
            .distinctBy { it.serie ?: it.chiaveIdentita }
}
