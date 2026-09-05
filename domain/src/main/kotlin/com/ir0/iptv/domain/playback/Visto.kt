package com.ir0.iptv.domain.playback

/** I Canali non compaiono qui: essendo live non hanno una posizione da riprendere (ADR 0004). */
enum class TipoVisto { FILM, EPISODIO }

data class Visto(
    val chiaveIdentita: String,
    val tipo: TipoVisto,
    val titolo: String,
    val streamUrl: String,
    val posizioneMs: Long,
    val durataMs: Long,
    val aggiornatoIl: Long,
    val serie: String? = null,
    val posterUrl: String? = null
)
