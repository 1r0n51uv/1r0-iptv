package com.ir0.iptv.app

import com.ir0.iptv.app.playback.RichiestaRiproduzione
import com.ir0.iptv.domain.catalog.ContentCard

sealed interface Screen {
    data object Home : Screen
    data class Detail(val card: ContentCard) : Screen
    data class Player(val richiesta: RichiestaRiproduzione, val posizioneIniziale: Long) : Screen
}
