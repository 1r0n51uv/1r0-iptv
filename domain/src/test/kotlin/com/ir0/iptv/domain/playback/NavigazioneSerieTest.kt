package com.ir0.iptv.domain.playback

import com.ir0.iptv.domain.classification.Episodio
import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.classification.Stagione
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class NavigazioneSerieTest {

    private val navigazione = NavigazioneSerie()

    private val serie = Serie(
        name = "The Bear",
        seasons = listOf(
            Stagione(number = 1, episodes = listOf(ep(1, 1), ep(1, 2))),
            Stagione(number = 2, episodes = listOf(ep(2, 1), ep(2, 2)))
        )
    )

    @Test
    fun `with nothing watched the Dettaglio opens on the first Stagione`() {
        assertEquals(1, navigazione.stagioneIniziale(serie, visti = emptyList())?.number)
    }

    @Test
    fun `the Dettaglio opens on the Stagione of the Episodio to resume`() {
        val visti = listOf(episodio(chiave = ep(2, 1).url, serie = "The Bear", posizioneMs = 600_000))

        assertEquals(2, navigazione.stagioneIniziale(serie, visti)?.number)
    }

    @Test
    fun `the Dettaglio opens on the Stagione of the Episodio that comes after the last completed one`() {
        val visti = listOf(
            episodio(chiave = ep(1, 2).url, serie = "The Bear", posizioneMs = 1_790_000, durataMs = 1_800_000)
        )

        assertEquals(2, navigazione.stagioneIniziale(serie, visti)?.number)
    }

    @Test
    fun `a Serie watched to the end opens again on the first Stagione`() {
        val visti = serie.seasons.flatMap { it.episodes }.map {
            episodio(chiave = it.url, serie = "The Bear", posizioneMs = 1_790_000, durataMs = 1_800_000)
        }

        assertEquals(1, navigazione.stagioneIniziale(serie, visti)?.number)
    }

    @Test
    fun `a Serie with no Stagione has none to open`() {
        assertNull(navigazione.stagioneIniziale(Serie("Vuota", emptyList()), visti = emptyList()))
    }
}

private fun ep(stagione: Int, numero: Int) = Episodio(
    title = "S0${stagione}E0$numero",
    url = "http://example.com/s${stagione}e$numero.mp4",
    episodeNumber = numero
)
