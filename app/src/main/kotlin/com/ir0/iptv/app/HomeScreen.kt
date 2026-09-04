package com.ir0.iptv.app

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ir0.iptv.app.content.ContentCard
import com.ir0.iptv.app.content.ContentCatalog
import com.ir0.iptv.app.util.DownsampleBlurTransformation

@Composable
fun HomeScreen(
    catalogo: ContentCatalog,
    onCanaleClick: (ContentCard.Canale) -> Unit,
    onFilmClick: (ContentCard.Film) -> Unit,
    onSerieClick: (ContentCard.SerieCard) -> Unit
) {
    val hero: ContentCard? = catalogo.serie.firstOrNull() ?: catalogo.film.firstOrNull() ?: catalogo.canali.firstOrNull()

    MaterialTheme {
        Surface(color = Color(0xFF14161A)) {
            Box(Modifier.fillMaxSize()) {
                if (hero?.imageUrl != null) {
                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(hero.imageUrl)
                            .transformations(DownsampleBlurTransformation())
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.45f
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xCC14161A))
                    )
                }

                Row(Modifier.fillMaxSize()) {
                    Sidebar(activeIndex = 0)
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        TopBar()
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            if (hero != null) {
                                item {
                                    HeroSection(
                                        hero = hero,
                                        onCanaleClick = onCanaleClick,
                                        onFilmClick = onFilmClick,
                                        onSerieClick = onSerieClick
                                    )
                                }
                            }
                            if (catalogo.isEmpty) {
                                item {
                                    Text(
                                        text = "Nessun contenuto trovato nelle Sorgenti configurate.",
                                        color = Color(0xFF9AA0AA),
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(horizontal = 36.dp)
                                    )
                                }
                            } else {
                                if (catalogo.canali.isNotEmpty()) {
                                    item { ContentRow(titolo = "Canali", items = catalogo.canali, onClick = onCanaleClick) }
                                }
                                if (catalogo.film.isNotEmpty()) {
                                    item { ContentRow(titolo = "Film", items = catalogo.film, onClick = onFilmClick, tall = true) }
                                }
                                if (catalogo.serie.isNotEmpty()) {
                                    item { ContentRow(titolo = "Serie", items = catalogo.serie, onClick = onSerieClick, tall = true) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 36.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "1r0 IPTV", color = Color(0xFFF2F2F0), fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun HeroSection(
    hero: ContentCard,
    onCanaleClick: (ContentCard.Canale) -> Unit,
    onFilmClick: (ContentCard.Film) -> Unit,
    onSerieClick: (ContentCard.SerieCard) -> Unit
) {
    val badge = when (hero) {
        is ContentCard.Canale -> "CANALE"
        is ContentCard.Film -> "FILM"
        is ContentCard.SerieCard -> "SERIE"
    }
    val plot = when (hero) {
        is ContentCard.Film -> hero.plot
        is ContentCard.SerieCard.DaCaricare -> hero.plot
        else -> null
    }
    val buttonLabel = if (hero is ContentCard.SerieCard) "Vai alla Serie" else "Riproduci"
    val onClick: () -> Unit = when (hero) {
        is ContentCard.Canale -> ({ onCanaleClick(hero) })
        is ContentCard.Film -> ({ onFilmClick(hero) })
        is ContentCard.SerieCard -> ({ onSerieClick(hero) })
    }

    Column(
        modifier = Modifier.padding(horizontal = 36.dp).widthIn(max = 600.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = badge, color = Color(0xFFFFB454), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(text = hero.title, color = Color(0xFFF2F2F0), fontSize = 30.sp, fontWeight = FontWeight.Black)
        if (plot != null) {
            Text(
                text = plot,
                color = Color(0xFFC7CAD0),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFFB454))
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Text(text = buttonLabel, color = Color(0xFF14161A), fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
private fun <T : ContentCard> ContentRow(titolo: String, items: List<T>, onClick: (T) -> Unit, tall: Boolean = false) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = titolo,
            color = Color(0xFFF2F2F0),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 36.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(horizontal = 36.dp)
        ) {
            items(items) { item -> ContentCardView(card = item, tall = tall, onClick = { onClick(item) }) }
        }
    }
}

@Composable
private fun ContentCardView(card: ContentCard, tall: Boolean, onClick: () -> Unit) {
    val cardWidth = if (tall) 148.dp else 200.dp
    val cardHeight = if (tall) 210.dp else 110.dp
    Column(
        modifier = Modifier.width(cardWidth).clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
                .clip(RoundedCornerShape(10.dp))
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
            fontSize = 14.sp,
            maxLines = if (tall) 2 else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(cardWidth)
        )
    }
}
