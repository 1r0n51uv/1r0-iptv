package com.ir0.iptv.domain.customization

import com.ir0.iptv.domain.classification.ContentType

data class ContentCustomization(
    val hidden: Boolean = false,
    val favorite: Boolean = false,
    val manualType: ContentType? = null,
    /** Il titolo della card al momento di questa personalizzazione: a differenza del Visto (che
     * lo aveva gia'), qui non c'era modo di ritrovare il contenuto giusto se la Chiave di
     * Identita' cambia (es. passando da un account Xtream a un M3U dello stesso provider, vedi
     * Fase 13) - senza un titolo salvato, "Aggiunto ai Preferiti" diventava irrecuperabile. */
    val titolo: String? = null
)
