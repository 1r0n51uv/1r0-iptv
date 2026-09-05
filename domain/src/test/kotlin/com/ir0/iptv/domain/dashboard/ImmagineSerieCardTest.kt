package com.ir0.iptv.domain.dashboard

import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.classification.Episodio
import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.classification.Stagione
import com.ir0.iptv.domain.source.xtream.XtreamConnection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class ImmagineSerieCardTest {

    private fun episodio(slug: String, immagine: String? = null) =
        Episodio(title = slug, url = "http://example.com/$slug.mp4", episodeNumber = 1, immagine = immagine)

    private fun serieCard(
        poster: String?,
        vararg episodi: Episodio,
        immagineStagione: String? = null
    ) = ContentCard.SerieCard.Pronta(
        title = "The Bear",
        imageUrl = poster,
        serie = Serie("The Bear", listOf(Stagione(1, episodi.toList(), immagine = immagineStagione)))
    )

    @Test
    fun `usa l'immagine dell'Episodio in gioco quando c'e'`() {
        val card = serieCard(
            poster = "http://example.com/poster.jpg",
            episodio("s01e01", immagine = "http://example.com/e01.jpg"),
            episodio("s01e02", immagine = "http://example.com/e02.jpg")
        )

        val risultato = card.conImmagineDiEpisodio(listOf("http://example.com/s01e02.mp4"))

        assertEquals("http://example.com/e02.jpg", risultato.imageUrl)
    }

    @Test
    fun `senza immagine dell'Episodio usa la copertina della Stagione`() {
        val card = serieCard(
            "http://example.com/poster.jpg",
            episodio("s01e01"),
            immagineStagione = "http://example.com/stagione1.jpg"
        )

        val risultato = card.conImmagineDiEpisodio(listOf("http://example.com/s01e01.mp4"))

        assertEquals("http://example.com/stagione1.jpg", risultato.imageUrl)
    }

    @Test
    fun `ricade sulla locandina della Serie quando mancano Episodio e Stagione`() {
        val card = serieCard("http://example.com/poster.jpg", episodio("s01e01"))

        val risultato = card.conImmagineDiEpisodio(listOf("http://example.com/s01e01.mp4"))

        assertEquals("http://example.com/poster.jpg", risultato.imageUrl)
    }

    @Test
    fun `resta senza immagine quando mancano Episodio, Stagione e locandina`() {
        val card = serieCard(poster = null, episodio("s01e01"))

        val risultato = card.conImmagineDiEpisodio(listOf("http://example.com/s01e01.mp4"))

        assertEquals(null, risultato.imageUrl)
    }

    @Test
    fun `l'immagine dell'Episodio vince sulla copertina della Stagione`() {
        val card = serieCard(
            "http://example.com/poster.jpg",
            episodio("s01e01", immagine = "http://example.com/e01.jpg"),
            immagineStagione = "http://example.com/stagione1.jpg"
        )

        val risultato = card.conImmagineDiEpisodio(listOf("http://example.com/s01e01.mp4"))

        assertEquals("http://example.com/e01.jpg", risultato.imageUrl)
    }

    @Test
    fun `una Serie ancora da caricare non ha Episodi in memoria, quindi non cambia`() {
        val card = ContentCard.SerieCard.DaCaricare(
            title = "Severance",
            imageUrl = "http://example.com/sev.jpg",
            seriesId = 7,
            connection = XtreamConnection("host", 8080, "user", "pass")
        )

        val risultato = card.conImmagineDiEpisodio(listOf("http://example.com/qualsiasi.mp4"))

        assertSame(card, risultato)
    }
}
