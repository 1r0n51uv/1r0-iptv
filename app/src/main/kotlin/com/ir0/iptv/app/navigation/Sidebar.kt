package com.ir0.iptv.app.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.EasingCinema
import com.ir0.iptv.app.theme.LocalAccento
import com.ir0.iptv.app.theme.Palette
import com.ir0.iptv.app.theme.contenutoSopra
import com.ir0.iptv.app.zoomInFocus

/** Larghezza a riposo della Sidebar: i contenuti partono da qui (la Sidebar sta sopra di loro). */
val LARGHEZZA_SIDEBAR = 88.dp
private val LARGHEZZA_ESPANSA = 236.dp
private val CODA_SFUMATA = 220.dp
private val ICONA_DIMENSIONE = 40.dp
private val GLIFO_DIMENSIONE = 22.dp
private val SPAZIATURA = 8.dp
// (88 - 40) / 2, meno i 4dp della tacca: l'icona resta centrata nella Sidebar chiusa.
private val MARGINE_ICONA = 20.dp
private const val DURATA_ROTAZIONE_MS = 900
// Icone piccole e vicine: un'ombra da 160ms (il default altrove nell'app) sembra restare
// "in ritardo" dietro il focus, scorrendo veloce tra le sezioni col D-pad.
private const val DURATA_OMBRA_SIDEBAR_MS = 80

/**
 * Binario sottile sopra i contenuti che, appena prende il focus, si allarga mostrando le etichette
 * (come la navigazione delle app TV di streaming). L'allargamento e' solo visivo: il bersaglio di
 * focus resta l'icona 40dp nella stessa posizione, quindi DESTRA dalla Sidebar e SINISTRA verso di
 * lei si comportano come prima.
 */
@Composable
fun Sidebar(
    selezionata: Destinazione,
    onSeleziona: (Destinazione) -> Unit,
    /** Attaccato all'icona della sezione corrente: chi entra nella Sidebar (di solito con
     * SINISTRA dai contenuti) ci mette sopra il focus, invece che su quella piu' vicina. */
    focusSezioneCorrente: FocusRequester,
    inAggiornamento: Boolean = false,
    /** Da 0f a 1f, quanto e' avanzata la sincronizzazione in corso (vedi
     * ContentFetcher.onProgressoFrazionale): riempie il bordo dell'icona "Aggiorna catalogo"
     * invece di limitarsi a farla ruotare. Null quando non ancora noto (es. prima che il fetch
     * sia partito davvero). */
    progressoFrazionale: Float? = null,
    onAggiorna: () -> Unit = {},
    /** Vero solo quando l'utente e' entrato nella Sidebar premendo SINISTRA dai contenuti (lo
     * imposta MainActivity nell'exit del gruppo dei contenuti). */
    apertaDallUtente: Boolean = false,
    /** Chiamato quando la Sidebar perde il focus: MainActivity azzera [apertaDallUtente]. */
    onChiusa: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Si allarga SOLO se ci si e' entrati con SINISTRA, non per il semplice fatto di avere il
    // focus: all'avvio, aprendo un Dettaglio/il Player o tornandone, il focus puo' finire sulla
    // Sidebar da solo (la card premuta esce di composizione, la schermata nuova non ha ancora
    // preso il focus) e la Sidebar si apriva "a caso".
    var haFocus by remember { mutableStateOf(false) }
    val espansa = haFocus && apertaDallUtente
    val larghezza by animateDpAsState(
        targetValue = if (espansa) LARGHEZZA_ESPANSA else LARGHEZZA_SIDEBAR,
        animationSpec = tween(durationMillis = 320, easing = EasingCinema),
        label = "larghezzaSidebar"
    )
    val apertura by animateFloatAsState(
        targetValue = if (espansa) 1f else 0f,
        animationSpec = tween(durationMillis = 260),
        label = "aperturaSidebar"
    )
    Box(
        modifier = modifier
            // Da aperta la Sidebar porta con se' una coda sfumata sui contenuti, invece di finire
            // con uno stacco netto sopra le card.
            .width(larghezza + CODA_SFUMATA * apertura)
            .fillMaxHeight()
            .drawBehind {
                val pannello = (larghezza.toPx() / size.width).coerceIn(0.01f, 1f)
                drawRect(
                    Brush.horizontalGradient(
                        0f to Palette.inchiostro.copy(alpha = 0.94f),
                        pannello * 0.6f to Palette.inchiostro.copy(alpha = 0.72f + 0.24f * apertura),
                        pannello to Palette.inchiostro.copy(alpha = 0.35f + 0.6f * apertura),
                        1f to Palette.inchiostro.copy(alpha = if (apertura > 0f) 0f else 0.35f)
                    )
                )
            }
            .clipToBounds()
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 12.dp)
                .onFocusChanged {
                    haFocus = it.hasFocus
                    if (!it.hasFocus) onChiusa()
                }
                .focusGroup(),
            verticalArrangement = Arrangement.spacedBy(SPAZIATURA, Alignment.CenterVertically)
        ) {
            Destinazione.entries.forEach { destinazione ->
                val corrente = destinazione == selezionata
                SidebarButton(
                    icona = iconaDi(destinazione),
                    descrizione = destinazione.etichetta,
                    active = corrente,
                    apertura = apertura,
                    focusRequester = focusSezioneCorrente.takeIf { corrente },
                    onClick = { onSeleziona(destinazione) }
                )
            }
            RefreshButton(
                inAggiornamento = inAggiornamento,
                progressoFrazionale = progressoFrazionale,
                apertura = apertura,
                onClick = onAggiorna
            )
        }
    }
}

private fun iconaDi(destinazione: Destinazione): ImageVector = when (destinazione) {
    Destinazione.DASHBOARD -> Icons.Filled.Home
    Destinazione.CANALI -> Icons.Filled.LiveTv
    Destinazione.FILM -> Icons.Filled.Movie
    Destinazione.SERIE -> Icons.Filled.Tv
    Destinazione.GUIDA -> Icons.Filled.CalendarMonth
    Destinazione.CERCA -> Icons.Filled.Search
    Destinazione.PREFERITI -> Icons.Filled.Favorite
    Destinazione.SPORT -> Icons.Filled.SportsSoccer
    Destinazione.CONNESSIONE -> Icons.Filled.QrCode2
    Destinazione.IMPOSTAZIONI -> Icons.Filled.Settings
}

/** L'etichetta accanto all'icona, visibile solo a Sidebar aperta. Non focalizzabile. */
@Composable
private fun EtichettaSidebar(testo: String, evidenziata: Boolean, corrente: Boolean, apertura: Float) {
    if (apertura <= 0.01f) return
    Text(
        text = testo,
        color = if (evidenziata || corrente) Palette.testo else Palette.testoSecondario,
        fontSize = 16.sp,
        fontWeight = if (evidenziata || corrente) FontWeight.SemiBold else FontWeight.Medium,
        maxLines = 1,
        softWrap = false,
        modifier = Modifier
            .padding(start = 18.dp)
            .graphicsLayer {
                alpha = apertura
                translationX = (1f - apertura) * -12.dp.toPx()
            }
    )
}

/** La sezione corrente e' segnata da una tacca d'accento sul bordo sinistro. */
@Composable
private fun TaccaCorrente(visibile: Boolean, colore: Color) {
    val altezza by animateDpAsState(
        targetValue = if (visibile) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 260, easing = EasingCinema),
        label = "taccaSidebar"
    )
    Box(
        modifier = Modifier
            .width(4.dp)
            .height(altezza)
            .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
            .background(colore)
    )
}

/** Icona della sezione: a fuoco diventa un tassello d'accento (regola comune dei pulsanti, theme/Pulsanti.kt). */
@Composable
private fun SidebarButton(
    icona: ImageVector,
    descrizione: String,
    active: Boolean,
    apertura: Float,
    focusRequester: FocusRequester?,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val accento = LocalAccento.current
    val forma = RoundedCornerShape(12.dp)
    val fondo by animateColorAsState(
        targetValue = if (isFocused) accento else Color.Transparent,
        animationSpec = tween(durationMillis = 140),
        label = "fondoIconaSidebar"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        TaccaCorrente(visibile = active, colore = accento)
        Box(
            modifier = Modifier
                .padding(start = MARGINE_ICONA)
                .size(ICONA_DIMENSIONE)
                .zoomInFocus(
                    isFocused,
                    forma,
                    scalaMax = 1.12f,
                    ombraMax = 6.dp,
                    // Icona piccola e vicina alle altre: ritorno a riposo rapido, cosi' non resta
                    // "accesa" quando il focus e' gia' passato oltre.
                    rigidezza = Spring.StiffnessMedium,
                    durataOmbraMs = DURATA_OMBRA_SIDEBAR_MS
                )
                .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
                .clip(forma)
                .background(fondo)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icona,
                contentDescription = descrizione,
                tint = when {
                    isFocused -> contenutoSopra(accento)
                    active -> Palette.testo
                    else -> Palette.testoSecondario
                },
                modifier = Modifier.size(GLIFO_DIMENSIONE)
            )
        }
        EtichettaSidebar(descrizione, evidenziata = isFocused, corrente = active, apertura = apertura)
    }
}

@Composable
private fun RefreshButton(
    inAggiornamento: Boolean,
    progressoFrazionale: Float? = null,
    apertura: Float,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val accento = LocalAccento.current
    val forma = RoundedCornerShape(12.dp)
    val fondo by animateColorAsState(
        targetValue = if (isFocused) accento else Color.Transparent,
        animationSpec = tween(durationMillis = 140),
        label = "fondoAggiornaSidebar"
    )
    // La rotazione infinita vive solo mentre serve: appena inAggiornamento torna falso questo
    // ramo esce di composizione e Compose la ferma da solo, invece di girare per sempre.
    val angolo = if (inAggiornamento) {
        val transizione = rememberInfiniteTransition(label = "rotazioneAggiorna")
        val valore by transizione.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(DURATA_ROTAZIONE_MS, easing = LinearEasing)),
            label = "angoloAggiorna"
        )
        valore
    } else {
        0f
    }
    val descrizione = when {
        inAggiornamento && progressoFrazionale != null ->
            "Aggiornamento in corso: ${(progressoFrazionale * 100).toInt()}%"
        inAggiornamento -> "Aggiornamento in corso"
        else -> "Aggiorna catalogo"
    }
    // Versione corta per l'etichetta visibile: quella lunga non entra nella Sidebar aperta.
    val etichetta = when {
        inAggiornamento && progressoFrazionale != null -> "Aggiorno… ${(progressoFrazionale * 100).toInt()}%"
        inAggiornamento -> "Aggiorno…"
        else -> "Aggiorna catalogo"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        TaccaCorrente(visibile = false, colore = accento)
        Box(
            modifier = Modifier
                .padding(start = MARGINE_ICONA)
                .size(ICONA_DIMENSIONE)
                .zoomInFocus(
                    isFocused,
                    forma,
                    scalaMax = 1.12f,
                    ombraMax = 6.dp,
                    rigidezza = Spring.StiffnessMedium,
                    durataOmbraMs = DURATA_OMBRA_SIDEBAR_MS
                )
                .clip(forma)
                .background(fondo)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = !inAggiornamento,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // Rotazione continua dell'icona sempre presente durante l'aggiornamento (segnale di
            // vita anche prima che si sappia l'avanzamento): l'anello sopra si aggiunge appena e'
            // noto, riempiendosi via via invece di lasciare la sola attesa indefinita. Smussato
            // con animateFloatAsState perche' l'avanzamento arriva a scatti (un aggiornamento per
            // punto percentuale, vedi ContentFetcher.StreamConProgresso), non di continuo.
            if (inAggiornamento && progressoFrazionale != null) {
                val progressoAnimato by animateFloatAsState(
                    targetValue = progressoFrazionale,
                    label = "progressoAggiorna"
                )
                CircularProgressIndicator(
                    progress = { progressoAnimato },
                    modifier = Modifier.size(GLIFO_DIMENSIONE + 6.dp),
                    color = accento,
                    strokeWidth = 2.dp
                )
            }
            Icon(
                imageVector = Icons.Filled.Autorenew,
                contentDescription = descrizione,
                tint = when {
                    isFocused -> contenutoSopra(accento)
                    inAggiornamento -> Palette.testoTerziario
                    else -> Palette.testoSecondario
                },
                modifier = Modifier
                    .size(GLIFO_DIMENSIONE)
                    .graphicsLayer { rotationZ = angolo }
            )
        }
        EtichettaSidebar(etichetta, evidenziata = isFocused, corrente = false, apertura = apertura)
    }
}
