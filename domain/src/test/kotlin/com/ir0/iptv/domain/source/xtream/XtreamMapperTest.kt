package com.ir0.iptv.domain.source.xtream

import com.ir0.iptv.domain.source.m3u.M3uEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class XtreamMapperTest {

    private val connection = XtreamConnection(
        host = "iptv.provider.example",
        port = 8080,
        username = "user1",
        password = "pass1"
    )

    @Test
    fun `maps a live stream into an entry with a playable stream url`() {
        val dto = XtreamLiveStreamDto(
            name = "Rai 1",
            streamId = 123,
            streamIcon = "http://logos.example/rai1.png",
            epgChannelId = "rai1.it",
            categoryName = "Generaliste"
        )

        val entry = XtreamMapper().toChannel(dto, connection)

        assertEquals(
            M3uEntry(
                title = "Rai 1",
                url = "http://iptv.provider.example:8080/live/user1/pass1/123.m3u8",
                tvgId = "rai1.it",
                tvgLogo = "http://logos.example/rai1.png",
                groupTitle = "Generaliste"
            ),
            entry
        )
    }

    @Test
    fun `maps a vod stream into a movie with a playable stream url`() {
        val dto = XtreamVodStreamDto(
            name = "Dune: Parte Due",
            streamId = 456,
            streamIcon = "http://logos.example/dune2.jpg",
            plot = "Paul Atreides si unisce ai Fremen per vendicare la sua famiglia.",
            categoryName = "Film",
            containerExtension = "mp4"
        )

        val movie = XtreamMapper().toMovie(dto, connection)

        assertEquals(
            XtreamMovie(
                title = "Dune: Parte Due",
                url = "http://iptv.provider.example:8080/movie/user1/pass1/456.mp4",
                poster = "http://logos.example/dune2.jpg",
                plot = "Paul Atreides si unisce ai Fremen per vendicare la sua famiglia.",
                categoryName = "Film"
            ),
            movie
        )
    }

    @Test
    fun `maps series info into a Serie with seasons and episodes ordered and urls built`() {
        val dto = XtreamSeriesInfoDto(
            seriesName = "The Bear",
            episodesBySeason = mapOf(
                2 to listOf(XtreamEpisodeDto(id = 902, episodeNum = 1, title = "Prenotazioni", containerExtension = "mp4")),
                1 to listOf(
                    XtreamEpisodeDto(id = 101, episodeNum = 1, title = "Sistemi", containerExtension = "mp4"),
                    XtreamEpisodeDto(id = 102, episodeNum = 2, title = "Pasta", containerExtension = "mp4")
                )
            ),
            cover = "http://logos.example/thebear.jpg"
        )

        val serie = XtreamMapper().toSerie(dto, connection)

        assertEquals("The Bear", serie.name)
        assertEquals("http://logos.example/thebear.jpg", serie.poster)
        assertEquals(listOf(1, 2), serie.seasons.map { it.number })
        assertEquals(
            listOf("Sistemi", "Pasta"),
            serie.seasons[0].episodes.map { it.title }
        )
        assertEquals(
            "http://iptv.provider.example:8080/series/user1/pass1/101.mp4",
            serie.seasons[0].episodes[0].url
        )
    }
}
