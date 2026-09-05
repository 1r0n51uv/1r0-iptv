package com.ir0.iptv.domain.dashboard

import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.classification.Episodio
import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.classification.Stagione
import com.ir0.iptv.domain.customization.ContentCustomization
import com.ir0.iptv.domain.playback.TipoVisto
import com.ir0.iptv.domain.playback.Visto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RilevatoreNuoviEpisodiTest {

    private val rilevatore = RilevatoreNuoviEpisodi()

    private val bear = ContentCard.SerieCard.Pronta(
        "The Bear",
        null,
        Serie("The Bear", listOf(Stagione(1, listOf(episodio("s01e01"), episodio("s01e02")))))
    )
    private val severance = ContentCard.SerieCard.Pronta(
        "Severance",
        null,
        Serie("Severance", listOf(Stagione(1, listOf(episodio("sev-s01e01")))))
    )
    private val catalogo = ContentCatalog(serie = listOf(bear, severance))

    @Test
    fun `a Serie is followed when it is a Preferito`() {
        val seguite = rilevatore.serieSeguite(
            catalogo,
            visti = emptyList(),
            personalizzazioni = mapOf(bear.chiaveIdentita to ContentCustomization(favorite = true))
        )

        assertEquals(listOf<ContentCard.SerieCard>(bear), seguite)
    }

    @Test
    fun `a Serie is followed when one of its Episodi has been watched`() {
        val visti = listOf(visto("http://example.com/s01e01.mp4", serie = "The Bear"))

        val seguite = rilevatore.serieSeguite(catalogo, visti, personalizzazioni = emptyMap())

        assertEquals(listOf<ContentCard.SerieCard>(bear), seguite)
    }

    @Test
    fun `a Serie nobody watched or starred is not followed`() {
        assertTrue(rilevatore.serieSeguite(catalogo, emptyList(), emptyMap()).isEmpty())
    }

    @Test
    fun `a Serie seen for the first time has no new Episodi, so the first run stays quiet`() {
        val attuali = mapOf(bear.chiaveIdentita to setOf("s01e01", "s01e02"))

        assertTrue(rilevatore.nuovi(attuali, precedenti = emptyMap()).isEmpty())
    }

    @Test
    fun `an Episodio that was not there at the last refresh is new`() {
        val precedenti = mapOf(bear.chiaveIdentita to setOf("s01e01"))
        val attuali = mapOf(bear.chiaveIdentita to setOf("s01e01", "s01e02"))

        assertEquals(mapOf(bear.chiaveIdentita to setOf("s01e02")), rilevatore.nuovi(attuali, precedenti))
    }

    @Test
    fun `a Serie with nothing added produces nothing`() {
        val stessi = mapOf(bear.chiaveIdentita to setOf("s01e01", "s01e02"))

        assertTrue(rilevatore.nuovi(stessi, stessi).isEmpty())
    }

    @Test
    fun `Episodi that disappeared from the Sorgente are not reported as new`() {
        val precedenti = mapOf(bear.chiaveIdentita to setOf("s01e01", "s01e02"))
        val attuali = mapOf(bear.chiaveIdentita to setOf("s01e01"))

        assertTrue(rilevatore.nuovi(attuali, precedenti).isEmpty())
    }

    @Test
    fun `the Nuovi episodi row lists the Serie that got something new`() {
        val precedenti = mapOf(bear.chiaveIdentita to setOf("s01e01"))
        val attuali = mapOf(bear.chiaveIdentita to setOf("s01e01", "s01e02"))

        val riga = rilevatore.riga(listOf(bear, severance), rilevatore.nuovi(attuali, precedenti))

        assertEquals(TipoRiga.NUOVI_EPISODI, riga?.tipo)
        assertEquals(listOf<ContentCard>(bear), riga?.contenuti)
    }

    @Test
    fun `with nothing new there is no row at all`() {
        assertEquals(null, rilevatore.riga(listOf(bear), emptyMap()))
    }
}

private fun episodio(slug: String) =
    Episodio(title = slug, url = "http://example.com/$slug.mp4", episodeNumber = 1)

private fun visto(chiave: String, serie: String) = Visto(
    chiaveIdentita = chiave,
    tipo = TipoVisto.EPISODIO,
    titolo = chiave,
    streamUrl = chiave,
    posizioneMs = 600_000,
    durataMs = 1_800_000,
    aggiornatoIl = 1,
    serie = serie
)
