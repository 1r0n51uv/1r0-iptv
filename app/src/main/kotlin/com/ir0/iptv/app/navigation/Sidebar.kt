package com.ir0.iptv.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.theme.Colori
import com.ir0.iptv.app.theme.Spazi
import com.ir0.iptv.app.theme.Tipo
import com.ir0.iptv.app.ui.sfondoBlocco

/** La barra di navigazione persistente: raggruppata per Diretta / Libreria / Sistema, voce
 * attiva a blocco pieno del colore di sezione, voce in focus a blocco di carta. */
private val GRUPPI: List<List<Destinazione>> = listOf(
    listOf(Destinazione.DASHBOARD),
    listOf(Destinazione.GUIDA, Destinazione.CANALI, Destinazione.SPORT),
    listOf(Destinazione.FILM, Destinazione.SERIE, Destinazione.PREFERITI, Destinazione.CERCA),
    listOf(Destinazione.CONNESSIONE, Destinazione.IMPOSTAZIONI)
)

private val FORMA = RoundedCornerShape(8.dp)

@Composable
fun Sidebar(
    selezionata: Destinazione,
    onSeleziona: (Destinazione) -> Unit,
    /** Attaccato alla voce della sezione corrente: chi entra nella Sidebar (con SINISTRA dai
     * contenuti) ci mette sopra il focus. */
    focusSezioneCorrente: FocusRequester,
    inAggiornamento: Boolean = false,
    onAggiorna: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(Spazi.sidebar)
            .fillMaxHeight()
            .background(Colori.superficie)
            .padding(vertical = Spazi.m)
            .focusGroup(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GRUPPI.forEachIndexed { indiceGruppo, gruppo ->
            if (indiceGruppo > 0) Divisore()
            gruppo.forEach { destinazione ->
                val corrente = destinazione == selezionata
                VoceSidebar(
                    icona = iconaDi(destinazione),
                    etichetta = destinazione.etichetta,
                    coloreSezione = destinazione.coloreSezione,
                    attiva = corrente,
                    focusRequester = focusSezioneCorrente.takeIf { corrente },
                    onClick = { onSeleziona(destinazione) }
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Divisore()
        VoceSidebar(
            icona = Icons.Outlined.Sync,
            etichetta = if (inAggiornamento) "In corso" else "Aggiorna",
            coloreSezione = Colori.sistema,
            attiva = false,
            focusRequester = null,
            attenuata = inAggiornamento,
            onClick = { if (!inAggiornamento) onAggiorna() }
        )
    }
}

private fun iconaDi(destinazione: Destinazione): ImageVector = when (destinazione) {
    Destinazione.DASHBOARD -> Icons.Outlined.Home
    Destinazione.GUIDA -> Icons.Outlined.GridView
    Destinazione.CANALI -> Icons.Outlined.LiveTv
    Destinazione.SPORT -> Icons.Outlined.SportsSoccer
    Destinazione.FILM -> Icons.Outlined.Movie
    Destinazione.SERIE -> Icons.Outlined.Tv
    Destinazione.PREFERITI -> Icons.Outlined.Star
    Destinazione.CERCA -> Icons.Outlined.Search
    Destinazione.CONNESSIONE -> Icons.Outlined.QrCode2
    Destinazione.IMPOSTAZIONI -> Icons.Outlined.Tune
}

@Composable
private fun Divisore() {
    Box(
        modifier = Modifier
            .padding(vertical = Spazi.s, horizontal = Spazi.l)
            .fillMaxWidth()
            .height(1.dp)
            .background(Colori.linea)
    )
}

@Composable
private fun VoceSidebar(
    icona: ImageVector,
    etichetta: String,
    coloreSezione: Color,
    attiva: Boolean,
    focusRequester: FocusRequester?,
    attenuata: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val infocata by interactionSource.collectIsFocusedAsState()

    val coloreContenuto = when {
        attiva -> Colori.inchiostro
        infocata -> Colori.inchiostro
        attenuata -> Colori.testoDebole
        else -> Colori.testoFioco
    }

    Column(
        modifier = Modifier
            .padding(horizontal = Spazi.s, vertical = 3.dp)
            .width(84.dp)
            .let { base ->
                if (attiva) base.clip(FORMA).background(coloreSezione)
                else base.sfondoBlocco(infocata, coloreSezione, FORMA)
            }
            .then(
                if (attiva && infocata) Modifier.border(2.dp, Colori.inchiostro, FORMA) else Modifier
            )
            .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icona,
            contentDescription = etichetta,
            tint = coloreContenuto,
            modifier = Modifier.width(24.dp).height(24.dp)
        )
        Text(
            text = etichetta,
            style = Tipo.etichetta.copy(fontSize = 11.sp, lineHeight = 12.sp),
            color = coloreContenuto,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
