package com.ir0.iptv.domain.epg

/** Titolo e descrizione arrivano da Xtream in base64: la decodifica sta in :app, che ha
 * `android.util.Base64` (quello di `java.util` richiederebbe API 26). */
data class XtreamEpgListingDto(
    val titolo: String,
    val descrizione: String?,
    val inizioSecondi: Long,
    val fineSecondi: Long
)

class XtreamEpgMapper {

    fun toProgrammi(listings: List<XtreamEpgListingDto>): List<Programma> = listings
        .filter { it.titolo.isNotBlank() && it.fineSecondi > it.inizioSecondi }
        .map {
            Programma(
                titolo = it.titolo.trim(),
                descrizione = it.descrizione?.trim()?.ifBlank { null },
                inizioMs = it.inizioSecondi * 1000,
                fineMs = it.fineSecondi * 1000
            )
        }
        .sortedBy { it.inizioMs }
}
