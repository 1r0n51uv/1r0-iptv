package com.ir0.iptv.app.navigation

import androidx.compose.ui.graphics.Color
import com.ir0.iptv.app.theme.Colori

enum class Destinazione(val etichetta: String, val coloreSezione: Color) {
    DASHBOARD("Home", Colori.live),
    CANALI("Canali", Colori.live),
    FILM("Film", Colori.film),
    SERIE("Serie", Colori.serie),
    GUIDA("Guida", Colori.live),
    CERCA("Cerca", Colori.serie),
    PREFERITI("Preferiti", Colori.preferiti),
    SPORT("Sport", Colori.sport),
    CONNESSIONE("Connessione", Colori.sistema),
    IMPOSTAZIONI("Setup", Colori.sistema)
}
