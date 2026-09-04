package com.ir0.iptv.domain.customization

import com.ir0.iptv.domain.source.m3u.M3uEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IdentityKeyResolverTest {

    @Test
    fun `uses tvg-id as the identity key when present`() {
        val entry = M3uEntry(title = "Rai 1", url = "http://example.com/rai1.m3u8", tvgId = "rai1.it")

        val key = IdentityKeyResolver().resolve(entry)

        assertEquals("rai1.it", key)
    }

    @Test
    fun `falls back to the stream url when tvg-id is missing`() {
        val entry = M3uEntry(title = "Rai 1", url = "http://example.com/rai1.m3u8", tvgId = null)

        val key = IdentityKeyResolver().resolve(entry)

        assertEquals("http://example.com/rai1.m3u8", key)
    }

    @Test
    fun `falls back to the stream url when tvg-id is blank`() {
        val entry = M3uEntry(title = "Rai 1", url = "http://example.com/rai1.m3u8", tvgId = "")

        val key = IdentityKeyResolver().resolve(entry)

        assertEquals("http://example.com/rai1.m3u8", key)
    }
}
