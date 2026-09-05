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
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.playback.Visto

@Composable
fun CanaliScreen(
    catalogo: ContentCatalog,
    visti: List<Visto>,
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {}
) {
    SchermataCategoria("Canali", catalogo.canali, visti, onContenutoClick, onContenutoLongClick)
}

@Composable
fun FilmScreen(
    catalogo: ContentCatalog,
    visti: List<Visto>,
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {}
) {
    SchermataCategoria("Film", catalogo.film, visti, onContenutoClick, onContenutoLongClick)
}

@Composable
fun SerieScreen(
    catalogo: ContentCatalog,
    visti: List<Visto>,
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {}
) {
    SchermataCategoria("Serie", catalogo.serie, visti, onContenutoClick, onContenutoLongClick)
}

@Composable
private fun SchermataCategoria(
    titolo: String,
    contenuti: List<ContentCard>,
    visti: List<Visto>,
    onClick: (ContentCard) -> Unit,
    onLongClick: (ContentCard) -> Unit
) {
    if (contenuti.isEmpty()) {
        SchermataVuota("Nessun contenuto trovato in $titolo.")
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = titolo, color = Color(0xFFF2F2F0), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        GrigliaContenuti(
            contenuti = contenuti,
            visti = visti,
            onClick = onClick,
            onLongClick = onLongClick
        )
    }
}
