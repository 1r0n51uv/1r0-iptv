package com.ir0.iptv.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog

@Composable
fun CanaliScreen(catalogo: ContentCatalog, activeIndex: Int, onSidebarClick: (Int) -> Unit, onCanaleClick: (ContentCard.Canale) -> Unit) {
    AppScaffold(activeIndex = activeIndex, onSidebarClick = onSidebarClick, backgroundImageUrl = catalogo.canali.firstOrNull()?.imageUrl) {
        CatalogGrid(
            titolo = "Canali",
            items = catalogo.canali,
            tall = false,
            onClick = onCanaleClick,
            emptyMessage = "Nessun canale trovato nelle Sorgenti configurate."
        )
    }
}

@Composable
fun FilmScreen(catalogo: ContentCatalog, activeIndex: Int, onSidebarClick: (Int) -> Unit, onFilmClick: (ContentCard.Film) -> Unit) {
    AppScaffold(activeIndex = activeIndex, onSidebarClick = onSidebarClick, backgroundImageUrl = catalogo.film.firstOrNull()?.imageUrl) {
        CatalogGrid(
            titolo = "Film",
            items = catalogo.film,
            tall = true,
            onClick = onFilmClick,
            emptyMessage = "Nessun film trovato nelle Sorgenti configurate."
        )
    }
}

@Composable
fun SerieScreen(catalogo: ContentCatalog, activeIndex: Int, onSidebarClick: (Int) -> Unit, onSerieClick: (ContentCard.SerieCard) -> Unit) {
    AppScaffold(activeIndex = activeIndex, onSidebarClick = onSidebarClick, backgroundImageUrl = catalogo.serie.firstOrNull()?.imageUrl) {
        CatalogGrid(
            titolo = "Serie TV",
            items = catalogo.serie,
            tall = true,
            onClick = onSerieClick,
            emptyMessage = "Nessuna serie trovata nelle Sorgenti configurate."
        )
    }
}

@Composable
fun PreferitiScreen(activeIndex: Int, onSidebarClick: (Int) -> Unit) {
    AppScaffold(activeIndex = activeIndex, onSidebarClick = onSidebarClick) {
        Column(
            modifier = Modifier.padding(horizontal = 36.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Preferiti", color = Color(0xFFF2F2F0), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "Non hai ancora aggiunto contenuti ai preferiti.",
                color = Color(0xFF9AA0AA),
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun <T : ContentCard> CatalogGrid(titolo: String, items: List<T>, tall: Boolean, onClick: (T) -> Unit, emptyMessage: String) {
    val firstItemFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (items.isNotEmpty()) firstItemFocusRequester.requestFocus()
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            text = titolo,
            color = Color(0xFFF2F2F0),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 36.dp, vertical = 16.dp)
        )
        if (items.isEmpty()) {
            Text(
                text = emptyMessage,
                color = Color(0xFF9AA0AA),
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 36.dp)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = if (tall) 148.dp else 200.dp),
                contentPadding = PaddingValues(horizontal = 36.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(items) { index, item ->
                    ContentCardView(
                        card = item,
                        tall = tall,
                        onClick = { onClick(item) },
                        focusRequester = if (index == 0) firstItemFocusRequester else null
                    )
                }
            }
        }
    }
}
