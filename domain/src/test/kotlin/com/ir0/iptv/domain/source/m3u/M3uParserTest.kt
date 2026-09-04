package com.ir0.iptv.domain.source.m3u

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class M3uParserTest {

    @Test
    fun `parses a single entry with tvg attributes and group-title`() {
        val content = """
            #EXTM3U
            #EXTINF:-1 tvg-id="rai1.it" tvg-logo="http://logos.example/rai1.png" group-title="Generaliste",Rai 1
            http://example.com/rai1.m3u8
        """.trimIndent()

        val entries = M3uParser().parse(content)

        assertEquals(
            listOf(
                M3uEntry(
                    title = "Rai 1",
                    url = "http://example.com/rai1.m3u8",
                    tvgId = "rai1.it",
                    tvgLogo = "http://logos.example/rai1.png",
                    groupTitle = "Generaliste"
                )
            ),
            entries
        )
    }

    @Test
    fun `parses multiple entries in order`() {
        val content = """
            #EXTM3U
            #EXTINF:-1 tvg-id="rai1.it" tvg-logo="http://logos.example/rai1.png" group-title="Generaliste",Rai 1
            http://example.com/rai1.m3u8
            #EXTINF:-1 tvg-id="rai2.it" tvg-logo="http://logos.example/rai2.png" group-title="Generaliste",Rai 2
            http://example.com/rai2.m3u8
        """.trimIndent()

        val entries = M3uParser().parse(content)

        assertEquals(listOf("Rai 1", "Rai 2"), entries.map { it.title })
        assertEquals(
            listOf("http://example.com/rai1.m3u8", "http://example.com/rai2.m3u8"),
            entries.map { it.url }
        )
    }

    @Test
    fun `entry with no tvg attributes has null tvgId, tvgLogo and groupTitle`() {
        val content = """
            #EXTM3U
            #EXTINF:-1,Canale Senza Attributi
            http://example.com/senza-attributi.m3u8
        """.trimIndent()

        val entry = M3uParser().parse(content).single()

        assertEquals(
            M3uEntry(
                title = "Canale Senza Attributi",
                url = "http://example.com/senza-attributi.m3u8",
                tvgId = null,
                tvgLogo = null,
                groupTitle = null
            ),
            entry
        )
    }

    @Test
    fun `ignores blank lines between entries`() {
        val content = """
            #EXTM3U

            #EXTINF:-1,Rai 1
            http://example.com/rai1.m3u8

            #EXTINF:-1,Rai 2
            http://example.com/rai2.m3u8
        """.trimIndent()

        val entries = M3uParser().parse(content)

        assertEquals(listOf("Rai 1", "Rai 2"), entries.map { it.title })
    }
}
