package com.ir0.iptv.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog

@Composable
fun HomeScreen(
    catalogo: ContentCatalog,
    activeIndex: Int,
    onSidebarClick: (Int) -> Unit,
    onCanaleClick: (ContentCard.Canale) -> Unit,
    onFilmClick: (ContentCard.Film) -> Unit,
    onSerieClick: (ContentCard.SerieCard) -> Unit
) {
    val hero: ContentCard? = catalogo.serie.firstOrNull() ?: catalogo.film.firstOrNull() ?: catalogo.canali.firstOrNull()

    AppScaffold(activeIndex = activeIndex, onSidebarClick = onSidebarClick, backgroundImageUrl = hero?.imageUrl) {
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

    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(targetValue = if (isFocused) 1.05f else 1f, label = "heroButtonScale")
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

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
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFFB454))
                .then(
                    if (isFocused) Modifier.border(3.dp, Color(0xFFF2F2F0), RoundedCornerShape(8.dp)) else Modifier
                )
                .focusRequester(focusRequester)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
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
