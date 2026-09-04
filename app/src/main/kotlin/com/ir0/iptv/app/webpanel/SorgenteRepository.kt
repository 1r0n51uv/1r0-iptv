package com.ir0.iptv.app.webpanel

import android.content.Context
import com.ir0.iptv.domain.source.Sorgente
import com.ir0.iptv.domain.source.xtream.XtreamConnection
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

class SorgenteRepository(context: Context) {
    private val file = File(context.applicationContext.filesDir, "sorgenti.json")

    @Synchronized
    fun elenco(): List<Sorgente> {
        if (!file.exists()) return emptyList()
        val array = JSONArray(file.readText())
        return (0 until array.length()).map { array.getJSONObject(it).toSorgente() }
    }

    @Synchronized
    fun trova(id: String): Sorgente? = elenco().firstOrNull { it.id == id }

    @Synchronized
    fun aggiungi(sorgente: Sorgente) {
        salva(elenco() + sorgente)
    }

    @Synchronized
    fun aggiorna(sorgente: Sorgente) {
        salva(elenco().map { if (it.id == sorgente.id) sorgente else it })
    }

    @Synchronized
    fun rimuovi(id: String) {
        salva(elenco().filterNot { it.id == id })
    }

    private fun salva(sorgenti: List<Sorgente>) {
        file.writeText(JSONArray(sorgenti.map { it.toJson() }).toString())
    }
}

private fun Sorgente.toJson(): JSONObject = when (this) {
    is Sorgente.M3u -> JSONObject()
        .put("id", id)
        .put("nome", nome)
        .put("tipo", "m3u")
        .put("url", url)

    is Sorgente.Xtream -> JSONObject()
        .put("id", id)
        .put("nome", nome)
        .put("tipo", "xtream")
        .put("host", connection.host)
        .put("port", connection.port)
        .put("username", connection.username)
        .put("password", connection.password)
}

private fun JSONObject.toSorgente(): Sorgente = when (getString("tipo")) {
    "m3u" -> Sorgente.M3u(id = getString("id"), nome = getString("nome"), url = getString("url"))
    else -> Sorgente.Xtream(
        id = getString("id"),
        nome = getString("nome"),
        connection = XtreamConnection(
            host = getString("host"),
            port = getInt("port"),
            username = getString("username"),
            password = getString("password")
        )
    )
}
