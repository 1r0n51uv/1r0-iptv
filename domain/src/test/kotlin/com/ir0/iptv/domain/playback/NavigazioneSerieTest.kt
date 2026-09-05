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

    @Test
    fun `episodiSuccessivi lists everything after the given Episodio, into the next Stagione`() {
        val successivi = navigazione.episodiSuccessivi(serie, ep(1, 2).url)

        assertEquals(listOf(ep(2, 1).url, ep(2, 2).url), successivi.map { it.url })
    }

    @Test
    fun `episodiSuccessivi is empty for the last Episodio of the last Stagione`() {
        assertEquals(emptyList<String>(), navigazione.episodiSuccessivi(serie, ep(2, 2).url).map { it.url })
    }

    @Test
    fun `episodiSuccessivi is empty when the Episodio is not part of the Serie`() {
        assertEquals(emptyList<String>(), navigazione.episodiSuccessivi(serie, "http://example.com/altro.mp4").map { it.url })
    }
}

private fun ep(stagione: Int, numero: Int) = Episodio(
    title = "S0${stagione}E0$numero",
    url = "http://example.com/s${stagione}e$numero.mp4",
    episodeNumber = numero
)
