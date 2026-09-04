package com.ir0.iptv.domain.customization

import com.ir0.iptv.domain.source.m3u.M3uEntry

class CustomizationMerger(
    private val identityKeyResolver: IdentityKeyResolver = IdentityKeyResolver()
) {

    fun merge(
        previous: Map<String, ContentCustomization>,
        freshEntries: List<M3uEntry>
    ): List<Pair<M3uEntry, ContentCustomization>> {
        return freshEntries.map { entry ->
            val key = identityKeyResolver.resolve(entry)
            entry to (previous[key] ?: ContentCustomization())
        }
    }
}
