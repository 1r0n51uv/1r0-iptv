package com.ir0.iptv.app.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Un'unica regola per tutti i pulsanti dell'app, cosi' non ce ne sono piu' di bianchi e d'accento
 * a seconda della schermata:
 *
 * - **a fuoco**: sempre pieno del colore d'accento scelto nelle Impostazioni, testo in contrasto;
 * - **a riposo, primario** (l'azione principale della schermata: Riprendi, Play): bianco pieno;
 * - **a riposo, secondario** (tutto il resto): vetro traslucido sopra lo sfondo.
 */
enum class RuoloPulsante { PRIMARIO, SECONDARIO }

@Immutable
data class ColoriPulsante(val sfondo: Color, val contenuto: Color)

/** Il testo/icona da usare sopra [sfondo]: inchiostro sui colori chiari (ambra, verde, rosa, blu),
 * bianco su quelli scuri (rosso). */
fun contenutoSopra(sfondo: Color): Color =
    if (sfondo.luminance() > 0.3f) Palette.inchiostro else Palette.testo

/** Vetro dei pulsanti secondari a riposo. */
val VetroPulsante = Palette.testo.copy(alpha = 0.14f)

@Composable
fun coloriPulsante(ruolo: RuoloPulsante, infocato: Boolean): ColoriPulsante {
    val accento = LocalAccento.current
    val bersaglio = when {
        infocato -> accento
        ruolo == RuoloPulsante.PRIMARIO -> Palette.testo
        else -> VetroPulsante
    }
    val sfondo by animateColorAsState(
        targetValue = bersaglio,
        animationSpec = tween(durationMillis = 160),
        label = "sfondoPulsante"
    )
    val contenuto = when {
        infocato -> contenutoSopra(accento)
        ruolo == RuoloPulsante.PRIMARIO -> Palette.inchiostro
        else -> Palette.testo
    }
    return ColoriPulsante(sfondo, contenuto)
}
