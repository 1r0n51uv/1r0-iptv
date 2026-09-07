package com.ir0.iptv.app

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
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
 * Stato della gesture sul tasto OK, in campi normali (non snapshot): il timer della pressione
 * lunga gira in una coroutine e il suo esito deve essere visibile *subito* al gestore del KeyUp,
 * che parte pochi ms dopo. Con `mutableStateOf` la scrittura del timer non era ancora visibile al
 * KeyUp e partivano *entrambe* le azioni: il menu si apriva e dietro si apriva anche il Dettaglio.
 *
 * [attiva] copre l'intera gesture, dal primo KeyDown al KeyUp: tra i due arrivano i KeyDown
 * ripetuti dell'auto-repeat (`FLAG_LONG_PRESS`), che vanno ignorati e NON devono ri-armare la
 * gesture — era l'altra meta' del bug.
 */
private class StatoPressione {
    var attiva = false
    var lungaScattata = false
    var clickScattato = false
}

/**
 * "Premi OK per aprire, tieni premuto OK per il menu" col telecomando.
 *
 * Il tasto centrale del D-pad e' gestito **tutto** in un `onPreviewKeyEvent` messo per primo
 * nella catena (intercetta prima di `clickable`, che quindi riceve solo il tap del tocco): un
 * timer fa scattare [onLongClick] mentre OK e' ancora premuto (il menu appare subito); al
 * rilascio, se il long non e' gia' scattato, parte [onClick]. Le due azioni si escludono a
 * vicenda tramite [StatoPressione].
 */
fun Modifier.pressabile(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val premutoTouch by interactionSource.collectIsPressedAsState()
    var premutoTasto by remember { mutableStateOf(false) }
    var timerPressioneLunga by remember { mutableStateOf<Job?>(null) }
    val stato = remember { StatoPressione() }

    val premuta = premutoTouch || premutoTasto
    val scalaPressione by animateFloatAsState(
        targetValue = if (premuta) 0.93f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "scalaPressione"
    )

    fun fine() {
        stato.attiva = false
        premutoTasto = false
        timerPressioneLunga?.cancel()
        timerPressioneLunga = null
    }

    Modifier
        .graphicsLayer {
            scaleX = scalaPressione
            scaleY = scalaPressione
        }
        .onPreviewKeyEvent { evento ->
            val tastoCentrale = evento.key == Key.DirectionCenter ||
                evento.key == Key.Enter ||
                evento.key == Key.NumPadEnter
            if (!tastoCentrale) return@onPreviewKeyEvent false
            when (evento.type) {
                KeyEventType.KeyDown -> {
                    // Solo il primo KeyDown apre la gesture; i KeyDown ripetuti dell'auto-repeat
                    // vengono ingoiati senza ri-armare nulla.
                    if (!stato.attiva) {
                        stato.attiva = true
                        stato.lungaScattata = false
                        stato.clickScattato = false
                        premutoTasto = true
                        if (onLongClick != null) {
                            timerPressioneLunga = scope.launch {
                                delay(SOGLIA_PRESSIONE_LUNGA_MS)
                                if (stato.attiva && !stato.clickScattato) {
                                    stato.lungaScattata = true
                                    onLongClick()
                                }
                            }
                        }
                    }
                    true
                }

                KeyEventType.KeyUp -> {
                    val giaLunga = stato.lungaScattata
                    fine()
                    if (!giaLunga && !stato.clickScattato) {
                        stato.clickScattato = true
                        onClick()
                    }
                    true
                }

                else -> false
            }
        }
        // Rete di sicurezza: se il focus se ne va mentre OK e' premuto (menu che si apre), la
        // gesture si chiude e la card non resta rimpicciolita.
        .onFocusChanged { if (!it.isFocused) fine() }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}
