package com.ir0.iptv.domain.customization

import com.ir0.iptv.domain.source.m3u.M3uEntry

class IdentityKeyResolver {

    fun resolve(entry: M3uEntry): String = entry.tvgId?.ifBlank { null } ?: entry.url
}
