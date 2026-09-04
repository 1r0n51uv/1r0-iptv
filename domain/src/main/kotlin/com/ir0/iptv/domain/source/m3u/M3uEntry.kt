package com.ir0.iptv.domain.source.m3u

data class M3uEntry(
    val title: String,
    val url: String,
    val tvgId: String? = null,
    val tvgLogo: String? = null,
    val groupTitle: String? = null
)
