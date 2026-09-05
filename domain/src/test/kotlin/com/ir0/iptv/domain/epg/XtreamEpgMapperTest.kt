package com.ir0.iptv.domain.epg

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class XtreamEpgMapperTest {

    private val mapper = XtreamEpgMapper()

    @Test
    fun `maps an EPG listing into a Programma`() {
        val dto = XtreamEpgListingDto(
            titolo = "Tg1",
            descrizione = "Le notizie del giorno",
            inizioSecondi = 1_700_000_000,
            fineSecondi = 1_700_001_800
        )

        val programmi = mapper.toProgrammi(listOf(dto))

        assertEquals(
            listOf(
                Programma(
                    titolo = "Tg1",
                    descrizione = "Le notizie del giorno",
                    inizioMs = 1_700_000_000_000,
                    fineMs = 1_700_001_800_000
                )
            ),
            programmi
        )
    }

    @Test
    fun `listings come back sorted by start time whatever order the provider used`() {
        val programmi = mapper.toProgrammi(
            listOf(
                XtreamEpgListingDto("Notte", null, 300, 400),
                XtreamEpgListingDto("Tg1", null, 100, 200)
            )
        )

        assertEquals(listOf("Tg1", "Notte"), programmi.map { it.titolo })
    }

    @Test
    fun `a listing that ends before it starts is dropped instead of breaking the guide`() {
        val programmi = mapper.toProgrammi(listOf(XtreamEpgListingDto("Rotto", null, 400, 100)))

        assertTrue(programmi.isEmpty())
    }

    @Test
    fun `a listing without a title is dropped`() {
        val programmi = mapper.toProgrammi(listOf(XtreamEpgListingDto("   ", null, 100, 200)))

        assertTrue(programmi.isEmpty())
    }
}
