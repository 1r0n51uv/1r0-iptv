package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.playback.Visto

@Composable
fun FavoritesScreen(
    preferiti: List<ContentCard>,
    visti: List<Visto>,
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {}
) {
    if (preferiti.isEmpty()) {
        SchermataVuota("Nessun Preferito: aprine uno e premi Preferiti nella sua pagina di Dettaglio.")
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = "Preferiti", color = Color(0xFFF2F2F0), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        GrigliaContenuti(
            contenuti = preferiti,
            visti = visti,
            onClick = onContenutoClick,
            onLongClick = onContenutoLongClick
        )
    }
}
