package com.ir0.iptv.app

import com.ir0.iptv.app.playback.RichiestaRiproduzione
import com.ir0.iptv.domain.catalog.ContentCard

/** Schermate che si aprono sopra la destinazione corrente della Sidebar. */
sealed interface Screen {
    data class Detail(val card: ContentCard) : Screen

    /**
     * [coda] sono gli Episodi da riprodurre dopo questo, in ordine: quando la riproduzione
     * arriva in fondo si passa da soli al primo della coda. Vuota per Film, Canali e per gli
     * Episodi aperti senza il contesto della Serie.
     */
    data class Player(
        val richiesta: RichiestaRiproduzione,
        val posizioneIniziale: Long,
        val coda: List<RichiestaRiproduzione> = emptyList()
    ) : Screen
}
