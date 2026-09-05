package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ir0.iptv.app.sport.PartitaConCanale
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.customization.ContentCustomization
import com.ir0.iptv.domain.playback.Visto

@Composable
fun SportScreen(
    catalogo: ContentCatalog,
    partite: List<PartitaConCanale>,
    visti: List<Visto>,
    personalizzazioni: Map<String, ContentCustomization> = emptyMap(),
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {}
) {
    val canaliSport = remember(catalogo) {
        catalogo.canali.filter { it.categoria?.contains("sport", ignoreCase = true) == true }
    }

    if (partite.isEmpty() && canaliSport.isEmpty()) {
        SchermataVuota(
            "Nessun evento in diretta al momento. Aggiungi la chiave API sport dalle Impostazioni " +
                "per vedere le partite del giorno."
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A)),
        contentPadding = PaddingValues(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        if (partite.isNotEmpty()) {
            item { FasciaSport(partite = partite, onCanaleClick = onContenutoClick) }
        }
        if (canaliSport.isNotEmpty()) {
            item {
                RigaContenuti(
                    titolo = "Canali Sport",
                    contenuti = canaliSport,
                    visti = visti,
                    personalizzazioni = personalizzazioni,
                    chiaveDaFocalizzare = null,
                    focusRequester = null,
                    onClick = onContenutoClick,
                    onLongClick = onContenutoLongClick
                )
            }
        }
    }
}
