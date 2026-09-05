package com.ir0.iptv.domain.dashboard

import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.customization.ContentCustomization
import com.ir0.iptv.domain.playback.TipoVisto
import com.ir0.iptv.domain.playback.Visto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CostruttoreDashboardTest {

    private val costruttore = CostruttoreDashboard()

    private val rai1 = ContentCard.Canale("Rai 1", null, "http://example.com/rai1.m3u8")
    private val dune = ContentCard.Film("Dune", null, "http://example.com/dune.mp4")
    private val bear = ContentCard.SerieCard.Pronta("The Bear", null, Serie("The Bear", emptyList()))
    private val catalogo = ContentCatalog(canali = listOf(rai1), film = listOf(dune), serie = listOf(bear))

    @Test
    fun `an empty catalog produces no rows at all`() {
        assertTrue(costruttore.costruisci(ContentCatalog(), emptyList(), emptyMap()).isEmpty())
    }

    @Test
    fun `rows come in a fixed order and empty ones are left out`() {
        val righe = costruttore.costruisci(catalogo, emptyList(), emptyMap())

        assertEquals(listOf(TipoRiga.CANALI, TipoRiga.FILM, TipoRiga.SERIE), righe.map { it.tipo })
    }

    @Test
    fun `a half watched Film opens the continue watching row`() {
        val visti = listOf(visto(dune.chiaveIdentita, TipoVisto.FILM))

        val righe = costruttore.costruisci(catalogo, visti, emptyMap())

        assertEquals(TipoRiga.CONTINUA, righe.first().tipo)
        assertEquals(listOf(dune), righe.first().contenuti)
    }

    @Test
    fun `a half watched Episodio shows up as its Serie`() {
        val visti = listOf(
            visto("http://example.com/bear-s01e01.mp4", TipoVisto.EPISODIO, serie = "The Bear")
        )

        val righe = costruttore.costruisci(catalogo, visti, emptyMap())

        assertEquals(listOf<ContentCard>(bear), righe.first().contenuti)
    }

    @Test
    fun `content that disappeared from the Sorgente drops out of continue watching`() {
        val visti = listOf(visto("http://example.com/sparito.mp4", TipoVisto.FILM))

        val righe = costruttore.costruisci(catalogo, visti, emptyMap())

        assertTrue(righe.none { it.tipo == TipoRiga.CONTINUA })
    }

    @Test
    fun `the Preferiti row shows up only once something is a Preferito`() {
        val senza = costruttore.costruisci(catalogo, emptyList(), emptyMap())
        assertTrue(senza.none { it.tipo == TipoRiga.PREFERITI })

        val con = costruttore.costruisci(
            catalogo,
            emptyList(),
            mapOf(rai1.chiaveIdentita to ContentCustomization(favorite = true))
        )

        assertEquals(listOf<ContentCard>(rai1), con.first { it.tipo == TipoRiga.PREFERITI }.contenuti)
    }

    @Test
    fun `continue watching comes before Preferiti, which comes before the catalog rows`() {
        val righe = costruttore.costruisci(
            catalogo,
            listOf(visto(dune.chiaveIdentita, TipoVisto.FILM)),
            mapOf(rai1.chiaveIdentita to ContentCustomization(favorite = true))
        )

        assertEquals(
            listOf(TipoRiga.CONTINUA, TipoRiga.PREFERITI, TipoRiga.CANALI, TipoRiga.FILM, TipoRiga.SERIE),
            righe.map { it.tipo }
        )
    }
}

private fun visto(chiave: String, tipo: TipoVisto, serie: String? = null) = Visto(
    chiaveIdentita = chiave,
    tipo = tipo,
    titolo = chiave,
    streamUrl = chiave,
    posizioneMs = 600_000,
    durataMs = 9_960_000,
    aggiornatoIl = 1,
    serie = serie
)
