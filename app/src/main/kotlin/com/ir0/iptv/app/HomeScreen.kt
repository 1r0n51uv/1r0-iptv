package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog

@Composable
fun HomeScreen(
    catalogo: ContentCatalog,
    onCanaleClick: (ContentCard.Canale) -> Unit,
    onFilmClick: (ContentCard.Film) -> Unit,
    onSerieClick: (ContentCard.SerieCard) -> Unit
) {
    MaterialTheme {
        Surface(color = Color(0xFF14161A)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF14161A)),
                contentPadding = PaddingValues(vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                item {
                    Text(
                        text = "1r0 IPTV",
                        color = Color(0xFFF2F2F0),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
                if (catalogo.isEmpty) {
                    item {
                        Text(
                            text = "Nessun contenuto trovato nelle Sorgenti configurate.",
                            color = Color(0xFF9AA0AA),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                } else {
                    if (catalogo.canali.isNotEmpty()) {
                        item { ContentRow(titolo = "Canali", items = catalogo.canali, onClick = onCanaleClick) }
                    }
                    if (catalogo.film.isNotEmpty()) {
                        item { ContentRow(titolo = "Film", items = catalogo.film, onClick = onFilmClick) }
                    }
                    if (catalogo.serie.isNotEmpty()) {
                        item { ContentRow(titolo = "Serie", items = catalogo.serie, onClick = onSerieClick) }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    MaterialTheme {
        Surface(color = Color(0xFF14161A)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF14161A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Caricamento contenuti…",
                    color = Color(0xFF9AA0AA),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun <T : ContentCard> ContentRow(titolo: String, items: List<T>, onClick: (T) -> Unit) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = titolo,
            color = Color(0xFFF2F2F0),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) {
            items(items) { item -> ContentCardView(card = item, onClick = { onClick(item) }) }
        }
    }
}

@Composable
private fun ContentCardView(card: ContentCard, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(110.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF262B33))
        ) {
            val imageUrl = card.imageUrl
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = card.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Text(
            text = card.title,
            color = Color(0xFFF2F2F0),
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
