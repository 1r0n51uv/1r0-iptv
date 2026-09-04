package com.ir0.iptv.domain.classification

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EpisodeInfoExtractorTest {

    @Test
    fun `extracts series name, season and episode from a SxxExx title`() {
        val result = EpisodeInfoExtractor().extract("The Bear S02E04")

        assertEquals(
            EpisodeInfo(seriesName = "The Bear", season = 2, episode = 4, episodeTitle = null),
            result
        )
    }

    @Test
    fun `extracts the episode title when present after the code`() {
        val result = EpisodeInfoExtractor().extract("The Bear S02E04 Fondo Ammortizzatore")

        assertEquals(
            EpisodeInfo(seriesName = "The Bear", season = 2, episode = 4, episodeTitle = "Fondo Ammortizzatore"),
            result
        )
    }

    @Test
    fun `matches lowercase sxxexx code`() {
        val result = EpisodeInfoExtractor().extract("The Bear s02e04")

        assertEquals(
            EpisodeInfo(seriesName = "The Bear", season = 2, episode = 4, episodeTitle = null),
            result
        )
    }

    @Test
    fun `returns null when title has no SxxExx code`() {
        val result = EpisodeInfoExtractor().extract("Speciale dietro le quinte")

        assertEquals(null, result)
    }
}
