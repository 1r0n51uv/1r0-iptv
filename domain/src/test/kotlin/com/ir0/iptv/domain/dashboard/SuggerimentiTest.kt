package com.ir0.iptv.domain.dashboard

import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.customization.ContentCustomization
import com.ir0.iptv.domain.playback.TipoVisto
import com.ir0.iptv.domain.playback.Visto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SuggerimentiTest {

    private val suggerimenti = Suggerimenti()

    private val dune = ContentCard.Film("Dune", null, "http://example.com/dune.mp4")
    private val arrival = ContentCard.Film("Arrival", null, "http://example.com/arrival.mp4")
    private val bear = ContentCard.SerieCard.Pronta("The Bear", null, Serie("The Bear", emptyList()))
    private val catalogo = ContentCatalog(film = listOf(dune, arrival), serie = listOf(bear))

    @Test
    fun `without a single Visto or Preferito there is nothing to go on, so no call is made`() {
        assertNull(suggerimenti.prompt(catalogo, visti = emptyList(), personalizzazioni = emptyMap()))
    }

    @Test
    fun `the prompt carries what was watched and what is a Preferito`() {
        val prompt = suggerimenti.prompt(
            catalogo,
            visti = listOf(visto("http://example.com/dune.mp4", "Dune")),
            personalizzazioni = mapOf(bear.chiaveIdentita to ContentCustomization(favorite = true))
        )

        assertTrue(prompt!!.contains("Dune"))
        assertTrue(prompt.contains("The Bear"))
    }

    @Test
    fun `the prompt offers the catalog to pick from, capped so a huge M3U does not blow up the request`() {
        val enorme = ContentCatalog(
            film = (1..500).map { ContentCard.Film("Film $it", null, "http://example.com/$it.mp4") }
        )

        val prompt = suggerimenti.prompt(
            enorme,
            visti = listOf(visto("http://example.com/1.mp4", "Film 1")),
            personalizzazioni = emptyMap()
        )!!

        assertTrue(prompt.contains("Film 2"))
        assertTrue(!prompt.contains("Film 500"), "il campione di catalogo deve essere limitato")
    }

    @Test
    fun `reads back a plain JSON array of titles`() {
        val titoli = suggerimenti.titoliDaRisposta("""["Dune", "The Bear"]""")

        assertEquals(listOf("Dune", "The Bear"), titoli)
    }

    @Test
    fun `reads back a JSON array even when the model wraps it in prose or a code fence`() {
        val risposta = """
            Ecco i miei suggerimenti:
            ```json
            ["Dune", "Arrival"]
            ```
        """.trimIndent()

        assertEquals(listOf("Dune", "Arrival"), suggerimenti.titoliDaRisposta(risposta))
    }

    @Test
    fun `falls back to one title per line when the model ignores the JSON request`() {
        val titoli = suggerimenti.titoliDaRisposta("1. Dune\n2. The Bear\n\n")

        assertEquals(listOf("Dune", "The Bear"), titoli)
    }

    @Test
    fun `an empty answer gives no titles`() {
        assertTrue(suggerimenti.titoliDaRisposta("").isEmpty())
        assertTrue(suggerimenti.titoliDaRisposta("[]").isEmpty())
    }

    @Test
    fun `titles are matched back to the catalog, keeping the order the model chose`() {
        val riga = suggerimenti.riga(catalogo, listOf("The Bear", "Dune"))

        assertEquals(TipoRiga.SUGGERITI, riga?.tipo)
        assertEquals(listOf("The Bear", "Dune"), riga?.contenuti?.map { it.title })
    }

    @Test
    fun `a title the model invented is dropped instead of showing an empty card`() {
        val riga = suggerimenti.riga(catalogo, listOf("Dune", "Un Film Inventato"))

        assertEquals(listOf("Dune"), riga?.contenuti?.map { it.title })
    }

    @Test
    fun `matching ignores case and spacing, and the same title twice shows once`() {
        val riga = suggerimenti.riga(catalogo, listOf("  dune  ", "DUNE"))

        assertEquals(listOf("Dune"), riga?.contenuti?.map { it.title })
    }

    @Test
    fun `nothing matched means no row at all`() {
        assertNull(suggerimenti.riga(catalogo, listOf("Un Film Inventato")))
        assertNull(suggerimenti.riga(catalogo, emptyList()))
    }
}

private fun visto(chiave: String, titolo: String) = Visto(
    chiaveIdentita = chiave,
    tipo = TipoVisto.FILM,
    titolo = titolo,
    streamUrl = chiave,
    posizioneMs = 600_000,
    durataMs = 9_960_000,
    aggiornatoIl = 1
)
