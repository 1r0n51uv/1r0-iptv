package com.ir0.iptv.domain.catalog

class RicercaCatalogo {

    fun cerca(catalogo: ContentCatalog, query: String): List<ContentCard> {
        val termine = query.trim()
        if (termine.isEmpty()) return emptyList()
        return catalogo.tutti.filter { it.title.contains(termine, ignoreCase = true) }
    }
}
