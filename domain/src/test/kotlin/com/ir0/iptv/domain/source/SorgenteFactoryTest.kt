package com.ir0.iptv.domain.source

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SorgenteFactoryTest {

    private val factory = SorgenteFactory(newId = { "id-fisso" })

    @Test
    fun `creaM3u builds a M3u Sorgente with trimmed nome and url`() {
        val sorgente = factory.creaM3u(nome = "  Provider Principale  ", url = "https://esempio.tv/lista.m3u8")

        assertEquals(Sorgente.M3u(id = "id-fisso", nome = "Provider Principale", url = "https://esempio.tv/lista.m3u8"), sorgente)
    }

    @Test
    fun `creaM3u rejects a blank nome`() {
        assertThrows(IllegalArgumentException::class.java) {
            factory.creaM3u(nome = "  ", url = "https://esempio.tv/lista.m3u8")
        }
    }

    @Test
    fun `creaM3u rejects a url without a http or https scheme`() {
        assertThrows(IllegalArgumentException::class.java) {
            factory.creaM3u(nome = "Provider", url = "esempio.tv/lista.m3u8")
        }
    }

    @Test
    fun `creaXtream builds a Xtream Sorgente with trimmed fields`() {
        val sorgente = factory.creaXtream(
            nome = "  Provider Xtream  ",
            host = "  iptv.provider.example  ",
            port = 8080,
            username = "  utente  ",
            password = "segreta"
        )

        assertEquals(
            Sorgente.Xtream(
                id = "id-fisso",
                nome = "Provider Xtream",
                connection = com.ir0.iptv.domain.source.xtream.XtreamConnection(
                    host = "iptv.provider.example",
                    port = 8080,
                    username = "utente",
                    password = "segreta"
                )
            ),
            sorgente
        )
    }

    @Test
    fun `creaXtream rejects a blank host`() {
        assertThrows(IllegalArgumentException::class.java) {
            factory.creaXtream(nome = "Provider", host = " ", port = 8080, username = "u", password = "p")
        }
    }

    @Test
    fun `creaXtream rejects a port outside the valid range`() {
        assertThrows(IllegalArgumentException::class.java) {
            factory.creaXtream(nome = "Provider", host = "host", port = 0, username = "u", password = "p")
        }
        assertThrows(IllegalArgumentException::class.java) {
            factory.creaXtream(nome = "Provider", host = "host", port = 70000, username = "u", password = "p")
        }
    }

    @Test
    fun `creaXtream rejects a blank username or password`() {
        assertThrows(IllegalArgumentException::class.java) {
            factory.creaXtream(nome = "Provider", host = "host", port = 8080, username = " ", password = "p")
        }
        assertThrows(IllegalArgumentException::class.java) {
            factory.creaXtream(nome = "Provider", host = "host", port = 8080, username = "u", password = " ")
        }
    }

    @Test
    fun `modificaM3u keeps the given id instead of generating a new one`() {
        val sorgente = factory.modificaM3u(id = "id-esistente", nome = "Rinominata", url = "https://esempio.tv/nuova.m3u8")

        assertEquals(Sorgente.M3u(id = "id-esistente", nome = "Rinominata", url = "https://esempio.tv/nuova.m3u8"), sorgente)
    }

    @Test
    fun `modificaM3u applies the same validation as creaM3u`() {
        assertThrows(IllegalArgumentException::class.java) {
            factory.modificaM3u(id = "id-esistente", nome = "Rinominata", url = "senza-schema.m3u8")
        }
    }

    @Test
    fun `modificaXtream keeps the given id instead of generating a new one`() {
        val sorgente = factory.modificaXtream(
            id = "id-esistente",
            nome = "Rinominata",
            host = "nuovo.host.example",
            port = 25461,
            username = "utente",
            password = "segreta"
        )

        assertEquals(
            Sorgente.Xtream(
                id = "id-esistente",
                nome = "Rinominata",
                connection = com.ir0.iptv.domain.source.xtream.XtreamConnection(
                    host = "nuovo.host.example",
                    port = 25461,
                    username = "utente",
                    password = "segreta"
                )
            ),
            sorgente
        )
    }

    @Test
    fun `modificaXtream applies the same validation as creaXtream`() {
        assertThrows(IllegalArgumentException::class.java) {
            factory.modificaXtream(id = "id-esistente", nome = "Rinominata", host = " ", port = 8080, username = "u", password = "p")
        }
    }
}
