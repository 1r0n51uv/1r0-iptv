package com.ir0.iptv.app

import com.ir0.iptv.app.content.ContentCard
import com.ir0.iptv.app.playback.RichiestaRiproduzione

sealed interface Screen {
    data object Home : Screen
    data class SeriesDetail(val card: ContentCard.SerieCard) : Screen
    data class Player(val richiesta: RichiestaRiproduzione) : Screen
}
