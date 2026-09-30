package com.ir0.iptv.domain.catalog

import com.ir0.iptv.domain.customization.ContentCustomization
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RiconciliazionePersonalizzazioniTest {

    private val riconciliazione = RiconciliazionePersonalizzazioni()

    @Test
    fun `leaves a customization untouched when its Chiave still exists`() {
        val rai1 = ContentCard.Canale("Rai 1", null, "http://nuovo.example/rai1.m3u8")
        val personalizzazioni = mapOf(rai1.chiaveIdentita to ContentCustomization(favorite = true, titolo = "Rai 1"))

        val risultato = riconciliazione.riconcilia(personalizzazioni, ContentCatalog(canali = listOf(rai1)))

        assertEquals(personalizzazioni, risultato)
    }

    @Test
    fun `reattaches a favorite to its new Chiave when the old one is gone but the title uniquely matches`() {
        val rai1Nuovo = ContentCard.Canale("Rai 1", null, "http://nuovo.example/rai1.m3u8")
        val personalizzazioni = mapOf(
            "http://vecchio.example/rai1.m3u8" to ContentCustomization(favorite = true, titolo = "Rai 1")
        )

        val risultato = riconciliazione.riconcilia(personalizzazioni, ContentCatalog(canali = listOf(rai1Nuovo)))

        assertTrue(risultato.containsKey(rai1Nuovo.chiaveIdentita))
        assertEquals(true, risultato[rai1Nuovo.chiaveIdentita]?.favorite)
        assertFalse(risultato.containsKey("http://vecchio.example/rai1.m3u8"))
    }

    @Test
    fun `does not reattach when two contents share the same title (ambiguous, fails closed)`() {
        val unoDeiDue = ContentCard.Canale("Sport HD", null, "http://nuovo.example/sport-1.m3u8")
        val altroDeiDue = ContentCard.Canale("Sport HD", null, "http://nuovo.example/sport-2.m3u8")
        val personalizzazioni = mapOf(
            "http://vecchio.example/sport.m3u8" to ContentCustomization(favorite = true, titolo = "Sport HD")
        )

        val risultato = riconciliazione.riconcilia(
            personalizzazioni,
            ContentCatalog(canali = listOf(unoDeiDue, altroDeiDue))
        )

        assertTrue(risultato.containsKey("http://vecchio.example/sport.m3u8"))
    }

    @Test
    fun `a customization with no stored title (created before this fix) cannot be reattached`() {
        val rai1Nuovo = ContentCard.Canale("Rai 1", null, "http://nuovo.example/rai1.m3u8")
        val personalizzazioni = mapOf(
            "http://vecchio.example/rai1.m3u8" to ContentCustomization(favorite = true)
        )

        val risultato = riconciliazione.riconcilia(personalizzazioni, ContentCatalog(canali = listOf(rai1Nuovo)))

        assertTrue(risultato.containsKey("http://vecchio.example/rai1.m3u8"))
    }
}
