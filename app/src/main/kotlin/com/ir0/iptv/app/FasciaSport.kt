package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.theme.LocalAccento
import com.ir0.iptv.app.sport.PartitaConCanale
import com.ir0.iptv.domain.catalog.ContentCard

@Composable
fun FasciaSport(partite: List<PartitaConCanale>, onCanaleClick: (ContentCard) -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Sport in diretta",
            color = Color(0xFFF2F2F0),
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            partite.forEach { conCanale ->
                CardPartita(conCanale = conCanale, onClick = { onCanaleClick(it) })
            }
        }
    }
}

@Composable
private fun CardPartita(conCanale: PartitaConCanale, onClick: (ContentCard.Canale) -> Unit) {
    val partita = conCanale.partita
    val canale = conCanale.canale
    var infocata by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .width(320.dp)
            .onFocusChanged { infocata = it.isFocused }
            .let { if (canale != null) it.clickable { onClick(canale) } else it }
            .background(Color(0xFF1F232A), RoundedCornerShape(10.dp))
            .border(
                2.dp,
                if (infocata) LocalAccento.current else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = listOfNotNull(partita.competizione, if (partita.inCorso) "IN CORSO" else null)
                .joinToString(" · "),
            color = if (partita.inCorso) LocalAccento.current else Color(0xFF6D7380),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${partita.casa} - ${partita.ospite}",
            color = Color(0xFFF2F2F0),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = when {
                partita.golCasa != null && partita.golOspite != null ->
                    "${partita.golCasa} - ${partita.golOspite}"

                else -> "—"
            },
            color = Color(0xFFC7CAD0),
            fontSize = 15.sp
        )
        Text(
            text = canale?.title ?: "Nessun Canale corrispondente nel catalogo",
            color = Color(0xFF6D7380),
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
