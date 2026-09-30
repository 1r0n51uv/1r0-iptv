package com.ir0.iptv.domain.catalog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ChiaveIdentitaXtreamTest {

    @Test
    fun `builds a stable key from the stream type and id`() {
        assertEquals("xtream-film:683439", chiaveIdentitaXtream("movie", 683439))
        assertEquals("xtream-canale:289", chiaveIdentitaXtream("live", 289))
        assertEquals("xtream-episodio:91069", chiaveIdentitaXtream("series", 91069))
    }

    @Test
    fun `migrates an old Xtream stream URL to the new stable key, regardless of host or credentials`() {
        assertEquals(
            "xtream-film:683439",
            migraChiaveIdentita("http://7qcpnm.stkyn.com:80/movie/amy5_3p519k/pbst7ybp/683439.mp4")
        )
        assertEquals(
            "xtream-canale:712323",
            migraChiaveIdentita("http://7qcpnm.stkyn.com:80/live/amy5_3p519k/pbst7ybp/712323.m3u8")
        )
        assertEquals(
            "xtream-episodio:91069",
            migraChiaveIdentita("http://7qcpnm.stkyn.com:80/series/amy5_3p519k/pbst7ybp/91069.mkv")
        )
        // Stesso streamId, credenziali e host completamente diversi: stessa Chiave migrata.
        assertEquals(
            migraChiaveIdentita("http://5c29u5.thkyn.com:8080/movie/amy5_9GTK8X/qwupusbQ/683439.mp4"),
            migraChiaveIdentita("http://7qcpnm.stkyn.com:80/movie/amy5_3p519k/pbst7ybp/683439.mp4")
        )
    }

    @Test
    fun `leaves a non-Xtream key (M3U URL, or already-migrated key) untouched`() {
        assertEquals("http://esempio.tv/rai1.m3u8", migraChiaveIdentita("http://esempio.tv/rai1.m3u8"))
        assertEquals("xtream-film:683439", migraChiaveIdentita("xtream-film:683439"))
        assertEquals("serie:One Piece (1999)", migraChiaveIdentita("serie:One Piece (1999)"))
    }
}
