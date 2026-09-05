package com.ir0.iptv.domain.playback

import com.ir0.iptv.domain.classification.Episodio
import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.classification.Stagione
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ProssimaVisioneResolverTest {

    private val resolver = ProssimaVisioneResolver()

    private val serie = Serie(
        name = "The Bear",
        seasons = listOf(
            Stagione(number = 1, episodes = listOf(ep(1, 1), ep(1, 2))),
            Stagione(number = 2, episodes = listOf(ep(2, 1), ep(2, 2))),
            Stagione(number = null, episodes = listOf(Episodio("Speciale", url("extra"), episodeNumber = null)))
        )
    )

    @Test
    fun `with nothing watched the next watch is the first Episodio of the first Stagione`() {
        val prossima = resolver.risolvi(serie, visti = emptyList())

        assertEquals(ProssimaVisione.Inizia(ep(1, 1)), prossima)
    }

    @Test
    fun `an Episodio left in the middle is resumed at its saved position`() {
        val visti = listOf(episodio(chiave = url("s2e1"), serie = "The Bear", posizioneMs = 600_000))

        val prossima = resolver.risolvi(serie, visti)

        assertEquals(ProssimaVisione.Riprendi(ep(2, 1), posizioneMs = 600_000), prossima)
    }

    @Test
    fun `after a completed Episodio the next watch is the one that follows it`() {
        val visti = listOf(
            episodio(chiave = url("s1e1"), serie = "The Bear", posizioneMs = 1_790_000, durataMs = 1_800_000)
        )

        val prossima = resolver.risolvi(serie, visti)

        assertEquals(ProssimaVisione.Inizia(ep(1, 2)), prossima)
    }

    @Test
    fun `after the last Episodio of a Stagione the next watch crosses into the following Stagione`() {
        val visti = listOf(
            episodio(chiave = url("s1e2"), serie = "The Bear", posizioneMs = 1_790_000, durataMs = 1_800_000)
        )

        val prossima = resolver.risolvi(serie, visti)

        assertEquals(ProssimaVisione.Inizia(ep(2, 1)), prossima)
    }

    @Test
    fun `an Episodio in progress wins over an earlier completed one`() {
        val visti = listOf(
            episodio(chiave = url("s1e1"), serie = "The Bear", posizioneMs = 1_790_000, durataMs = 1_800_000, aggiornatoIl = 100),
            episodio(chiave = url("s2e2"), serie = "The Bear", posizioneMs = 300_000, aggiornatoIl = 200)
        )

        val prossima = resolver.risolvi(serie, visti)

        assertEquals(ProssimaVisione.Riprendi(ep(2, 2), posizioneMs = 300_000), prossima)
    }

    @Test
    fun `an Episodio barely started is offered from the beginning instead of resumed`() {
        val visti = listOf(episodio(chiave = url("s2e1"), serie = "The Bear", posizioneMs = 2_000))

        val prossima = resolver.risolvi(serie, visti)

        assertEquals(ProssimaVisione.Inizia(ep(2, 1)), prossima)
    }

    @Test
    fun `once every Episodio has been completed the Serie is done`() {
        val visti = serie.seasons.flatMap { it.episodes }.map { episodio ->
            episodio(chiave = episodio.url, serie = "The Bear", posizioneMs = 1_790_000, durataMs = 1_800_000)
        }

        val prossima = resolver.risolvi(serie, visti)

        assertEquals(ProssimaVisione.Completata, prossima)
    }

    @Test
    fun `a Serie with no Episodio at all is done`() {
        val prossima = resolver.risolvi(Serie(name = "Vuota", seasons = emptyList()), visti = emptyList())

        assertEquals(ProssimaVisione.Completata, prossima)
    }
}

private fun ep(stagione: Int, numero: Int) =
    Episodio(title = "S0${stagione}E0$numero", url = url("s${stagione}e$numero"), episodeNumber = numero)

private fun url(slug: String) = "http://example.com/$slug.mp4"
