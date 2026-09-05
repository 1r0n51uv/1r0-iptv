package com.ir0.iptv.domain.dashboard

import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.classification.Serie
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MemoriaFocusTest {

    private val memoria = MemoriaFocus()

    private val rai1 = ContentCard.Canale("Rai 1", null, "http://example.com/rai1.m3u8")
    private val dune = ContentCard.Film("Dune", null, "http://example.com/dune.mp4")
    private val simpson = ContentCard.SerieCard.Pronta("I Simpson", null, Serie("I Simpson", emptyList()))
    private val righe = listOf(
        RigaDashboard(TipoRiga.CANALI, listOf(rai1)),
        RigaDashboard(TipoRiga.FILM, listOf(dune)),
        RigaDashboard(TipoRiga.SERIE, listOf(simpson))
    )

    @Test
    fun `on a cold start the focus goes back to the last content it was on`() {
        val chiave = memoria.focusIniziale(righe, ultimaChiave = dune.chiaveIdentita, contenutoDiDefault = null)

        assertEquals(dune.chiaveIdentita, chiave)
    }

    @Test
    fun `without any history the focus goes to the Contenuto di default`() {
        val chiave = memoria.focusIniziale(righe, ultimaChiave = null, contenutoDiDefault = simpson.chiaveIdentita)

        assertEquals(simpson.chiaveIdentita, chiave)
    }

    @Test
    fun `with neither history nor default the focus goes to the first content of the first row`() {
        val chiave = memoria.focusIniziale(righe, ultimaChiave = null, contenutoDiDefault = null)

        assertEquals(rai1.chiaveIdentita, chiave)
    }

    @Test
    fun `a remembered content that is no longer in the catalog falls back to the default`() {
        val chiave = memoria.focusIniziale(
            righe,
            ultimaChiave = "http://example.com/sparito.mp4",
            contenutoDiDefault = simpson.chiaveIdentita
        )

        assertEquals(simpson.chiaveIdentita, chiave)
    }

    @Test
    fun `a Contenuto di default that is no longer in the catalog falls back to the first content`() {
        val chiave = memoria.focusIniziale(righe, ultimaChiave = null, contenutoDiDefault = "serie:Sparita")

        assertEquals(rai1.chiaveIdentita, chiave)
    }

    @Test
    fun `with no rows there is nothing to focus`() {
        assertNull(memoria.focusIniziale(emptyList(), ultimaChiave = null, contenutoDiDefault = null))
    }
}
