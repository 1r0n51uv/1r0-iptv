package com.ir0.iptv.app

import com.ir0.iptv.app.playback.RichiestaRiproduzione
import com.ir0.iptv.domain.catalog.ContentCard

/** Schermate che si aprono sopra la destinazione corrente della Sidebar. */
sealed interface Screen {
    data class Detail(val card: ContentCard) : Screen
    data class Player(val richiesta: RichiestaRiproduzione, val posizioneIniziale: Long) : Screen
}
