package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.playback.Visto

@Composable
fun BrowseScreen(
    catalogo: ContentCatalog,
    visti: List<Visto>,
    chiaveDaFocalizzare: String?,
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {}
) {
    if (catalogo.isEmpty) {
        SchermataVuota("Nessun contenuto trovato nelle Sorgenti configurate.")
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A)),
        contentPadding = PaddingValues(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        item {
            RigaContenuti(
                titolo = "Canali",
                contenuti = catalogo.canali,
                visti = visti,
                chiaveDaFocalizzare = chiaveDaFocalizzare,
                focusRequester = null,
                onClick = onContenutoClick,
                onLongClick = onContenutoLongClick
            )
        }
        item {
            RigaContenuti(
                titolo = "Film",
                contenuti = catalogo.film,
                visti = visti,
                chiaveDaFocalizzare = chiaveDaFocalizzare,
                focusRequester = null,
                onClick = onContenutoClick,
                onLongClick = onContenutoLongClick
            )
        }
        item {
            RigaContenuti(
                titolo = "Serie",
                contenuti = catalogo.serie,
                visti = visti,
                chiaveDaFocalizzare = chiaveDaFocalizzare,
                focusRequester = null,
                onClick = onContenutoClick,
                onLongClick = onContenutoLongClick
            )
        }
    }
}

/** Scheletro delle righe invece di una scritta al centro: il catalogo di una Sorgente grande
 * ci mette parecchio, e uno schermo quasi vuoto sembra un'app bloccata. */
@Composable
fun LoadingScreen(messaggio: String = "Caricamento contenuti…") {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Text(text = messaggio, color = Color(0xFF9AA0AA), fontSize = 16.sp)
        repeat(2) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1F232A))
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(5) {
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .height(112.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1F232A))
                        )
                    }
                }
            }
        }
    }
}
