package com.ir0.iptv.domain.source.m3u

class M3uParser {

    fun parse(content: String): List<M3uEntry> = parse(content.lineSequence())

    /** Stessa logica della versione a `String`, ma su una `Sequence` cosi' chi la chiama puo'
     * darla in pasto riga per riga da uno stream di rete invece di dover prima materializzare
     * l'intera playlist (spesso decine o centinaia di MB per un provider reale) in memoria. */
    fun parse(righe: Sequence<String>): List<M3uEntry> {
        val entries = mutableListOf<M3uEntry>()
        var extinfInSospeso: String? = null
        for (rigaGrezza in righe) {
            val riga = rigaGrezza.trim()
            val extinf = extinfInSospeso
            if (riga.startsWith("#EXTINF")) {
                extinfInSospeso = riga
            } else if (extinf != null && riga.isNotEmpty()) {
                val commaIndex = extinf.lastIndexOf(',')
                val attributes = extinf.substring(0, commaIndex)
                val title = extinf.substring(commaIndex + 1).trim()
                entries += M3uEntry(
                    title = title,
                    url = riga,
                    tvgId = REGEX_TVG_ID.find(attributes)?.groupValues?.get(1),
                    tvgLogo = REGEX_TVG_LOGO.find(attributes)?.groupValues?.get(1),
                    groupTitle = REGEX_GROUP_TITLE.find(attributes)?.groupValues?.get(1)
                )
                extinfInSospeso = null
            }
        }
        return entries
    }

    companion object {
        // Precompilate una volta sola: costruire un nuovo Regex per ogni attributo di ogni riga
        // (come si faceva prima) significa ricompilarlo decine o centinaia di migliaia di volte
        // per una playlist reale di un provider con panel - confermato sul dispositivo, un
        // parsing che doveva durare pochi secondi ne impiegava diversi minuti.
        private val REGEX_TVG_ID = Regex("""tvg-id="([^"]*)"""")
        private val REGEX_TVG_LOGO = Regex("""tvg-logo="([^"]*)"""")
        private val REGEX_GROUP_TITLE = Regex("""group-title="([^"]*)"""")
    }
}
