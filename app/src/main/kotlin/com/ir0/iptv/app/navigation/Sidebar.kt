package com.ir0.iptv.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.theme.LocalAccento

@Composable
fun Sidebar(
    selezionata: Destinazione,
    onSeleziona: (Destinazione) -> Unit,
    inAggiornamento: Boolean = false,
    onAggiorna: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(210.dp)
            .fillMaxHeight()
            .background(Color(0xFF1A1D22))
            .padding(vertical = 32.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "1r0 IPTV",
            color = LocalAccento.current,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 12.dp, bottom = 18.dp)
        )
        Destinazione.entries.forEach { destinazione ->
            VoceSidebar(
                etichetta = destinazione.etichetta,
                attiva = destinazione == selezionata,
                onClick = { onSeleziona(destinazione) }
            )
        }
        VoceSidebar(
            etichetta = if (inAggiornamento) "Aggiornamento…" else "Aggiorna catalogo",
            attiva = false,
            onClick = { if (!inAggiornamento) onAggiorna() },
            modifier = Modifier.padding(top = 18.dp)
        )
    }
}

@Composable
private fun VoceSidebar(
    etichetta: String,
    attiva: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var infocata by remember { mutableStateOf(false) }
    Text(
        text = etichetta,
        color = if (attiva) Color(0xFF14161A) else Color(0xFFC7CAD0),
        fontSize = 15.sp,
        fontWeight = if (attiva) FontWeight.Bold else FontWeight.Medium,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { infocata = it.isFocused }
            .clickable(onClick = onClick)
            .background(
                if (attiva) LocalAccento.current else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .border(
                2.dp,
                if (infocata) Color(0xFFF2F2F0) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 11.dp)
    )
}
