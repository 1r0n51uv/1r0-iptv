package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.settings.Impostazioni
import com.ir0.iptv.app.theme.Accento
import com.ir0.iptv.app.theme.LocalAccento

@Composable
fun ImpostazioniScreen(
    impostazioni: Impostazioni,
    indirizzoPannelloWeb: String?,
    onCambia: (Impostazioni) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Text("Impostazioni", color = Color(0xFFF2F2F0), fontSize = 22.sp, fontWeight = FontWeight.Bold)

        Sezione("Colore di accento") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Accento.entries.forEach { accento ->
                    PastigliaColore(
                        accento = accento,
                        scelto = accento == Accento.daNome(impostazioni.accento),
                        onClick = { onCambia(impostazioni.copy(accento = accento.name)) }
                    )
                }
            }
        }

        Sezione("Sport in diretta in Dashboard") {
            Interruttore(
                acceso = impostazioni.sportInDashboard,
                testo = if (impostazioni.sportInDashboard) "Attivo" else "Disattivato",
                onClick = { onCambia(impostazioni.copy(sportInDashboard = !impostazioni.sportInDashboard)) }
            )
            if (impostazioni.chiaveApiSport.isNullOrBlank()) {
                Nota("Serve la chiave del provider di dati sportivi, da inserire nel Pannello Web.")
            }
        }

        Sezione("Suggerimenti AI") {
            Nota(
                if (impostazioni.chiaveApiAi.isNullOrBlank()) {
                    "Nessuna chiave configurata: la riga Suggeriti resta vuota."
                } else {
                    "Chiave configurata."
                }
            )
        }

        Sezione("Contenuto di default") {
            Nota(
                impostazioni.contenutoDiDefault
                    ?: "Nessuno: all'avvio il focus va sull'ultimo contenuto aperto."
            )
        }

        Nota(
            "Chiavi e contenuto di default si impostano dal Pannello Web" +
                (indirizzoPannelloWeb?.let { ", su $it" } ?: "") + ": si digita meglio che col telecomando."
        )
    }
}

@Composable
private fun Sezione(titolo: String, contenuto: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(titolo, color = Color(0xFFF2F2F0), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        contenuto()
    }
}

@Composable
private fun Nota(testo: String) {
    Text(testo, color = Color(0xFF6D7380), fontSize = 13.sp)
}

@Composable
private fun PastigliaColore(accento: Accento, scelto: Boolean, onClick: () -> Unit) {
    var infocata by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .onFocusChanged { infocata = it.isFocused }
            .clickable(onClick = onClick)
            .background(if (scelto) Color(0xFF262B33) else Color(0xFF1A1D22), RoundedCornerShape(8.dp))
            .border(
                2.dp,
                if (infocata) Color(0xFFF2F2F0) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(accento.colore)
        ) {}
        Text(
            text = accento.etichetta,
            color = if (scelto) Color(0xFFF2F2F0) else Color(0xFFC7CAD0),
            fontSize = 14.sp,
            fontWeight = if (scelto) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun Interruttore(acceso: Boolean, testo: String, onClick: () -> Unit) {
    var infocato by remember { mutableStateOf(false) }
    Text(
        text = testo,
        color = if (acceso) Color(0xFF14161A) else Color(0xFFC7CAD0),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .onFocusChanged { infocato = it.isFocused }
            .clickable(onClick = onClick)
            .background(if (acceso) LocalAccento.current else Color(0xFF262B33), RoundedCornerShape(8.dp))
            .border(
                2.dp,
                if (infocato) Color(0xFFF2F2F0) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 18.dp, vertical = 10.dp)
    )
}
