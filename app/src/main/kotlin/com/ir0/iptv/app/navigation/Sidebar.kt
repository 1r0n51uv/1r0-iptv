package com.ir0.iptv.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ir0.iptv.app.theme.LocalAccento
import com.ir0.iptv.app.zoomInFocus

private val ICONA_DIMENSIONE = 40.dp
private val GLIFO_DIMENSIONE = 22.dp
private val SPAZIATURA = 8.dp

@Composable
fun Sidebar(
    selezionata: Destinazione,
    onSeleziona: (Destinazione) -> Unit,
    /** Attaccato all'icona della sezione corrente: chi entra nella Sidebar (di solito con
     * SINISTRA dai contenuti) ci mette sopra il focus, invece che su quella piu' vicina. */
    focusSezioneCorrente: FocusRequester,
    inAggiornamento: Boolean = false,
    onAggiorna: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(88.dp)
            .fillMaxHeight()
            .background(Color(0xFF191C22))
            .padding(vertical = 12.dp)
            .focusGroup(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SPAZIATURA)
    ) {
        Destinazione.entries.forEach { destinazione ->
            val corrente = destinazione == selezionata
            SidebarButton(
                icona = iconaDi(destinazione),
                descrizione = destinazione.etichetta,
                active = corrente,
                focusRequester = focusSezioneCorrente.takeIf { corrente },
                onClick = { onSeleziona(destinazione) }
            )
        }
        RefreshButton(inAggiornamento = inAggiornamento, onClick = onAggiorna)
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
    Destinazione.IMPOSTAZIONI -> Icons.Filled.Settings
}

@Composable
private fun SidebarButton(
    icona: ImageVector,
    descrizione: String,
    active: Boolean,
    focusRequester: FocusRequester?,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val accento = LocalAccento.current
    val forma = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .size(ICONA_DIMENSIONE)
            .zoomInFocus(isFocused, forma, scalaMax = 1.18f, ombraMax = 6.dp)
            .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
            .clip(forma)
            .background(if (active) accento else Color.Transparent)
            .then(
                if (isFocused && !active) {
                    Modifier.border(2.dp, accento, forma)
                } else {
                    Modifier
                }
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icona,
            contentDescription = descrizione,
            tint = if (active) Color(0xFF14161A) else Color(0xFF9AA0AA),
            modifier = Modifier.size(GLIFO_DIMENSIONE)
        )
    }
}

@Composable
private fun RefreshButton(inAggiornamento: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val accento = LocalAccento.current
    val forma = RoundedCornerShape(10.dp)
    Column(
        modifier = Modifier
            .size(ICONA_DIMENSIONE)
            .zoomInFocus(isFocused, forma, scalaMax = 1.18f, ombraMax = 6.dp)
            .clip(forma)
            .then(
                if (isFocused) {
                    Modifier.border(2.dp, accento, forma)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !inAggiornamento,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Autorenew,
            contentDescription = if (inAggiornamento) "Aggiornamento in corso" else "Aggiorna catalogo",
            tint = if (inAggiornamento) Color(0xFF4A505C) else Color(0xFF9AA0AA),
            modifier = Modifier.size(GLIFO_DIMENSIONE)
        )
    }
}
