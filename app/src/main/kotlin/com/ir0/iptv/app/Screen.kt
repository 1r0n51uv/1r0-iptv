package com.ir0.iptv.app

import com.ir0.iptv.app.content.ContentCard

sealed interface Screen {
    data object Home : Screen
    data class SeriesDetail(val card: ContentCard.SerieCard) : Screen
    data class Player(val title: String, val streamUrl: String) : Screen
}
