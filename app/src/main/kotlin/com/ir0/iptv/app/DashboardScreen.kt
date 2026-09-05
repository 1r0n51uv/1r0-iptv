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
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.dashboard.RigaDashboard
import com.ir0.iptv.domain.dashboard.TipoRiga
import com.ir0.iptv.domain.playback.RegistroVisti
import com.ir0.iptv.domain.playback.Visto

private val registroVisti = RegistroVisti()

@Composable
fun DashboardScreen(
    righe: List<RigaDashboard>,
    visti: List<Visto>,
    chiaveDaFocalizzare: String?,
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {}
) {
    if (righe.isEmpty()) {
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
            statoColonna.scrollToItem(indiceRigaDaFocalizzare)
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
        items(righe) { riga ->
            RigaContenuti(
                titolo = titoloDi(riga.tipo),
                contenuti = riga.contenuti,
                visti = visti,
                chiaveDaFocalizzare = chiaveDaFocalizzare,
                focusRequester = focusRequester,
                onClick = onContenutoClick,
                onLongClick = onContenutoLongClick
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
    onLongClick: (ContentCard) -> Unit = {}
) {
    if (contenuti.isEmpty()) return
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
    TipoRiga.SPORT -> "Sport in diretta"
    TipoRiga.PREFERITI -> "Preferiti"
    TipoRiga.CANALI -> "Canali"
    TipoRiga.FILM -> "Film"
    TipoRiga.SERIE -> "Serie"
}
