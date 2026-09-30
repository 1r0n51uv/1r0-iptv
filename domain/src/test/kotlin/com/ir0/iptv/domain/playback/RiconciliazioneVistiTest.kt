package com.ir0.iptv.domain.playback

import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.classification.Episodio
import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.classification.Stagione
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class RiconciliazioneVistiTest {

    private val riconciliazione = RiconciliazioneVisti()

    @Test
    fun `leaves a Visto untouched when its Chiave still exists in the fresh catalog`() {
        val dune = ContentCard.Film(title = "Dune", imageUrl = null, streamUrl = "http://nuovo.example/dune.mp4")
        val visto = filmVisto(chiave = dune.chiaveIdentita, titolo = "Dune")
        val catalogo = ContentCatalog(film = listOf(dune))

        val risultato = riconciliazione.riconcilia(listOf(visto), catalogo)

        assertSame(visto, risultato.single())
    }

    @Test
    fun `reattaches a Film Visto to its new Chiave when the old one is gone but the title uniquely matches`() {
        val duneNuovo = ContentCard.Film(title = "Dune", imageUrl = null, streamUrl = "http://nuovo.example/dune.mp4")
        val visto = filmVisto(chiave = "http://vecchio.example/dune.mp4", titolo = "Dune", posizioneMs = 600_000)
        val catalogo = ContentCatalog(film = listOf(duneNuovo))

        val risultato = riconciliazione.riconcilia(listOf(visto), catalogo).single()

        assertEquals(duneNuovo.chiaveIdentita, risultato.chiaveIdentita)
        assertEquals(duneNuovo.streamUrl, risultato.streamUrl)
        // Il resto del Visto (posizione, timestamp...) non deve cambiare, solo l'identita'.
        assertEquals(600_000, risultato.posizioneMs)
    }

    @Test
    fun `does not reattach when two Film share the same title (ambiguous, fails closed)`() {
        val unoDeiDue = ContentCard.Film(title = "Dune", imageUrl = null, streamUrl = "http://nuovo.example/dune-1.mp4")
        val altroDeiDue = ContentCard.Film(title = "Dune", imageUrl = null, streamUrl = "http://nuovo.example/dune-2.mp4")
        val visto = filmVisto(chiave = "http://vecchio.example/dune.mp4", titolo = "Dune")
        val catalogo = ContentCatalog(film = listOf(unoDeiDue, altroDeiDue))

        val risultato = riconciliazione.riconcilia(listOf(visto), catalogo).single()

        assertEquals("http://vecchio.example/dune.mp4", risultato.chiaveIdentita)
    }

    @Test
    fun `reattaches an Episodio Visto by matching Serie name and Episodio title`() {
        val episodioNuovo = Episodio(title = "Sistemi", url = "http://nuovo.example/s01e01.mp4", episodeNumber = 1)
        val serieNuova = ContentCard.SerieCard.Pronta(
            title = "The Bear",
            imageUrl = null,
            serie = Serie(name = "The Bear", seasons = listOf(Stagione(number = 1, episodes = listOf(episodioNuovo))))
        )
        val visto = episodioVisto(
            chiave = "http://vecchio.example/s01e01.mp4",
            serie = "The Bear",
            titolo = "Sistemi",
            posizioneMs = 60_000
        )
        val catalogo = ContentCatalog(serie = listOf(serieNuova))

        val risultato = riconciliazione.riconcilia(listOf(visto), catalogo).single()

        assertEquals(episodioNuovo.url, risultato.chiaveIdentita)
        assertEquals(60_000, risultato.posizioneMs)
    }

    @Test
    fun `does not reattach an Episodio when the Serie itself is not found`() {
        val visto = episodioVisto(chiave = "http://vecchio.example/s01e01.mp4", serie = "The Bear", titolo = "Sistemi")
        val catalogo = ContentCatalog(serie = emptyList())

        val risultato = riconciliazione.riconcilia(listOf(visto), catalogo).single()

        assertEquals("http://vecchio.example/s01e01.mp4", risultato.chiaveIdentita)
    }
}

private fun filmVisto(chiave: String, titolo: String, posizioneMs: Long = 0) = Visto(
    chiaveIdentita = chiave,
    tipo = TipoVisto.FILM,
    titolo = titolo,
    streamUrl = "http://vecchio.example/stream.mp4",
    posizioneMs = posizioneMs,
    durataMs = 9_960_000,
    aggiornatoIl = 0
)

private fun episodioVisto(chiave: String, serie: String, titolo: String, posizioneMs: Long = 0) = Visto(
    chiaveIdentita = chiave,
    tipo = TipoVisto.EPISODIO,
    titolo = titolo,
    streamUrl = "http://vecchio.example/stream.mp4",
    posizioneMs = posizioneMs,
    durataMs = 1_800_000,
    aggiornatoIl = 0,
    serie = serie
)
