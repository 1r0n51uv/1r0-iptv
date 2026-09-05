package com.ir0.iptv.app.dashboard

import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.customization.ContentCustomization
import com.ir0.iptv.domain.dashboard.RigaDashboard
import com.ir0.iptv.domain.dashboard.Suggerimenti
import com.ir0.iptv.domain.playback.Visto

class SuggerimentiAi(
    private val client: ClientAi = ClientAi(),
    private val suggerimenti: Suggerimenti = Suggerimenti()
) {

    suspend fun riga(
        chiaveApi: String?,
        catalogo: ContentCatalog,
        visti: List<Visto>,
        personalizzazioni: Map<String, ContentCustomization>
    ): RigaDashboard? {
        if (chiaveApi.isNullOrBlank()) return null
        val prompt = suggerimenti.prompt(catalogo, visti, personalizzazioni) ?: return null
        val risposta = client.rispondi(chiaveApi, prompt) ?: return null
        return suggerimenti.riga(catalogo, suggerimenti.titoliDaRisposta(risposta))
    }
}
