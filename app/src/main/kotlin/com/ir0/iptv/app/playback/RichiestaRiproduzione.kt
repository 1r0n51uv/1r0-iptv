package com.ir0.iptv.app.playback

import com.ir0.iptv.domain.playback.TipoVisto

/** Un [tipo] nullo indica un Canale: live, quindi non genera un Visto (ADR 0004). */
data class RichiestaRiproduzione(
    val titolo: String,
    val streamUrl: String,
    val tipo: TipoVisto? = null,
    val serie: String? = null,
    val posterUrl: String? = null,
    /** Di norma quella della card/Episodio di partenza (stabile anche se il provider Xtream
     * ruota credenziali, vedi ChiaveIdentitaXtream nel dominio); ripiega sull'URL dello stream
     * solo se il chiamante non ne conosce una migliore. */
    val chiaveIdentita: String = streamUrl
)
