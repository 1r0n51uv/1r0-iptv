package com.ir0.iptv.domain.sport

import com.ir0.iptv.domain.catalog.ContentCard
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SelettorePartiteTest {

    private val selettore = SelettorePartite()

    @Test
    fun `matches already being played come before the ones yet to start`() {
        val daGiocare = partita("Roma", "Lazio", inCorso = false, inizioMs = 5_000)
        val inCorso = partita("Inter", "Milan", inCorso = true, inizioMs = 1_000)

        val evidenza = selettore.inEvidenza(listOf(daGiocare, inCorso))

        assertEquals(listOf(inCorso, daGiocare), evidenza)
    }

    @Test
    fun `only two matches make it into the spotlight`() {
        val partite = (1..5).map { partita("Casa $it", "Ospite $it", inCorso = true, inizioMs = it.toLong()) }

        assertEquals(2, selettore.inEvidenza(partite).size)
    }

    @Test
    fun `among the ones being played the most recently started comes first`() {
        val presto = partita("Inter", "Milan", inCorso = true, inizioMs = 1_000)
        val tardi = partita("Roma", "Lazio", inCorso = true, inizioMs = 3_000)

        assertEquals(listOf(tardi, presto), selettore.inEvidenza(listOf(presto, tardi)))
    }

    @Test
    fun `among the ones yet to start the closest kickoff comes first`() {
        val tardi = partita("Roma", "Lazio", inCorso = false, inizioMs = 9_000)
        val presto = partita("Inter", "Milan", inCorso = false, inizioMs = 4_000)

        assertEquals(listOf(presto, tardi), selettore.inEvidenza(listOf(tardi, presto)))
    }

    @Test
    fun `with no match there is nothing to put in the spotlight`() {
        assertTrue(selettore.inEvidenza(emptyList()).isEmpty())
    }

    @Test
    fun `a match is tied to the event Canale that names both teams`() {
        val evento = canale("Serie A: Inter - Milan")
        val canali = listOf(canale("Rai 1"), evento)

        assertEquals(evento, selettore.canalePer(partita("Inter", "Milan"), canali))
    }

    @Test
    fun `naming only one of the two teams is not enough to tie a Canale to the match`() {
        val canali = listOf(canale("Inter TV"))

        assertNull(selettore.canalePer(partita("Inter", "Milan"), canali))
    }

    @Test
    fun `tying a Canale to a match ignores case`() {
        val evento = canale("INTER VS MILAN")

        assertEquals(evento, selettore.canalePer(partita("Inter", "Milan"), listOf(evento)))
    }

    @Test
    fun `without a matching Canale there is nothing to open, and that is fine`() {
        assertNull(selettore.canalePer(partita("Inter", "Milan"), listOf(canale("Rai 1"))))
    }
}

private fun partita(
    casa: String,
    ospite: String,
    inCorso: Boolean = true,
    inizioMs: Long = 0
) = PartitaLive(
    casa = casa,
    ospite = ospite,
    competizione = "Serie A",
    golCasa = null,
    golOspite = null,
    inCorso = inCorso,
    inizioMs = inizioMs
)

private fun canale(titolo: String) = ContentCard.Canale(titolo, null, "http://example.com/${titolo}.m3u8")
