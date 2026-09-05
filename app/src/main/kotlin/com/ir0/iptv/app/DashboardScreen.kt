package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.sport.PartitaConCanale
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.dashboard.RigaDashboard
import com.ir0.iptv.domain.dashboard.TipoRiga
import com.ir0.iptv.domain.playback.RegistroVisti
import com.ir0.iptv.domain.playback.Visto

private val registroVisti = RegistroVisti()

/** Le righe curate compaiono sempre, anche vuote: la Dashboard deve far capire cosa puo'
 * mostrare, non solo cosa mostra in questo momento. */
@Composable
fun DashboardScreen(
    righe: List<RigaDashboard>,
    visti: List<Visto>,
    chiaveDaFocalizzare: String?,
    catalogoVuoto: Boolean,
    sport: List<PartitaConCanale> = emptyList(),
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {}
) {
    if (catalogoVuoto) {
        SchermataVuota("Nessun contenuto trovato nelle Sorgenti configurate.")
        return
    }

    val statoColonna = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val indiceRigaDaFocalizzare = righe.indexOfFirst { riga ->
        riga.contenuti.any { it.chiaveIdentita == chiaveDaFocalizzare }
    }

    LaunchedEffect(chiaveDaFocalizzare, righe) {
        if (indiceRigaDaFocalizzare >= 0) {
            // +1: la fascia sport occupa il primo posto nella LazyColumn.
            statoColonna.scrollToItem(indiceRigaDaFocalizzare + 1)
            // La card puo' non essere ancora attaccata: in quel caso resta il focus di default.
            runCatching { focusRequester.requestFocus() }
        }
    }

    LazyColumn(
        state = statoColonna,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A)),
        contentPadding = PaddingValues(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        item {
            FasciaSport(partite = sport, onCanaleClick = onContenutoClick)
        }
        items(righe) { riga ->
            RigaContenuti(
                titolo = titoloDi(riga.tipo),
                contenuti = riga.contenuti,
                visti = visti,
                chiaveDaFocalizzare = chiaveDaFocalizzare,
                focusRequester = focusRequester,
                onClick = onContenutoClick,
                onLongClick = onContenutoLongClick,
                messaggioVuoto = messaggioVuotoDi(riga.tipo)
            )
        }
    }
}

@Composable
fun RigaContenuti(
    titolo: String,
    contenuti: List<ContentCard>,
    visti: List<Visto>,
    chiaveDaFocalizzare: String?,
    focusRequester: FocusRequester?,
    onClick: (ContentCard) -> Unit,
    onLongClick: (ContentCard) -> Unit = {},
    /** Quando non null, la riga resta visibile (con questo messaggio) anche a contenuti vuoti;
     * quando null, una riga vuota semplicemente non compare (comportamento delle righe di
     * catalogo su Sfoglia/Cerca/Sport, dove una sezione vuota non aggiunge nulla da leggere). */
    messaggioVuoto: String? = null
) {
    if (contenuti.isEmpty()) {
        if (messaggioVuoto == null) return
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = titolo,
                color = Color(0xFFF2F2F0),
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Text(
                text = messaggioVuoto,
                color = Color(0xFF6D7380),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
        return
    }

    val statoRiga = rememberLazyListState()
    val indiceDaFocalizzare = contenuti.indexOfFirst { it.chiaveIdentita == chiaveDaFocalizzare }

    LaunchedEffect(chiaveDaFocalizzare, contenuti) {
        if (indiceDaFocalizzare > 0) statoRiga.scrollToItem(indiceDaFocalizzare)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = titolo,
            color = Color(0xFFF2F2F0),
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        LazyRow(
            state = statoRiga,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) {
            items(contenuti) { card ->
                CardContenuto(
                    card = card,
                    percentuale = registroVisti.percentuale(visti, card.chiaveIdentita),
                    focusRequester = focusRequester.takeIf { card.chiaveIdentita == chiaveDaFocalizzare },
                    onClick = { onClick(card) },
                    onLongClick = { onLongClick(card) }
                )
            }
        }
    }
}

@Composable
fun SchermataVuota(messaggio: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
            .padding(32.dp)
    ) {
        Text(text = messaggio, color = Color(0xFF9AA0AA), fontSize = 16.sp)
    }
}

private fun titoloDi(tipo: TipoRiga): String = when (tipo) {
    TipoRiga.CONTINUA -> "Continua a guardare"
    TipoRiga.NUOVI_EPISODI -> "Nuovi episodi"
    TipoRiga.SUGGERITI -> "Suggeriti"
    TipoRiga.PREFERITI -> "Preferiti"
}

private fun messaggioVuotoDi(tipo: TipoRiga): String = when (tipo) {
    TipoRiga.CONTINUA -> "Quello che guardi appare qui, per riprendere da dove avevi lasciato."
    TipoRiga.NUOVI_EPISODI ->
        "Segui una Serie (guardala o aggiungila ai Preferiti) per vedere qui i nuovi episodi."
    TipoRiga.SUGGERITI -> "Aggiungi la chiave API di Claude dalle Impostazioni per ricevere suggerimenti."
    TipoRiga.PREFERITI -> "Nessun Preferito ancora: aggiungine uno dalla sua pagina di Dettaglio."
}
