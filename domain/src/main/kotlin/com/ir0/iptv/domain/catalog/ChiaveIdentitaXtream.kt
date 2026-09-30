package com.ir0.iptv.domain.catalog

/**
 * Alcuni provider Xtream ruotano periodicamente username e password dell'account (a volte anche
 * l'host), pur restando lo stesso stream: usare l'URL completo come Chiave di Identita' (come si
 * faceva prima) fa cambiare quella Chiave ad ogni rotazione, orfanizzando Preferiti e Visti gia'
 * salvati - bug confermato su un account reale dopo una rotazione delle credenziali. Qui invece si
 * usa l'id numerico dello stream, stabile per lo stesso contenuto a prescindere da host/credenziali.
 */
private val PREFISSO_PER_TIPO = mapOf(
    "movie" to "xtream-film",
    "live" to "xtream-canale",
    "series" to "xtream-episodio"
)

private val PATTERN_URL_XTREAM =
    Regex("""^https?://[^/]+/(movie|live|series)/[^/]+/[^/]+/(\d+)\.[^/.]+$""")

fun chiaveIdentitaXtream(tipoPath: String, streamId: Int): String =
    "${PREFISSO_PER_TIPO.getValue(tipoPath)}:$streamId"

/** Riscrive una vecchia Chiave di Identita' (l'URL completo dello stream Xtream) nella nuova
 * forma stabile, se la riconosce; altrimenti la lascia invariata (Sorgenti M3U, che non hanno un
 * id numerico stabile oltre all'URL stesso, o una Chiave gia' nella forma nuova). */
fun migraChiaveIdentita(chiave: String): String {
    val match = PATTERN_URL_XTREAM.matchEntire(chiave) ?: return chiave
    val (tipoPath, id) = match.destructured
    return chiaveIdentitaXtream(tipoPath, id.toInt())
}
