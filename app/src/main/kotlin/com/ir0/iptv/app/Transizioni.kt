package com.ir0.iptv.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Decelerazione "cinematografica": parte svelta e si posa piano, niente rimbalzi. */
val EasingCinema = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

/**
 * Dissolvenza + leggera salita della schermata nuova ad ogni cambio di [chiave].
 *
 * Solo `graphicsLayer` (alpha/traslazione in fase di disegno): la schermata e' composta e misurata
 * subito, quindi le richieste di focus fatte all'apertura (ripristino della card, pulsante
 * principale del Dettaglio) funzionano esattamente come prima — a differenza di AnimatedContent/
 * Crossfade, che terrebbero montate per qualche istante due schermate insieme.
 */
@Composable
fun Modifier.ingressoSchermata(chiave: Any?): Modifier {
    val progresso = remember(chiave) { Animatable(0f) }
    LaunchedEffect(chiave) {
        progresso.animateTo(1f, tween(durationMillis = 420, easing = EasingCinema))
    }
    val salita = with(LocalDensity.current) { 18.dp.toPx() }
    return graphicsLayer {
        alpha = progresso.value
        translationY = (1f - progresso.value) * salita
    }
}

/**
 * Maschera d'alfa sull'immagine di scenografia: trasparente sul bordo sinistro e su quello
 * inferiore, piena verso l'alto a destra. Senza, il bordo dell'immagine (che occupa solo una
 * parte dello schermo) resta visibile come uno stacco netto anche sotto le sfumature scure.
 */
fun Modifier.sfumaBordiScenografia(): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        drawRect(
            brush = Brush.horizontalGradient(0f to Color.Transparent, 0.45f to Color.Black),
            blendMode = BlendMode.DstIn
        )
        drawRect(
            brush = Brush.verticalGradient(0.55f to Color.Black, 1f to Color.Transparent),
            blendMode = BlendMode.DstIn
        )
    }

/**
 * Entrata scaglionata di un blocco in una lista (righe della Home, sezioni): ognuno parte
 * [indice] × 70ms dopo il precedente, salendo di 28dp mentre compare. Come [ingressoSchermata],
 * tocca solo il disegno: il contenuto e' focalizzabile da subito.
 */
@Composable
fun Modifier.entrataScaglionata(indice: Int): Modifier {
    val progresso = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(80L + indice * 70L)
        progresso.animateTo(1f, tween(durationMillis = 560, easing = EasingCinema))
    }
    val salita = with(LocalDensity.current) { 28.dp.toPx() }
    return graphicsLayer {
        alpha = progresso.value
        translationY = (1f - progresso.value) * salita
    }
}
