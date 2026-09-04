package com.ir0.iptv.domain.classification

import com.ir0.iptv.domain.source.m3u.M3uEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ContentClassifierTest {

    private fun entry(groupTitle: String?) = M3uEntry(
        title = "Voce di test",
        url = "http://example.com/stream.m3u8",
        groupTitle = groupTitle
    )

    @Test
    fun `entry with no film or serie keyword in group-title is a Canale`() {
        val result = ContentClassifier().classify(entry(groupTitle = "Generaliste"))

        assertEquals(ContentType.CANALE, result)
    }

    @Test
    fun `group-title containing 'film' is a Film`() {
        val result = ContentClassifier().classify(entry(groupTitle = "Film Azione"))

        assertEquals(ContentType.FILM, result)
    }

    @Test
    fun `group-title containing 'serie' is a Serie`() {
        val result = ContentClassifier().classify(entry(groupTitle = "Serie TV"))

        assertEquals(ContentType.SERIE, result)
    }

    @Test
    fun `missing group-title is a Canale`() {
        val result = ContentClassifier().classify(entry(groupTitle = null))

        assertEquals(ContentType.CANALE, result)
    }

    @Test
    fun `keyword matching ignores case`() {
        assertEquals(ContentType.FILM, ContentClassifier().classify(entry(groupTitle = "FILM AZIONE")))
        assertEquals(ContentType.SERIE, ContentClassifier().classify(entry(groupTitle = "SERIE")))
    }
}
