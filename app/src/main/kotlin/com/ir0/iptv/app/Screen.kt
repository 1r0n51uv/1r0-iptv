package com.ir0.iptv.app

import com.ir0.iptv.domain.classification.Serie

sealed interface Screen {
    data object Home : Screen
    data class SeriesDetail(val serie: Serie) : Screen
    data class Player(val title: String, val streamUrl: String) : Screen
}
