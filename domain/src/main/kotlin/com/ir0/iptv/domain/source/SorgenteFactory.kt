package com.ir0.iptv.domain.source

import com.ir0.iptv.domain.source.xtream.XtreamConnection
import java.util.UUID

class SorgenteFactory(
    private val newId: () -> String = { UUID.randomUUID().toString() }
) {

    fun creaM3u(nome: String, url: String): Sorgente.M3u = m3u(newId(), nome, url)

    fun modificaM3u(id: String, nome: String, url: String): Sorgente.M3u = m3u(id, nome, url)

    fun creaXtream(nome: String, host: String, port: Int, username: String, password: String): Sorgente.Xtream =
        xtream(newId(), nome, host, port, username, password)

    fun modificaXtream(id: String, nome: String, host: String, port: Int, username: String, password: String): Sorgente.Xtream =
        xtream(id, nome, host, port, username, password)

    private fun m3u(id: String, nome: String, url: String): Sorgente.M3u {
        require(nome.isNotBlank()) { "Il nome della Sorgente non può essere vuoto" }
        require(url.startsWith("http://") || url.startsWith("https://")) {
            "L'URL della playlist deve iniziare con http:// o https://"
        }
        return Sorgente.M3u(id = id, nome = nome.trim(), url = url.trim())
    }

    private fun xtream(id: String, nome: String, host: String, port: Int, username: String, password: String): Sorgente.Xtream {
        require(nome.isNotBlank()) { "Il nome della Sorgente non può essere vuoto" }
        require(host.isNotBlank()) { "L'host non può essere vuoto" }
        require(port in 1..65535) { "La porta deve essere compresa tra 1 e 65535" }
        require(username.isNotBlank()) { "Lo username non può essere vuoto" }
        require(password.isNotBlank()) { "La password non può essere vuota" }
        return Sorgente.Xtream(
            id = id,
            nome = nome.trim(),
            connection = XtreamConnection(
                host = host.trim(),
                port = port,
                username = username.trim(),
                password = password
            )
        )
    }
}
