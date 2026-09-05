package com.ir0.iptv.app.session

import android.content.Context
import java.io.File

/** Ricorda su quale contenuto era il focus, per ritrovarlo alla riapertura dell'app. */
class StatoSessioneRepository(context: Context) {
    private val file = File(context.applicationContext.filesDir, "sessione.txt")

    @Synchronized
    fun ultimaChiave(): String? = if (file.exists()) file.readText().ifBlank { null } else null

    @Synchronized
    fun salvaUltimaChiave(chiave: String) {
        file.writeText(chiave)
    }
}
