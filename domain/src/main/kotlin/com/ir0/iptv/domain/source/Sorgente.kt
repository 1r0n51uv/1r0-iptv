package com.ir0.iptv.domain.source

import com.ir0.iptv.domain.source.xtream.XtreamConnection

sealed interface Sorgente {
    val id: String
    val nome: String

    data class M3u(
        override val id: String,
        override val nome: String,
        val url: String
    ) : Sorgente

    data class Xtream(
        override val id: String,
        override val nome: String,
        val connection: XtreamConnection
    ) : Sorgente
}
