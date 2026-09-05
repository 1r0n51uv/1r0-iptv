package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.theme.LocalAccento
import com.ir0.iptv.app.content.ContentFetcher
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.epg.GuidaTv
import com.ir0.iptv.domain.epg.Programma
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val guidaTv = GuidaTv()
private val oraDelGiorno = SimpleDateFormat("HH:mm", Locale.ITALY)

@Composable
fun GuidaTvScreen(
    catalogo: ContentCatalog,
    onCanaleClick: (ContentCard.Canale) -> Unit
) {
    val conEpg = remember(catalogo) { catalogo.canali.filter { it.xtream != null } }
    if (conEpg.isEmpty()) {
        SchermataVuota(
            "La Guida TV legge il palinsesto dalle Sorgenti Xtream: nessun Canale Xtream configurato."
        )
        return
    }

    var selezionato by remember(conEpg) { mutableStateOf(conEpg.first()) }
    var palinsesto by remember(selezionato) { mutableStateOf<List<Programma>?>(null) }
    LaunchedEffect(selezionato) {
        palinsesto = selezionato.xtream?.let { ContentFetcher().palinsesto(it) }.orEmpty()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
            .padding(32.dp),
        horizontalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        LazyColumn(
            modifier = Modifier.width(300.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(conEpg) { canale ->
                VoceCanale(
                    canale = canale,
                    attivo = canale.chiaveIdentita == selezionato.chiaveIdentita,
                    onFocus = { selezionato = canale },
                    onClick = { onCanaleClick(canale) }
                )
            }
        }
        Palinsesto(titolo = selezionato.title, programmi = palinsesto)
    }
}

@Composable
private fun VoceCanale(
    canale: ContentCard.Canale,
    attivo: Boolean,
    onFocus: () -> Unit,
    onClick: () -> Unit
) {
    var infocato by remember { mutableStateOf(false) }
    Text(
        text = canale.title,
        color = if (attivo) LocalAccento.current else Color(0xFFC7CAD0),
        fontSize = 15.sp,
        fontWeight = if (attivo) FontWeight.SemiBold else FontWeight.Normal,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                infocato = it.isFocused
                if (it.isFocused) onFocus()
            }
            .clickable(onClick = onClick)
            .background(if (attivo) Color(0xFF1F232A) else Color.Transparent, RoundedCornerShape(8.dp))
            .border(2.dp, if (infocato) Color(0xFFF2F2F0) else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    )
}

@Composable
private fun Palinsesto(titolo: String, programmi: List<Programma>?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = titolo, color = Color(0xFFF2F2F0), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        when {
            programmi == null -> Text("Caricamento palinsesto…", color = Color(0xFF6D7380), fontSize = 15.sp)

            programmi.isEmpty() -> Text(
                text = "Nessun palinsesto disponibile per questo Canale.",
                color = Color(0xFF9AA0AA),
                fontSize = 15.sp
            )

            else -> {
                val ora = System.currentTimeMillis()
                val inOnda = guidaTv.inOnda(programmi, ora)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(programmi) { programma ->
                        VoceProgramma(
                            programma = programma,
                            inOnda = programma == inOnda,
                            percentuale = if (programma == inOnda) guidaTv.percentuale(programma, ora) else 0
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoceProgramma(programma: Programma, inOnda: Boolean, percentuale: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (inOnda) Color(0xFF1F232A) else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = oraDelGiorno.format(Date(programma.inizioMs)),
                color = if (inOnda) LocalAccento.current else Color(0xFF6D7380),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = programma.titolo,
                color = Color(0xFFF2F2F0),
                fontSize = 15.sp,
                fontWeight = if (inOnda) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (inOnda) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3A3F48))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentuale / 100f)
                        .fillMaxHeight()
                        .background(LocalAccento.current)
                )
            }
        }
    }
}
