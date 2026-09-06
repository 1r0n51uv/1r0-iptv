package com.ir0.iptv.app

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SOGLIA_PRESSIONE_LUNGA_MS = 400L

/**
 * "Premi OK per aprire, tieni premuto OK per il menu" col telecomando.
 *
 * `combinedClickable` gestisce da solo il tocco (tap e pressione lunga) ma NON la pressione lunga
 * del tasto centrale del D-pad. Qui si intercettano gli eventi del tasto centrale prima di
 * `combinedClickable` (onPreviewKeyEvent):
 *  - un timer fa scattare [onLongClick] mentre OK e' ancora premuto (il menu appare subito);
 *  - al rilascio, se il long non e' scattato, parte [onClick].
 *
 * Mentre OK e' premuto la card si rimpicciolisce un po' (zoom out) per far vedere cosa si sta
 * premendo. Il "trascinamento" del KeyUp verso il menu appena aperto lo neutralizza il menu
 * stesso (vedi MenuContenuto), che ignora la coda della pressione lunga.
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pressabile(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val premutoTouch by interactionSource.collectIsPressedAsState()
    var premutoTasto by remember { mutableStateOf(false) }
    var timerPressioneLunga by remember { mutableStateOf<Job?>(null) }
    var lungaScattata by remember { mutableStateOf(false) }

    val premuta = premutoTouch || premutoTasto
    val scalaPressione by animateFloatAsState(
        targetValue = if (premuta) 0.93f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "scalaPressione"
    )

    fun fine() {
        premutoTasto = false
        timerPressioneLunga?.cancel()
        timerPressioneLunga = null
    }

    Modifier
        .graphicsLayer {
            scaleX = scalaPressione
            scaleY = scalaPressione
        }
        // Rete di sicurezza: se il focus se ne va mentre OK e' premuto (menu che si apre), la
        // card non deve restare rimpicciolita.
        .onFocusChanged { if (!it.isFocused) fine() }
        .combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick,
            onLongClick = onLongClick
        )
        .onPreviewKeyEvent { evento ->
            val tastoCentrale = evento.key == Key.DirectionCenter ||
                evento.key == Key.Enter ||
                evento.key == Key.NumPadEnter
            if (!tastoCentrale) return@onPreviewKeyEvent false
            when (evento.type) {
                KeyEventType.KeyDown -> {
                    if (!premutoTasto) {
                        premutoTasto = true
                        lungaScattata = false
                        if (onLongClick != null) {
                            timerPressioneLunga = scope.launch {
                                delay(SOGLIA_PRESSIONE_LUNGA_MS)
                                lungaScattata = true
                                // Il menu si apre e ruba il focus: qui la card ha finito la sua
                                // pressione, torna a dimensione normale.
                                premutoTasto = false
                                onLongClick()
                            }
                        }
                    }
                    true
                }

                KeyEventType.KeyUp -> {
                    val eraLunga = lungaScattata
                    fine()
                    lungaScattata = false
                    if (!eraLunga) onClick()
                    true
                }

                else -> false
            }
        }
}
