package com.ir0.iptv.domain.dashboard

import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.catalog.ElencoPreferiti
import com.ir0.iptv.domain.customization.ContentCustomization
import com.ir0.iptv.domain.playback.Visto

/** Un catalogo M3U puo' avere decine di migliaia di voci: si manda solo un campione. */
private const val MAX_TITOLI_CATALOGO = 150
private const val MAX_TITOLI_GUSTI = 20
private const val QUANTI_SUGGERIMENTI = 8

class Suggerimenti(private val elencoPreferiti: ElencoPreferiti = ElencoPreferiti()) {

    /** Null quando non c'e' nulla su cui basarsi: senza gusti la chiamata sarebbe solo un costo. */
    fun prompt(
        catalogo: ContentCatalog,
        visti: List<Visto>,
        personalizzazioni: Map<String, ContentCustomization>
    ): String? {
        val gustiVisti = visti.map { it.serie ?: it.titolo }.distinct().take(MAX_TITOLI_GUSTI)
        val gustiPreferiti = elencoPreferiti.preferiti(catalogo, personalizzazioni)
            .map { it.title }
            .take(MAX_TITOLI_GUSTI)
        if (gustiVisti.isEmpty() && gustiPreferiti.isEmpty()) return null

        val gia = (gustiVisti + gustiPreferiti).toSet()
        val disponibili = catalogo.tutti.map { it.title }
            .filterNot { it in gia }
            .distinct()
            .take(MAX_TITOLI_CATALOGO)

        return buildString {
            appendLine("Scegli $QUANTI_SUGGERIMENTI titoli da consigliare a chi guarda questa TV.")
            appendLine()
            if (gustiVisti.isNotEmpty()) {
                appendLine("Ha guardato di recente:")
                gustiVisti.forEach { appendLine("- $it") }
                appendLine()
            }
            if (gustiPreferiti.isNotEmpty()) {
                appendLine("Ha fra i preferiti:")
                gustiPreferiti.forEach { appendLine("- $it") }
                appendLine()
            }
            appendLine("Titoli disponibili fra cui scegliere:")
            disponibili.forEach { appendLine("- $it") }
            appendLine()
            append("Rispondi solo con un array JSON dei titoli scelti, copiati esattamente ")
            append("dalla lista dei titoli disponibili, dal piu' al meno consigliato.")
        }
    }

    fun titoliDaRisposta(risposta: String): List<String> {
        val apertura = risposta.indexOf('[')
        val chiusura = risposta.lastIndexOf(']')
        val titoli = if (apertura in 0 until chiusura) {
            risposta.substring(apertura + 1, chiusura).split(',').map { it.trim().trim('"') }
        } else {
            risposta.lines().map { it.trim().removePrefix("-").trim().rimuoviNumerazione() }
        }
        return titoli.map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun riga(catalogo: ContentCatalog, titoli: List<String>): RigaDashboard? {
        val perTitolo = catalogo.tutti.associateBy { it.title.trim().lowercase() }
        val contenuti = titoli.mapNotNull { perTitolo[it.trim().lowercase()] }.distinct()
        return contenuti.takeIf { it.isNotEmpty() }?.let { RigaDashboard(TipoRiga.SUGGERITI, it) }
    }
}

private fun String.rimuoviNumerazione(): String = substringAfter(". ", this)
