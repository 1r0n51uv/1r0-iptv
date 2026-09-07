package com.ir0.iptv.app

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * L'elemento in focus si ingrandisce di poco ("zoom in" al cambio di focus) e solleva un'ombra
 * piu' marcata, cosi' si vede al volo cosa e' selezionato col telecomando. Va messo per primo
 * nella catena dei modificatori, prima di clip/background, perche' scala e ombra riguardino
 * tutta la card.
 *
 * La molla e' **critica (senza rimbalzo)**: con un rimbalzo la card oscillava di dimensione dopo
 * il cambio di focus e, scorrendo veloce una riga col D-pad, le card mezze-animate facevano
 * "ondeggiare" la riga su e giu'. Senza rimbalzo la scala sale (o scende) una volta sola e si
 * ferma, e l'elemento che perde il focus torna subito a riposo invece di restare "acceso" a
 * lungo.
 *
 * Nota: `graphicsLayer` scala solo in fase di disegno, la dimensione a layout resta quella, quindi
 * i contenitori a scorrimento vanno lasciati con un po' di spazio (contentPadding) perche' la
 * card ingrandita non venga tagliata ai bordi.
 */
@Composable
fun Modifier.zoomInFocus(
    infocata: Boolean,
    forma: Shape,
    scalaMax: Float = 1.07f,
    ombraMax: Dp = 24.dp,
    /** Piu' alta = ritorno a riposo piu' rapido. La Sidebar la alza per non lasciare l'icona
     * "accesa" mentre il focus e' gia' altrove. */
    rigidezza: Float = Spring.StiffnessMediumLow,
    /** Punto fisso dello zoom. Per una card in una riga orizzontale conviene ancorarla in basso
     * (0.5, 1): cresce solo verso l'alto, il bordo inferiore e la linea di base della riga non
     * si muovono. */
    origine: TransformOrigin = TransformOrigin.Center
): Modifier {
    val scala by animateFloatAsState(
        targetValue = if (infocata) scalaMax else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = rigidezza
        ),
        label = "zoomFocusScala"
    )
    val ombra by animateDpAsState(
        targetValue = if (infocata) ombraMax else 0.dp,
        animationSpec = tween(durationMillis = 160),
        label = "zoomFocusOmbra"
    )
    return this
        .graphicsLayer {
            scaleX = scala
            scaleY = scala
            transformOrigin = origine
        }
        .shadow(elevation = ombra, shape = forma, clip = false)
}
