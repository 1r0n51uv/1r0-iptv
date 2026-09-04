package com.ir0.iptv.domain.customization

import com.ir0.iptv.domain.source.m3u.M3uEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CustomizationMergerTest {

    @Test
    fun `reattaches an existing customization to the entry with the matching identity key`() {
        val entry = M3uEntry(title = "Rai 1", url = "http://example.com/rai1.m3u8", tvgId = "rai1.it")
        val previous = mapOf("rai1.it" to ContentCustomization(hidden = true))

        val merged = CustomizationMerger().merge(previous, listOf(entry))

        assertEquals(ContentCustomization(hidden = true), merged.single().second)
    }

    @Test
    fun `entries with no matching previous customization get the defaults`() {
        val entry = M3uEntry(title = "Nuovo Canale", url = "http://example.com/nuovo.m3u8", tvgId = "nuovo.it")

        val merged = CustomizationMerger().merge(previous = emptyMap(), freshEntries = listOf(entry))

        assertEquals(ContentCustomization(), merged.single().second)
    }

    @Test
    fun `customization survives a refresh even when the entry title changed, as long as the identity key matches`() {
        val renamedEntry = M3uEntry(title = "Rai Uno HD", url = "http://example.com/rai1.m3u8", tvgId = "rai1.it")
        val previous = mapOf("rai1.it" to ContentCustomization(favorite = true))

        val merged = CustomizationMerger().merge(previous, listOf(renamedEntry))

        assertEquals(ContentCustomization(favorite = true), merged.single().second)
    }

    @Test
    fun `customizations for entries no longer present in the fresh source disappear`() {
        val stillThere = M3uEntry(title = "Rai 1", url = "http://example.com/rai1.m3u8", tvgId = "rai1.it")
        val previous = mapOf(
            "rai1.it" to ContentCustomization(favorite = true),
            "canale-rimosso.it" to ContentCustomization(hidden = true)
        )

        val merged = CustomizationMerger().merge(previous, freshEntries = listOf(stillThere))

        assertEquals(1, merged.size)
        assertEquals("rai1.it", stillThere.tvgId)
    }
}
