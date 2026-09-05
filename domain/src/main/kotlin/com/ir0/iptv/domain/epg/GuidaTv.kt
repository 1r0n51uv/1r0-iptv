package com.ir0.iptv.domain.epg

class GuidaTv {

    fun inOnda(palinsesto: List<Programma>, ora: Long): Programma? =
        palinsesto.firstOrNull { ora >= it.inizioMs && ora < it.fineMs }

    fun prossimi(palinsesto: List<Programma>, ora: Long): List<Programma> =
        palinsesto.filter { it.inizioMs > ora }.sortedBy { it.inizioMs }

    fun percentuale(programma: Programma, ora: Long): Int {
        val durata = programma.fineMs - programma.inizioMs
        if (durata <= 0) return 0
        return ((ora - programma.inizioMs) * 100 / durata).toInt().coerceIn(0, 100)
    }
}
