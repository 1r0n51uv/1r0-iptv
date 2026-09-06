package com.ir0.iptv.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ir0.iptv.app.theme.Colori

/**
 * Il focus a blocco: la voce in focus diventa un blocco pieno di "carta" con una barra di colore
 * della sezione sul lato d'ingresso, e il chiamante inverte il testo. Niente scala, niente ombra —
 * così non può sfarfallare passando da una voce all'altra e si legge da tre metri.
 *
 * Va messo prima di `padding` nella catena, su un contenitore già dimensionato.
 */
@Composable
fun Modifier.sfondoBlocco(
    infocato: Boolean,
    sezione: Color,
    forma: Shape,
    barra: Dp = 4.dp
): Modifier {
    val sfondo by animateColorAsState(
        targetValue = if (infocato) Colori.carta else Color.Transparent,
        animationSpec = tween(durationMillis = 90),
        label = "sfondoBlocco"
    )
    return this
        .clip(forma)
        .background(sfondo)
        .drawBehind {
            if (infocato) {
                drawRect(color = sezione, topLeft = Offset.Zero, size = Size(barra.toPx(), size.height))
            }
        }
}

/** Colore del testo principale dentro una voce: inchiostro quando è il blocco in focus. */
fun testoSuBlocco(infocato: Boolean, aRiposo: Color = Colori.testo): Color =
    if (infocato) Colori.inchiostro else aRiposo

/** Colore del testo secondario dentro una voce in focus (grigio scuro su carta). */
fun testoSecondarioSuBlocco(infocato: Boolean, aRiposo: Color = Colori.testoFioco): Color =
    if (infocato) Color(0xFF4A4C50) else aRiposo

/** Barra di avanzamento piatta, senza pillola: "min rimasti", programma in onda, ripresa episodio. */
@Composable
fun BarraAvanzamento(
    percentuale: Int,
    colore: Color,
    modifier: Modifier = Modifier,
    fondo: Color = Colori.linea
) {
    Box(modifier = modifier.background(fondo)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(percentuale.coerceIn(0, 100) / 100f)
                .fillMaxHeight()
                .background(colore)
        )
    }
}
