package com.ir0.iptv.app.sport

import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.sport.PartitaLive
import com.ir0.iptv.domain.sport.SelettorePartite

data class PartitaConCanale(val partita: PartitaLive, val canale: ContentCard.Canale?)

class SportInEvidenza(
    private val client: ClientSport = ClientSport(),
    private val selettore: SelettorePartite = SelettorePartite()
) {

    suspend fun partite(
        attivo: Boolean,
        chiaveApi: String?,
        canali: List<ContentCard.Canale>
    ): List<PartitaConCanale> {
        if (!attivo || chiaveApi.isNullOrBlank()) return emptyList()
        return selettore.inEvidenza(client.partiteDiOggi(chiaveApi))
            .map { PartitaConCanale(it, selettore.canalePer(it, canali)) }
    }
}
