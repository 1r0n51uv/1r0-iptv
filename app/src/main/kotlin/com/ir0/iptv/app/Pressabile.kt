package com.ir0.iptv.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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
 * del tasto centrale del D-pad: alcuni telecomandi mandano un solo KeyDown/KeyUp, altri ripetono
 * il KeyDown, e in nessuno dei due casi `onLongClick` scatta. Qui si intercettano gli eventi del
 * tasto centrale prima di `combinedClickable` (onPreviewKeyEvent) e si distingue tap da pressione
 * lunga con un timer: se OK resta premuto oltre la soglia parte [onLongClick], altrimenti al
 * rilascio parte [onClick].
 */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.pressabile(
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
): Modifier = composed {
    val scope = rememberCoroutineScope()
    var timerPressioneLunga by remember { mutableStateOf<Job?>(null) }
    var lungaScattata by remember { mutableStateOf(false) }

    combinedClickable(onClick = onClick, onLongClick = onLongClick)
        .onPreviewKeyEvent { evento ->
            val tastoCentrale = evento.key == Key.DirectionCenter ||
                evento.key == Key.Enter ||
                evento.key == Key.NumPadEnter
            if (!tastoCentrale) return@onPreviewKeyEvent false
            when (evento.type) {
                KeyEventType.KeyDown -> {
                    // Il KeyDown si ripete finche' OK resta premuto: il timer parte una volta sola.
                    if (onLongClick != null && timerPressioneLunga?.isActive != true && !lungaScattata) {
                        timerPressioneLunga = scope.launch {
                            delay(SOGLIA_PRESSIONE_LUNGA_MS)
                            lungaScattata = true
                            onLongClick()
                        }
                    }
                    // Consuma il tasto centrale: click e long-click li gestiamo qui, non
                    // combinedClickable, cosi' non c'e' doppio scatto.
                    true
                }

                KeyEventType.KeyUp -> {
                    val eraGiaLunga = lungaScattata
                    timerPressioneLunga?.cancel()
                    timerPressioneLunga = null
                    lungaScattata = false
                    if (!eraGiaLunga) onClick()
                    true
                }

                else -> false
            }
        }
}
