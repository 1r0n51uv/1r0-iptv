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
        RigaDashboard(TipoRiga.CONTINUA, listOf(dune)),
        RigaDashboard(TipoRiga.NUOVI_EPISODI, emptyList()),
        RigaDashboard(TipoRiga.PREFERITI, listOf(simpson))
    )

    @Test
    fun `on a cold start the focus goes to the content to resume`() {
        val chiave = memoria.focusIniziale(righe, contenutoDiDefault = simpson.chiaveIdentita)

        assertEquals(dune.chiaveIdentita, chiave)
    }

    @Test
    fun `without anything to resume the focus goes to the Contenuto di default`() {
        val righeSenzaContinua = listOf(
            RigaDashboard(TipoRiga.CONTINUA, emptyList()),
            RigaDashboard(TipoRiga.PREFERITI, listOf(simpson))
        )

        val chiave = memoria.focusIniziale(righeSenzaContinua, contenutoDiDefault = simpson.chiaveIdentita)

        assertEquals(simpson.chiaveIdentita, chiave)
    }

    @Test
    fun `with nothing to resume and no default the focus goes to the first content of the first row`() {
        val righeSenzaContinua = listOf(
            RigaDashboard(TipoRiga.CONTINUA, emptyList()),
            RigaDashboard(TipoRiga.PREFERITI, listOf(simpson))
        )

        val chiave = memoria.focusIniziale(righeSenzaContinua, contenutoDiDefault = null)

        assertEquals(simpson.chiaveIdentita, chiave)
    }

    @Test
    fun `a Contenuto di default that is no longer in the catalog falls back to the first content`() {
        val righeSenzaContinua = listOf(
            RigaDashboard(TipoRiga.CONTINUA, emptyList()),
            RigaDashboard(TipoRiga.PREFERITI, listOf(simpson))
        )

        val chiave = memoria.focusIniziale(righeSenzaContinua, contenutoDiDefault = "serie:Sparita")

        assertEquals(simpson.chiaveIdentita, chiave)
    }

    @Test
    fun `with no rows there is nothing to focus`() {
        assertNull(memoria.focusIniziale(emptyList(), contenutoDiDefault = null))
    }
}
