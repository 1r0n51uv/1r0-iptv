package com.ir0.iptv.domain.catalog

import com.ir0.iptv.domain.customization.ContentCustomization

/**
 * Stessa idea di RiconciliazioneVisti (playback), per Preferiti/nascosti: una vecchia Chiave che
 * non esiste piu' nel catalogo appena sincronizzato (es. un cambio di formato della Sorgente, da
 * Xtream a M3U dello stesso provider o viceversa - vedi Fase 13) viene riagganciata al contenuto
 * con lo stesso titolo, quando ce n'e' uno solo nel catalogo fresco. Una personalizzazione creata
 * prima che [ContentCustomization] salvasse anche il titolo non ha nulla da abbinare e resta
 * persa: la stessa perdita di sempre, non peggiorata da questo fix.
 */
class RiconciliazionePersonalizzazioni {

    fun riconcilia(
        personalizzazioni: Map<String, ContentCustomization>,
        catalogo: ContentCatalog
    ): Map<String, ContentCustomization> {
        if (personalizzazioni.isEmpty()) return personalizzazioni
        val chiaviCorrenti = catalogo.tutti.mapTo(mutableSetOf()) { it.chiaveIdentita }
        val perTitolo = catalogo.tutti.groupBy { it.title }
        return personalizzazioni.entries.associate { (chiave, personalizzazione) ->
            val chiaveNuova = when {
                chiave in chiaviCorrenti -> chiave
                else -> personalizzazione.titolo
                    ?.let { perTitolo[it]?.singleOrNull() }
                    ?.chiaveIdentita
                    ?: chiave
            }
            chiaveNuova to personalizzazione
        }
    }
}
