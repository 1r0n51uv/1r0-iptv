package com.ir0.iptv.app.playback

import com.ir0.iptv.domain.playback.TipoVisto

/** Un [tipo] nullo indica un Canale: live, quindi non genera un Visto (ADR 0004). */
data class RichiestaRiproduzione(
    val titolo: String,
    val streamUrl: String,
    val tipo: TipoVisto? = null,
    val serie: String? = null,
    val posterUrl: String? = null
) {
    /** Film ed Episodi non espongono un tvg-id: la Chiave di Identità è l'URL dello stream. */
    val chiaveIdentita: String get() = streamUrl
}
