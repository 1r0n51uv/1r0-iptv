package com.ir0.iptv.domain.catalog

import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.customization.ContentCustomization
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ElencoPreferitiTest {

    private val elenco = ElencoPreferiti()

    private val rai1 = ContentCard.Canale("Rai 1", null, "http://example.com/rai1.m3u8")
    private val dune = ContentCard.Film("Dune", null, "http://example.com/dune.mp4")
    private val bear = ContentCard.SerieCard.Pronta("The Bear", null, Serie("The Bear", emptyList()))
    private val catalogo = ContentCatalog(canali = listOf(rai1), film = listOf(dune), serie = listOf(bear))

    @Test
    fun `with no customization at all nothing is a Preferito`() {
        assertTrue(elenco.preferiti(catalogo, emptyMap()).isEmpty())
    }

    @Test
    fun `the list mixes Canali, Film e Serie in a single list`() {
        val personalizzazioni = mapOf(
            rai1.chiaveIdentita to ContentCustomization(favorite = true),
            dune.chiaveIdentita to ContentCustomization(favorite = true),
            bear.chiaveIdentita to ContentCustomization(favorite = true)
        )

        val preferiti = elenco.preferiti(catalogo, personalizzazioni)

        assertEquals(listOf("Rai 1", "Dune", "The Bear"), preferiti.map { it.title })
    }

    @Test
    fun `a customization that is not a Preferito is left out`() {
        val personalizzazioni = mapOf(
            rai1.chiaveIdentita to ContentCustomization(favorite = true),
            dune.chiaveIdentita to ContentCustomization(hidden = true)
        )

        val preferiti = elenco.preferiti(catalogo, personalizzazioni)

        assertEquals(listOf("Rai 1"), preferiti.map { it.title })
    }

    @Test
    fun `a Preferito whose content disappeared from the Sorgente is not listed`() {
        val personalizzazioni = mapOf("http://example.com/sparito.mp4" to ContentCustomization(favorite = true))

        assertTrue(elenco.preferiti(catalogo, personalizzazioni).isEmpty())
    }

    @Test
    fun `a Serie keeps its Preferito across the switch from DaCaricare to Pronta`() {
        val daCaricare = ContentCard.SerieCard.DaCaricare("The Bear", null, seriesId = 42, connection = connessione)
        val personalizzazioni = mapOf(daCaricare.chiaveIdentita to ContentCustomization(favorite = true))

        assertTrue(elenco.preferito(personalizzazioni, bear))
    }

    @Test
    fun `toggling turns a content into a Preferito and back`() {
        val acceso = elenco.cambiaPreferito(emptyMap(), dune)
        assertTrue(elenco.preferito(acceso, dune))

        val spento = elenco.cambiaPreferito(acceso, dune)
        assertFalse(elenco.preferito(spento, dune))
    }

    @Test
    fun `toggling a Preferito leaves the other customizations of that content untouched`() {
        val personalizzazioni = mapOf(dune.chiaveIdentita to ContentCustomization(hidden = true))

        val acceso = elenco.cambiaPreferito(personalizzazioni, dune)

        assertEquals(ContentCustomization(hidden = true, favorite = true), acceso[dune.chiaveIdentita])
    }
}

private val connessione = com.ir0.iptv.domain.source.xtream.XtreamConnection(
    host = "example.com",
    port = 8080,
    username = "u",
    password = "p"
)
