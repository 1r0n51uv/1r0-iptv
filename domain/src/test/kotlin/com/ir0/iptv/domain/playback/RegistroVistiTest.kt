package com.ir0.iptv.domain.playback

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RegistroVistiTest {

    private val registro = RegistroVisti()

    @Test
    fun `registers a Visto the first time a Film is opened`() {
        val visti = registro.registra(emptyList(), film(chiave = "dune", posizioneMs = 0))

        assertEquals(listOf("dune"), visti.map { it.chiaveIdentita })
    }

    @Test
    fun `updating the position of an already open Visto keeps a single entry`() {
        val iniziale = registro.registra(emptyList(), film(chiave = "dune", posizioneMs = 1_000))

        val aggiornato = registro.registra(iniziale, film(chiave = "dune", posizioneMs = 600_000))

        assertEquals(1, aggiornato.size)
        assertEquals(600_000, aggiornato.single().posizioneMs)
    }

    @Test
    fun `there is no resume position for a content never opened`() {
        assertNull(registro.posizioneDiRipresa(emptyList(), "mai-aperto"))
    }

    @Test
    fun `the resume position of a partially watched Film is the saved position`() {
        val visti = listOf(film(chiave = "dune", posizioneMs = 600_000, durataMs = 9_960_000))

        assertEquals(600_000, registro.posizioneDiRipresa(visti, "dune"))
    }

    @Test
    fun `a content watched past the completion threshold restarts from the beginning`() {
        val visti = listOf(film(chiave = "dune", posizioneMs = 9_800_000, durataMs = 9_960_000))

        assertNull(registro.posizioneDiRipresa(visti, "dune"))
    }

    @Test
    fun `a content barely started restarts from the beginning instead of resuming at a few seconds`() {
        val visti = listOf(film(chiave = "dune", posizioneMs = 2_000, durataMs = 9_960_000))

        assertNull(registro.posizioneDiRipresa(visti, "dune"))
    }

    @Test
    fun `a content whose duration is unknown is never considered completed`() {
        val visto = film(chiave = "dune", posizioneMs = 600_000, durataMs = 0)

        assertTrue(!registro.completato(visto))
        assertEquals(600_000, registro.posizioneDiRipresa(listOf(visto), "dune"))
    }

    @Test
    fun `continue watching lists the most recently updated first`() {
        val visti = listOf(
            film(chiave = "dune", posizioneMs = 600_000, aggiornatoIl = 100),
            film(chiave = "arrival", posizioneMs = 600_000, aggiornatoIl = 300),
            film(chiave = "sicario", posizioneMs = 600_000, aggiornatoIl = 200)
        )

        val ripresa = registro.continuaAGuardare(visti)

        assertEquals(listOf("arrival", "sicario", "dune"), ripresa.map { it.chiaveIdentita })
    }

    @Test
    fun `continue watching leaves out completed content`() {
        val visti = listOf(
            film(chiave = "dune", posizioneMs = 600_000, durataMs = 9_960_000),
            film(chiave = "arrival", posizioneMs = 9_800_000, durataMs = 9_960_000)
        )

        val ripresa = registro.continuaAGuardare(visti)

        assertEquals(listOf("dune"), ripresa.map { it.chiaveIdentita })
    }

    @Test
    fun `continue watching collapses a Serie to its most recently watched Episodio`() {
        val visti = listOf(
            episodio(chiave = "bear-s01e01", serie = "The Bear", aggiornatoIl = 100),
            episodio(chiave = "bear-s02e04", serie = "The Bear", aggiornatoIl = 400),
            episodio(chiave = "bear-s01e02", serie = "The Bear", aggiornatoIl = 200)
        )

        val ripresa = registro.continuaAGuardare(visti)

        assertEquals(listOf("bear-s02e04"), ripresa.map { it.chiaveIdentita })
    }

    @Test
    fun `episodes of different Serie stay separate in continue watching`() {
        val visti = listOf(
            episodio(chiave = "bear-s01e01", serie = "The Bear", aggiornatoIl = 100),
            episodio(chiave = "severance-s01e01", serie = "Severance", aggiornatoIl = 200)
        )

        val ripresa = registro.continuaAGuardare(visti)

        assertEquals(listOf("severance-s01e01", "bear-s01e01"), ripresa.map { it.chiaveIdentita })
    }
}

internal fun film(
    chiave: String,
    posizioneMs: Long = 0,
    durataMs: Long = 9_960_000,
    aggiornatoIl: Long = 0
) = Visto(
    chiaveIdentita = chiave,
    tipo = TipoVisto.FILM,
    titolo = chiave,
    streamUrl = "http://example.com/$chiave.mp4",
    posizioneMs = posizioneMs,
    durataMs = durataMs,
    aggiornatoIl = aggiornatoIl
)

internal fun episodio(
    chiave: String,
    serie: String,
    posizioneMs: Long = 600_000,
    durataMs: Long = 1_800_000,
    aggiornatoIl: Long = 0
) = Visto(
    chiaveIdentita = chiave,
    tipo = TipoVisto.EPISODIO,
    titolo = chiave,
    streamUrl = "http://example.com/$chiave.mp4",
    posizioneMs = posizioneMs,
    durataMs = durataMs,
    aggiornatoIl = aggiornatoIl,
    serie = serie
)
