package com.ir0.iptv.app

/** Le sezioni della Home, nell'ordine scelto dall'utente dalle Impostazioni. Sport non e' una
 * RigaDashboard (i dati vengono da un provider esterno, non dal catalogo), quindi vive qui come
 * sezione a se' invece che nel dominio insieme a Continua/Nuovi episodi/Suggeriti/Preferiti. */
enum class SezioneHome(val etichetta: String) {
    SPORT("Sport in diretta"),
    CONTINUA("Continua a guardare"),
    NUOVI_EPISODI("Nuovi episodi"),
    SUGGERITI("Suggeriti"),
    PREFERITI("Preferiti");

    companion object {
        val ordinePredefinito: List<SezioneHome> = entries.toList()

        /** Interpreta l'ordine salvato nelle Impostazioni: nomi che non corrispondono piu' a
         * una Sezione si scartano, Sezioni nuove non ancora salvate si aggiungono in coda (cosi'
         * un aggiornamento dell'app non fa sparire una riga dalla vista). */
        fun daSalvato(nomi: List<String>?): List<SezioneHome> {
            if (nomi == null) return ordinePredefinito
            val salvate = nomi.mapNotNull { nome -> entries.firstOrNull { it.name == nome } }
            val mancanti = entries.filter { it !in salvate }
            return salvate + mancanti
        }
    }
}
