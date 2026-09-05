package com.ir0.iptv.domain.epg

data class Programma(
    val titolo: String,
    val descrizione: String? = null,
    val inizioMs: Long,
    val fineMs: Long
)
