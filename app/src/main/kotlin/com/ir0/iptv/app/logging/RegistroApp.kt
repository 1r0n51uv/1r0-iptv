package com.ir0.iptv.app.logging

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Registro testuale degli eventi rilevanti dell'app: crash (eccezioni non gestite) ed errori
 * applicativi che oggi falliscono in silenzio (una Sorgente irraggiungibile, una riproduzione che
 * si arrende dopo i retry). Letto dalle Impostazioni per capire quando e perche' l'app si blocca,
 * senza dover collegare un computer.
 *
 * File di testo append-only con rotazione a dimensione: sopravvive ai riavvii (a differenza di un
 * log solo in memoria, inutile proprio per un crash) ma non cresce all'infinito.
 */
object RegistroApp {
    private const val NOME_FILE = "registro-app.log"
    private const val DIMENSIONE_MASSIMA_BYTE = 512 * 1024
    private const val RIGHE_DOPO_ROTAZIONE = 400
    private val formato = SimpleDateFormat("dd/MM HH:mm:ss", Locale.ITALY)

    private var file: File? = null

    fun inizializza(context: Context) {
        file = File(context.applicationContext.filesDir, NOME_FILE)
    }

    @Synchronized
    fun errore(tag: String, messaggio: String, eccezione: Throwable? = null) {
        scrivi("ERRORE", tag, messaggio, eccezione)
    }

    @Synchronized
    fun crash(eccezione: Throwable) {
        scrivi("CRASH", "App", eccezione.message ?: eccezione.toString(), eccezione)
    }

    @Synchronized
    fun leggi(): String = file?.takeIf { it.exists() }?.let {
        runCatching { it.readText() }.getOrDefault("")
    }.orEmpty()

    @Synchronized
    fun svuota() {
        file?.takeIf { it.exists() }?.let { runCatching { it.delete() } }
    }

    private fun scrivi(livello: String, tag: String, messaggio: String, eccezione: Throwable?) {
        val fileCorrente = file ?: return
        val riga = buildString {
            append(formato.format(Date()))
            append(" [").append(livello).append("] ")
            append(tag).append(": ").append(messaggio)
            if (eccezione != null) {
                append('\n')
                append(eccezione.stackTraceToString())
            }
            append('\n')
        }
        // Il registro non deve mai far cadere (o rallentare in modo visibile) l'app che dovrebbe
        // aiutare a diagnosticare: un fallimento qui si ignora in silenzio.
        runCatching {
            fileCorrente.appendText(riga)
            ruotaSeTroppoGrande(fileCorrente)
        }
    }

    private fun ruotaSeTroppoGrande(file: File) {
        if (file.length() <= DIMENSIONE_MASSIMA_BYTE) return
        val righe = file.readLines().takeLast(RIGHE_DOPO_ROTAZIONE)
        file.writeText(righe.joinToString("\n", postfix = "\n"))
    }
}
