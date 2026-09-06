package com.ir0.iptv.app.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Sistema visivo "Palinsesto": l'app come un listino di programmazione, non una vetrina.
 * Un colore per sezione, una scala tipografica con cifre tabulari per orari e numeri di canale,
 * e un focus a blocco pieno (il cursore del televideo) al posto dello zoom.
 */
object Colori {
    val inchiostro = Color(0xFF100F0D)        // fondo, nero caldo (inchiostro su carta)
    val carta = Color(0xFFE8E4DA)             // superfici invertite / blocco in focus
    val superficie = Color(0xFF17181A)
    val superficieAlta = Color(0xFF202226)

    val testo = Color(0xFFF3F1EC)
    val testoFioco = Color(0xFF9EA0A2)
    val testoDebole = Color(0xFF64666A)

    val linea = Color(0xFF2B2D31)

    // Colori di sezione (dalla tavolozza a 7 colori del televideo, desaturati).
    val live = Color(0xFFE5484D)             // Guida, Canali, diretta
    val film = Color(0xFFE0A32E)
    val serie = Color(0xFF5B8DEF)
    val sport = Color(0xFF46A758)
    val preferiti = Color(0xFFC266E5)
    val sistema = Color(0xFF9EA0A2)

    // Semantica
    val inOnda = live
    val nuovo = Color(0xFF46A758)
    val nonDisponibile = Color(0xFF64666A)
}

/**
 * Colore della sezione corrente: guida il blocco di focus, la voce attiva della Sidebar e i
 * filetti sotto i titoli. Impostato in [com.ir0.iptv.app.MainActivity] in base alla destinazione.
 */
val LocalSezione = compositionLocalOf { Colori.live }

private val Grottesco = FontFamily.SansSerif

/** Cifre a larghezza fissa: orari, numeri di canale, SxxEyy, durate restano incolonnati. */
private val Tabulari = TextStyle(fontFamily = Grottesco, fontFeatureSettings = "tnum")

object Tipo {
    val titolo = TextStyle(
        fontFamily = Grottesco, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 44.sp,
        letterSpacing = (-0.5).sp
    )
    val sezione = TextStyle(
        fontFamily = Grottesco, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 28.sp
    )
    val corpo = TextStyle(
        fontFamily = Grottesco, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp
    )
    val corpoForte = corpo.copy(fontWeight = FontWeight.SemiBold)
    val etichetta = TextStyle(
        fontFamily = Grottesco, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp,
        letterSpacing = 0.3.sp
    )

    val oraGrande = Tabulari.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 24.sp)
    val ora = Tabulari.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 18.sp)
    val numeroCanale = Tabulari.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 18.sp)
    val siglaEpisodio = Tabulari.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp)
}

/** 8px di base; area di sicurezza 48 per l'overscan della TV. */
object Spazi {
    val bordoSchermo = 48.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 16.dp
    val l = 32.dp
    val xl = 64.dp
    val sidebar = 116.dp
    val sidebarStretta = 64.dp
}
