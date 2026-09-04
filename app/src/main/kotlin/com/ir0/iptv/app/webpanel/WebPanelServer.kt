package com.ir0.iptv.app.webpanel

import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.util.concurrent.atomic.AtomicBoolean

object WebPanelServer {
    const val PORT = 8080

    private val started = AtomicBoolean(false)

    fun start() {
        if (!started.compareAndSet(false, true)) return
        embeddedServer(CIO, port = PORT, host = "0.0.0.0") {
            routing {
                get("/") {
                    call.respondText(PLACEHOLDER_HTML, ContentType.Text.Html)
                }
            }
        }.start(wait = false)
    }
}

private val PLACEHOLDER_HTML = """
    <!doctype html>
    <html lang="it">
    <head>
      <meta charset="utf-8">
      <title>1r0 IPTV - Pannello Web</title>
    </head>
    <body style="margin:0; height:100vh; display:flex; align-items:center; justify-content:center; font-family:system-ui, sans-serif; background:#14161a; color:#f2f2f0;">
      <p>Pannello Web in arrivo.</p>
    </body>
    </html>
""".trimIndent()
