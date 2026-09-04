package com.ir0.iptv.app.webpanel

import android.content.Context
import com.ir0.iptv.domain.source.Sorgente
import com.ir0.iptv.domain.source.SorgenteFactory
import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.util.concurrent.atomic.AtomicBoolean

object WebPanelServer {
    const val PORT = 8080

    private val started = AtomicBoolean(false)

    fun start(context: Context) {
        if (!started.compareAndSet(false, true)) return
        val repository = SorgenteRepository(context)
        val factory = SorgenteFactory()

        embeddedServer(CIO, port = PORT, host = "0.0.0.0") {
            routing {
                get("/") {
                    call.respondText(sourcesPage(repository.elenco()), ContentType.Text.Html)
                }
                get("/sorgenti/nuova") {
                    call.respondText(sourceFormPage(), ContentType.Text.Html)
                }
                post("/sorgenti") {
                    val params = call.receiveParameters()
                    try {
                        repository.aggiungi(factory.creaDaParametri(params))
                        call.respondRedirect("/")
                    } catch (e: IllegalArgumentException) {
                        call.respondText(sourceFormPage(errore = e.message), ContentType.Text.Html)
                    }
                }
                get("/sorgenti/{id}/modifica") {
                    val sorgente = repository.trova(call.parameters["id"].orEmpty())
                    if (sorgente == null) {
                        call.respondRedirect("/")
                    } else {
                        call.respondText(sourceFormPage(sorgente = sorgente), ContentType.Text.Html)
                    }
                }
                post("/sorgenti/{id}") {
                    val id = call.parameters["id"].orEmpty()
                    val esistente = repository.trova(id)
                    val params = call.receiveParameters()
                    try {
                        repository.aggiorna(factory.modificaDaParametri(id, params, esistente))
                        call.respondRedirect("/")
                    } catch (e: IllegalArgumentException) {
                        call.respondText(
                            sourceFormPage(sorgente = esistente, errore = e.message),
                            ContentType.Text.Html
                        )
                    }
                }
                post("/sorgenti/{id}/elimina") {
                    repository.rimuovi(call.parameters["id"].orEmpty())
                    call.respondRedirect("/")
                }
            }
        }.start(wait = false)
    }
}

private fun SorgenteFactory.creaDaParametri(params: io.ktor.http.Parameters): Sorgente = when (params["tipo"]) {
    "m3u" -> creaM3u(nome = params["nome"].orEmpty(), url = params["url"].orEmpty())
    "xtream" -> creaXtream(
        nome = params["nome"].orEmpty(),
        host = params["host"].orEmpty(),
        port = params["port"]?.toIntOrNull() ?: 0,
        username = params["username"].orEmpty(),
        password = params["password"].orEmpty()
    )

    else -> throw IllegalArgumentException("Tipo di Sorgente sconosciuto")
}

private fun SorgenteFactory.modificaDaParametri(id: String, params: io.ktor.http.Parameters, esistente: Sorgente?): Sorgente =
    when (params["tipo"]) {
        "m3u" -> modificaM3u(id = id, nome = params["nome"].orEmpty(), url = params["url"].orEmpty())
        "xtream" -> {
            val passwordInserita = params["password"].orEmpty()
            val password = passwordInserita.ifBlank { (esistente as? Sorgente.Xtream)?.connection?.password.orEmpty() }
            modificaXtream(
                id = id,
                nome = params["nome"].orEmpty(),
                host = params["host"].orEmpty(),
                port = params["port"]?.toIntOrNull() ?: 0,
                username = params["username"].orEmpty(),
                password = password
            )
        }

        else -> throw IllegalArgumentException("Tipo di Sorgente sconosciuto")
    }

private fun pageShell(content: String): String = """
    <!doctype html>
    <html lang="it">
    <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1">
      <title>1r0 IPTV - Pannello Web</title>
      <style>
        * { box-sizing: border-box; }
        body { margin: 0; font-family: system-ui, sans-serif; background: #f5f5f4; color: #1c1c1a; }
        a { color: #b45309; text-decoration: none; }
        table, thead, tbody, tr, th, td { display: block; width: 100%; }
        table { border-collapse: collapse; }
        thead { display: none; }
        tbody tr { margin-bottom: 12px; border: 1px solid #e5e5e2; border-radius: 8px; overflow: hidden; background: #fff; }
        td { padding: 10px 14px; border-bottom: 1px solid #eeeeeb; font-size: 14px; display: flex; justify-content: space-between; align-items: center; gap: 12px; text-align: right; }
        td:last-child { border-bottom: none; }
        td::before { content: attr(data-label); font-weight: 600; color: #6b6b66; font-size: 11px; text-transform: uppercase; letter-spacing: 0.03em; text-align: left; }
        .header { display: flex; align-items: center; padding: 14px 16px; border-bottom: 1px solid #e5e5e2; background: #fff; }
        .brand { font-size: 15px; font-weight: 700; }
        .content { padding: 16px; max-width: 720px; margin: 0 auto; }
        .btn { padding: 9px 14px; border-radius: 7px; font-size: 13px; font-weight: 600; cursor: pointer; border: none; display: inline-block; }
        .btn-primary { background: #b45309; color: #fff; }
        .btn-ghost { background: #fff; color: #1c1c1a; border: 1px solid #d9d9d5; }
        .btn-danger { background: #fff; color: #b91c1c; border: 1px solid #f3c6c6; }
        .list-header { display: flex; flex-direction: column; gap: 12px; margin-bottom: 16px; }
        .field-label { font-size: 13px; font-weight: 600; color: #3a3a36; margin-bottom: 6px; }
        .field-input { width: 100%; padding: 10px 12px; border-radius: 7px; border: 1px solid #d9d9d5; font-size: 14px; margin-bottom: 16px; }
        .type-opts { display: flex; flex-direction: column; gap: 12px; margin-bottom: 20px; }
        .type-opt { flex: 1; padding: 14px; border-radius: 9px; border: 2px solid #e5e5e2; cursor: pointer; }
        .type-opt.active { border-color: #b45309; background: #fdf3ea; }
        .host-row { display: flex; flex-direction: column; gap: 0; }
        .error { background: #fee2e2; color: #991b1b; padding: 10px 14px; border-radius: 7px; margin-bottom: 16px; font-size: 13px; }
        .actions { display: flex; gap: 8px; justify-content: flex-end; }
        .actions form { margin: 0; }
        @media (min-width: 640px) {
          .content { padding: 28px 32px; }
          .brand { font-size: 16px; }
          table, thead, tbody, tr, th, td { display: revert; width: revert; }
          table { width: 100%; }
          thead { display: table-header-group; }
          th { text-align: left; font-size: 12px; text-transform: uppercase; letter-spacing: 0.04em; color: #6b6b66; padding: 10px 14px; border-bottom: 1px solid #e5e5e2; }
          tbody tr { margin-bottom: 0; border: none; border-radius: 0; background: transparent; }
          td { display: table-cell; text-align: left; padding: 14px; }
          td::before { content: none; }
          .list-header { flex-direction: row; align-items: center; justify-content: space-between; }
          .type-opts { flex-direction: row; }
          .host-row { flex-direction: row; gap: 14px; }
          .host-row .field-port { flex: 1; }
          .host-row .field-username { flex: 2; }
        }
      </style>
    </head>
    <body>
      <div class="header"><div class="brand">1r0 IPTV &middot; Pannello Web</div></div>
      <div class="content">$content</div>
      <script>
        function selectTipo(tipo) {
          document.getElementById('campi-m3u').style.display = tipo === 'm3u' ? 'block' : 'none';
          document.getElementById('campi-xtream').style.display = tipo === 'xtream' ? 'block' : 'none';
          document.getElementById('tipo').value = tipo;
          document.getElementById('opt-m3u').classList.toggle('active', tipo === 'm3u');
          document.getElementById('opt-xtream').classList.toggle('active', tipo === 'xtream');
        }
      </script>
    </body>
    </html>
""".trimIndent()

private fun sourcesPage(sorgenti: List<Sorgente>): String {
    val righe = if (sorgenti.isEmpty()) {
        """<tr><td style="justify-content: center;">Nessuna Sorgente configurata</td></tr>"""
    } else {
        sorgenti.joinToString(separator = "") { sorgente ->
            val tipo = if (sorgente is Sorgente.M3u) "M3U" else "Xtream Codes"
            val dettagli = when (sorgente) {
                is Sorgente.M3u -> sorgente.url
                is Sorgente.Xtream -> "host: ${sorgente.connection.host}:${sorgente.connection.port}"
            }
            """
            <tr>
              <td data-label="Nome" style="font-weight: 600;">${sorgente.nome.escapeHtml()}</td>
              <td data-label="Tipo">$tipo</td>
              <td data-label="Dettagli" style="color: #6b6b66; font-family: ui-monospace, monospace; font-size: 12.5px;">${dettagli.escapeHtml()}</td>
              <td data-label="Azioni">
                <div class="actions">
                  <a class="btn btn-ghost" href="/sorgenti/${sorgente.id}/modifica">Modifica</a>
                  <form method="post" action="/sorgenti/${sorgente.id}/elimina" onsubmit="return confirm('Rimuovere questa Sorgente?')">
                    <button class="btn btn-danger" type="submit">Rimuovi</button>
                  </form>
                </div>
              </td>
            </tr>
            """.trimIndent()
        }
    }
    return pageShell(
        """
        <div class="list-header">
          <div>
            <div style="font-size: 20px; font-weight: 700;">Sorgenti</div>
            <div style="font-size: 13px; color: #6b6b66;">Playlist M3U e account Xtream Codes configurati su questa Android TV</div>
          </div>
          <a class="btn btn-primary" href="/sorgenti/nuova">+ Aggiungi Sorgente</a>
        </div>
        <table>
          <thead><tr><th>Nome</th><th>Tipo</th><th>Dettagli</th><th></th></tr></thead>
          <tbody>$righe</tbody>
        </table>
        """.trimIndent()
    )
}

private fun sourceFormPage(sorgente: Sorgente? = null, errore: String? = null): String {
    val erroreHtml = errore?.let { """<div class="error">${it.escapeHtml()}</div>""" }.orEmpty()
    val isM3u = sorgente is Sorgente.M3u
    val azione = if (sorgente != null) "/sorgenti/${sorgente.id}" else "/sorgenti"
    val titolo = if (sorgente != null) "Modifica Sorgente" else "Aggiungi Sorgente"
    val nome = (sorgente?.nome).orEmpty()
    val url = (sorgente as? Sorgente.M3u)?.url.orEmpty()
    val host = (sorgente as? Sorgente.Xtream)?.connection?.host.orEmpty()
    val port = (sorgente as? Sorgente.Xtream)?.connection?.port?.toString() ?: "8080"
    val username = (sorgente as? Sorgente.Xtream)?.connection?.username.orEmpty()

    return pageShell(
        """
        <div style="max-width: 560px; margin: 0 auto;">
          <div style="margin-bottom: 20px;">
            <div style="font-size: 20px; font-weight: 700;">$titolo</div>
            <div style="font-size: 13px; color: #6b6b66;">Playlist M3U o account Xtream Codes</div>
          </div>
          $erroreHtml
          <form method="post" action="$azione">
            <input type="hidden" id="tipo" name="tipo" value="${if (isM3u) "m3u" else "xtream"}">
            <div class="type-opts">
              <div class="type-opt ${if (isM3u) "active" else ""}" id="opt-m3u" onclick="selectTipo('m3u')">
                <div style="font-weight: 700; font-size: 14px;">Playlist M3U</div>
                <div style="font-size: 12px; color: #6b6b66;">Un link a un file .m3u / .m3u8</div>
              </div>
              <div class="type-opt ${if (!isM3u) "active" else ""}" id="opt-xtream" onclick="selectTipo('xtream')">
                <div style="font-weight: 700; font-size: 14px;">Xtream Codes</div>
                <div style="font-size: 12px; color: #6b6b66;">Host, username e password del provider</div>
              </div>
            </div>
            <div class="field-label">Nome Sorgente</div>
            <input class="field-input" name="nome" placeholder="es. Provider Principale" value="${nome.escapeHtml()}" required>
            <div id="campi-m3u" style="display: ${if (isM3u) "block" else "none"};">
              <div class="field-label">URL Playlist</div>
              <input class="field-input" name="url" placeholder="https://esempio.tv/lista.m3u8" value="${url.escapeHtml()}">
            </div>
            <div id="campi-xtream" style="display: ${if (isM3u) "none" else "block"};">
              <div class="field-label">Host</div>
              <input class="field-input" name="host" placeholder="iptv.provider.example" value="${host.escapeHtml()}">
              <div class="host-row">
                <div class="field-port">
                  <div class="field-label">Porta</div>
                  <input class="field-input" name="port" placeholder="8080" value="${port.escapeHtml()}">
                </div>
                <div class="field-username">
                  <div class="field-label">Username</div>
                  <input class="field-input" name="username" placeholder="username" value="${username.escapeHtml()}">
                </div>
              </div>
              <div class="field-label">Password</div>
              <input class="field-input" name="password" type="password" placeholder="********">
            </div>
            <div style="display: flex; gap: 10px; justify-content: flex-end;">
              <a class="btn btn-ghost" href="/">Annulla</a>
              <button class="btn btn-primary" type="submit">Salva Sorgente</button>
            </div>
          </form>
        </div>
        """.trimIndent()
    )
}

private fun String.escapeHtml(): String = replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
    .replace("\"", "&quot;")
