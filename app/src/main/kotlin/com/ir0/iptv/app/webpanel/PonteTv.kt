package com.ir0.iptv.app.webpanel

import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Il Pannello Web gira nello stesso processo dell'app TV (ADR 0001), quindi "riprodurre sulla
 * TV" non ha bisogno di un protocollo di cast: il browser chiama il server, il server posa qui
 * il contenuto scelto e la UI lo apre (ADR 0005).
 */
object PonteTv {

    private val _catalogo = MutableStateFlow(ContentCatalog())
    val catalogo: StateFlow<ContentCatalog> = _catalogo.asStateFlow()

    private val _daAprire = MutableStateFlow<ContentCard?>(null)
    val daAprire: StateFlow<ContentCard?> = _daAprire.asStateFlow()

    fun pubblicaCatalogo(catalogo: ContentCatalog) {
        _catalogo.value = catalogo
    }

    /** False quando la Chiave di Identità non è (più) nel catalogo caricato sulla TV. */
    fun chiediApertura(chiaveIdentita: String): Boolean {
        val card = _catalogo.value.tutti.firstOrNull { it.chiaveIdentita == chiaveIdentita } ?: return false
        _daAprire.value = card
        return true
    }

    fun aperturaConsumata() {
        _daAprire.value = null
    }
}
