package com.ir0.iptv.domain.catalog

import com.ir0.iptv.domain.customization.ContentCustomization

class ElencoPreferiti {

    fun preferiti(
        catalogo: ContentCatalog,
        personalizzazioni: Map<String, ContentCustomization>
    ): List<ContentCard> = catalogo.tutti.filter { preferito(personalizzazioni, it) }

    fun preferito(personalizzazioni: Map<String, ContentCustomization>, card: ContentCard): Boolean =
        personalizzazioni[card.chiaveIdentita]?.favorite == true

    fun cambiaPreferito(
        personalizzazioni: Map<String, ContentCustomization>,
        card: ContentCard
    ): Map<String, ContentCustomization> {
        val corrente = personalizzazioni[card.chiaveIdentita] ?: ContentCustomization()
        return personalizzazioni + (card.chiaveIdentita to corrente.copy(favorite = !corrente.favorite))
    }
}
