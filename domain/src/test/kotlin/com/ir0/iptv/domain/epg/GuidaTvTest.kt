package com.ir0.iptv.domain.epg

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GuidaTvTest {

    private val guida = GuidaTv()

    private val tg = Programma("Tg1", inizioMs = 1_000, fineMs = 2_000)
    private val film = Programma("Film della sera", inizioMs = 2_000, fineMs = 5_000)
    private val notte = Programma("Notte", inizioMs = 5_000, fineMs = 6_000)
    private val palinsesto = listOf(film, tg, notte)

    @Test
    fun `the Programma on air is the one the current time falls into`() {
        assertEquals(film, guida.inOnda(palinsesto, ora = 3_000))
    }

    @Test
    fun `a Programma is on air from its start, and over at its end`() {
        assertEquals(film, guida.inOnda(palinsesto, ora = 2_000))
        assertEquals(notte, guida.inOnda(palinsesto, ora = 5_000))
    }

    @Test
    fun `outside the palinsesto nothing is on air`() {
        assertNull(guida.inOnda(palinsesto, ora = 500))
        assertNull(guida.inOnda(palinsesto, ora = 9_000))
        assertNull(guida.inOnda(emptyList(), ora = 3_000))
    }

    @Test
    fun `a hole in the palinsesto leaves nothing on air`() {
        val conBuco = listOf(tg, notte)

        assertNull(guida.inOnda(conBuco, ora = 3_000))
    }

    @Test
    fun `what comes next is listed in order of start time`() {
        assertEquals(listOf(notte), guida.prossimi(palinsesto, ora = 3_000))
        assertEquals(listOf(film, notte), guida.prossimi(palinsesto, ora = 1_500))
    }

    @Test
    fun `nothing follows the last Programma`() {
        assertTrue(guida.prossimi(palinsesto, ora = 5_500).isEmpty())
    }

    @Test
    fun `the progress of the Programma on air is how far into it we are`() {
        assertEquals(0, guida.percentuale(film, ora = 2_000))
        assertEquals(50, guida.percentuale(film, ora = 3_500))
        assertEquals(100, guida.percentuale(film, ora = 5_000))
    }

    @Test
    fun `a Programma with no duration has no progress instead of dividing by zero`() {
        val istantaneo = Programma("Rotto", inizioMs = 2_000, fineMs = 2_000)

        assertEquals(0, guida.percentuale(istantaneo, ora = 2_000))
    }
}
