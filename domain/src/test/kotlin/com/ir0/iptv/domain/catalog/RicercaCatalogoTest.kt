package com.ir0.iptv.domain.catalog

import com.ir0.iptv.domain.classification.Serie
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RicercaCatalogoTest {

    private val ricerca = RicercaCatalogo()

    private val catalogo = ContentCatalog(
        canali = listOf(ContentCard.Canale("Rai 1", null, "http://example.com/rai1.m3u8", categoria = "Generaliste")),
        film = listOf(ContentCard.Film("Dune: Parte Due", null, "http://example.com/dune.mp4")),
        serie = listOf(ContentCard.SerieCard.Pronta("The Bear", null, Serie("The Bear", emptyList())))
    )

    @Test
    fun `finds content whatever its type`() {
        assertEquals(listOf("Rai 1"), ricerca.cerca(catalogo, "rai").map { it.title })
        assertEquals(listOf("Dune: Parte Due"), ricerca.cerca(catalogo, "dune").map { it.title })
        assertEquals(listOf("The Bear"), ricerca.cerca(catalogo, "bear").map { it.title })
    }

    @Test
    fun `ignores case and surrounding spaces`() {
        assertEquals(listOf("The Bear"), ricerca.cerca(catalogo, "  BEAR ").map { it.title })
    }

    @Test
    fun `matches anywhere in the title, not only at the start`() {
        assertEquals(listOf("Dune: Parte Due"), ricerca.cerca(catalogo, "parte").map { it.title })
    }

    @Test
    fun `an empty query matches nothing, so the screen stays empty until you type`() {
        assertTrue(ricerca.cerca(catalogo, "").isEmpty())
        assertTrue(ricerca.cerca(catalogo, "   ").isEmpty())
    }

    @Test
    fun `a query matching nothing gives back nothing`() {
        assertTrue(ricerca.cerca(catalogo, "zzz").isEmpty())
    }

    @Test
    fun `results keep the catalog order, Canali then Film then Serie`() {
        val misto = ContentCatalog(
            canali = listOf(ContentCard.Canale("Sport Uno", null, "http://example.com/sport.m3u8")),
            film = listOf(ContentCard.Film("Sport Story", null, "http://example.com/sportstory.mp4")),
            serie = listOf(ContentCard.SerieCard.Pronta("Sport Life", null, Serie("Sport Life", emptyList())))
        )

        assertEquals(
            listOf("Sport Uno", "Sport Story", "Sport Life"),
            ricerca.cerca(misto, "sport").map { it.title }
        )
    }
}
