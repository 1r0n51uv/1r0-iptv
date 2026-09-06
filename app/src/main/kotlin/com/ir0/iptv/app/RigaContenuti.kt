package com.ir0.iptv.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.focus.FocusRequester
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ElencoPreferiti
import com.ir0.iptv.domain.customization.ContentCustomization
import com.ir0.iptv.domain.playback.RegistroVisti
import com.ir0.iptv.domain.playback.Visto

private val registroRiga = RegistroVisti()
private val preferitiRiga = ElencoPreferiti()

/**
 * Riga orizzontale di card a locandina. Su Sfoglia / Cerca / Sport la merchandising è il lavoro:
 * qui la card è la forma giusta. Sulla Home invece si usa un elenco (vedi [DashboardScreen]).
 */
@Composable
fun RigaContenuti(
    titolo: String,
    contenuti: List<ContentCard>,
    visti: List<Visto>,
    chiaveDaFocalizzare: String?,
    focusRequester: FocusRequester?,
    onClick: (ContentCard) -> Unit,
    onLongClick: (ContentCard) -> Unit = {},
    personalizzazioni: Map<String, ContentCustomization> = emptyMap(),
    /** Quando non null la riga resta visibile con questo messaggio anche a contenuti vuoti. */
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
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
        ) {
            items(contenuti) { card ->
                CardContenuto(
                    card = card,
                    percentuale = registroRiga.percentuale(visti, card.chiaveIdentita),
                    preferito = preferitiRiga.preferito(personalizzazioni, card),
                    focusRequester = focusRequester.takeIf { card.chiaveIdentita == chiaveDaFocalizzare },
                    onClick = { onClick(card) },
                    onLongClick = { onLongClick(card) }
                )
            }
        }
    }
}
