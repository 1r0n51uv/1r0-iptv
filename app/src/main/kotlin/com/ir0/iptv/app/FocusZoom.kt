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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * L'elemento in focus si ingrandisce di poco con un'animazione a molla ("zoom in / zoom out" al
 * cambio di focus) e solleva un'ombra piu' marcata, cosi' si vede al volo cosa e' selezionato
 * col telecomando. Va messo per primo nella catena dei modificatori, prima di clip/background,
 * perche' scala e ombra riguardino tutta la card.
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
    ombraMax: Dp = 24.dp
): Modifier {
    val scala by animateFloatAsState(
        targetValue = if (infocata) scalaMax else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "zoomFocusScala"
    )
    val ombra by animateDpAsState(
        targetValue = if (infocata) ombraMax else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "zoomFocusOmbra"
    )
    return this
        .graphicsLayer {
            scaleX = scala
            scaleY = scala
        }
        .shadow(elevation = ombra, shape = forma, clip = false)
}
