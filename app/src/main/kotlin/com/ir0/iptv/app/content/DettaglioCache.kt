package com.ir0.iptv.app.content

import com.ir0.iptv.domain.catalog.DettaglioEsteso
import com.ir0.iptv.domain.classification.Serie

/**
 * Cache in memoria per la sessione dell'app dei dati di Dettaglio gia' scaricati da una
 * Sorgente: stagioni/episodi di una Serie, metadati estesi di un Film. Senza questa cache,
 * tornare al Dettaglio dal Player lo smonta e lo ricompone da capo, rifacendo la chiamata alla
 * Sorgente ogni volta. Si perde al riavvio dell'app: nessuna persistenza su disco, dato che
 * stagioni/episodi ed i metadati di un Film cambiano di rado durante una sessione.
 *
 * Solo i risultati con successo vengono ricordati: un fallimento (Sorgente irraggiungibile,
 * provider senza quel dato) non viene messo in cache, cosi' il prossimo tentativo puo' ancora
 * riuscire invece di restare bloccato sull'esito negativo per l'intera sessione.
 */
object DettaglioCache {
    private val serie = mutableMapOf<String, Serie>()
    private val film = mutableMapOf<String, DettaglioEsteso>()

    fun serie(chiaveIdentita: String): Serie? = serie[chiaveIdentita]
    fun salvaSerie(chiaveIdentita: String, valore: Serie) {
        serie[chiaveIdentita] = valore
    }

    fun film(chiaveIdentita: String): DettaglioEsteso? = film[chiaveIdentita]
    fun salvaFilm(chiaveIdentita: String, valore: DettaglioEsteso) {
        film[chiaveIdentita] = valore
    }

    /** Da chiamare ad ogni "Aggiorna catalogo": una Serie tenuta in cache potrebbe avere nuovi
     * Episodi arrivati proprio con quel refresh (vedi Nuovi episodi), quindi il dato vecchio
     * andrebbe scartato invece di restare buono per il resto della sessione. */
    fun pulisci() {
        serie.clear()
        film.clear()
    }
}
