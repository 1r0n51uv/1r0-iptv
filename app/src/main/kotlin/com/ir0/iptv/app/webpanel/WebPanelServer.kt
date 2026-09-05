package com.ir0.iptv.app.webpanel

import android.content.Context
import com.ir0.iptv.app.settings.Impostazioni
import com.ir0.iptv.app.settings.ImpostazioniRepository
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.catalog.RicercaCatalogo
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
        val impostazioniRepository = ImpostazioniRepository(context)
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
                get("/impostazioni") {
                    call.respondText(
                        impostazioniPage(impostazioniRepository.leggi()),
                        ContentType.Text.Html
                    )
                }
                post("/impostazioni") {
                    val params = call.receiveParameters()
                    val correnti = impostazioniRepository.leggi()
                    impostazioniRepository.salva(
                        Impostazioni(
                            contenutoDiDefault = params["contenutoDiDefault"]?.ifBlank { null },
                            // Un campo chiave lasciato vuoto non cancella quella gia' salvata:
                            // la pagina non la ristampa mai per non esporla.
                            chiaveApiAi = params["chiaveApiAi"]?.ifBlank { null } ?: correnti.chiaveApiAi,
                            chiaveApiSport = params["chiaveApiSport"]?.ifBlank { null } ?: correnti.chiaveApiSport,
                            sportInDashboard = params["sportInDashboard"] == "on",
                            accento = params["accento"]?.ifBlank { null } ?: correnti.accento
                        )
                    )
                    call.respondRedirect("/impostazioni")
                }
                get("/riproduci") {
                    val query = call.request.queryParameters["q"].orEmpty()
                    call.respondText(
                        riproduciPage(PonteTv.catalogo.value, query),
                        ContentType.Text.Html
                    )
                }
                post("/riproduci") {
                    val params = call.receiveParameters()
                    val trovato = PonteTv.chiediApertura(params["chiave"].orEmpty())
                    val query = params["q"].orEmpty()
                    call.respondText(
                        riproduciPage(
                            catalogo = PonteTv.catalogo.value,
                            query = query,
                            esito = if (trovato) {
                                "Aperto sulla TV."
                            } else {
                                "Contenuto non più nel catalogo caricato sulla TV."
                            }
                        ),
                        ContentType.Text.Html
                    )
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
          <div class="actions">
            <a class="btn btn-ghost" href="/impostazioni">Impostazioni</a>
            <a class="btn btn-ghost" href="/riproduci">Riproduci sulla TV</a>
            <a class="btn btn-primary" href="/sorgenti/nuova">+ Aggiungi Sorgente</a>
          </div>
        </div>
        <table>
          <thead><tr><th>Nome</th><th>Tipo</th><th>Dettagli</th><th></th></tr></thead>
          <tbody>$righe</tbody>
        </table>
        """.trimIndent()
    )
}

private fun impostazioniPage(impostazioni: Impostazioni): String {
    val statoAi = if (impostazioni.chiaveApiAi.isNullOrBlank()) "nessuna chiave salvata" else "chiave salvata"
    val statoSport = if (impostazioni.chiaveApiSport.isNullOrBlank()) "nessuna chiave salvata" else "chiave salvata"
    val accenti = listOf("AMBRA" to "Ambra", "BLU" to "Blu", "VERDE" to "Verde", "ROSA" to "Rosa")
    val opzioniAccento = accenti.joinToString(separator = "") { (valore, etichetta) ->
        val scelto = if ((impostazioni.accento ?: "AMBRA") == valore) "selected" else ""
        """<option value="$valore" $scelto>$etichetta</option>"""
    }
    return pageShell(
        """
        <div class="list-header">
          <div>
            <div style="font-size: 20px; font-weight: 700;">Impostazioni</div>
            <div style="font-size: 13px; color: #6b6b66;">Valgono per l'app sulla Android TV</div>
          </div>
          <a class="btn btn-ghost" href="/">Sorgenti</a>
        </div>
        <form method="post" action="/impostazioni">
          <div class="field-label">Chiave API Claude (Suggeriti)</div>
          <input class="field-input" name="chiaveApiAi" type="password" placeholder="$statoAi">
          <div class="field-label">Chiave API football-data.org (Sport)</div>
          <input class="field-input" name="chiaveApiSport" type="password" placeholder="$statoSport">
          <div class="field-label">Sport in diretta in Dashboard</div>
          <label style="display:flex;align-items:center;gap:8px;margin-bottom:16px;font-size:14px;">
            <input type="checkbox" name="sportInDashboard" ${if (impostazioni.sportInDashboard) "checked" else ""}>
            Mostra due partite in evidenza
          </label>
          <div class="field-label">Contenuto di default (Chiave di Identita')</div>
          <input class="field-input" name="contenutoDiDefault" placeholder="lasciare vuoto per usare l'ultimo aperto" value="${(impostazioni.contenutoDiDefault ?: "").escapeHtml()}">
          <div class="field-label">Colore di accento</div>
          <select class="field-input" name="accento">$opzioniAccento</select>
          <div style="display: flex; gap: 10px; justify-content: flex-end;">
            <a class="btn btn-ghost" href="/">Annulla</a>
            <button class="btn btn-primary" type="submit">Salva Impostazioni</button>
          </div>
        </form>
        <div style="font-size:12px;color:#6b6b66;margin-top:14px;">
          Le chiavi restano su questa TV e non vengono mai ristampate qui: lasciando il campo vuoto
          si tiene quella gia' salvata.
        </div>
        """.trimIndent()
    )
}

private const val MAX_RISULTATI_RIPRODUCI = 100

private fun riproduciPage(catalogo: ContentCatalog, query: String, esito: String? = null): String {
    val risultati = if (query.isBlank()) {
        catalogo.tutti.take(MAX_RISULTATI_RIPRODUCI)
    } else {
        RicercaCatalogo().cerca(catalogo, query).take(MAX_RISULTATI_RIPRODUCI)
    }
    val esitoHtml = esito?.let { """<div class="error" style="background:#ecfdf5;color:#065f46;">${it.escapeHtml()}</div>""" }.orEmpty()
    val righe = if (catalogo.tutti.isEmpty()) {
        """<tr><td style="justify-content: center;">La TV non ha ancora caricato il catalogo</td></tr>"""
    } else if (risultati.isEmpty()) {
        """<tr><td style="justify-content: center;">Nessun risultato</td></tr>"""
    } else {
        risultati.joinToString(separator = "") { card ->
            val tipo = when (card) {
                is ContentCard.Canale -> "Canale"
                is ContentCard.Film -> "Film"
                is ContentCard.SerieCard -> "Serie"
            }
            """
            <tr>
              <td data-label="Titolo" style="font-weight: 600;">${card.title.escapeHtml()}</td>
              <td data-label="Tipo">$tipo</td>
              <td data-label="Azioni">
                <div class="actions">
                  <form method="post" action="/riproduci">
                    <input type="hidden" name="chiave" value="${card.chiaveIdentita.escapeHtml()}">
                    <input type="hidden" name="q" value="${query.escapeHtml()}">
                    <button class="btn btn-primary" type="submit">Riproduci sulla TV</button>
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
            <div style="font-size: 20px; font-weight: 700;">Riproduci sulla TV</div>
            <div style="font-size: 13px; color: #6b6b66;">Scegli un contenuto: la TV lo apre subito</div>
          </div>
          <a class="btn btn-ghost" href="/">Sorgenti</a>
        </div>
        $esitoHtml
        <form method="get" action="/riproduci" style="margin-bottom: 16px;">
          <input class="field-input" name="q" placeholder="Cerca fra Canali, Film e Serie" value="${query.escapeHtml()}" style="margin-bottom: 8px;">
          <button class="btn btn-ghost" type="submit">Cerca</button>
        </form>
        <table>
          <thead><tr><th>Titolo</th><th>Tipo</th><th></th></tr></thead>
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
