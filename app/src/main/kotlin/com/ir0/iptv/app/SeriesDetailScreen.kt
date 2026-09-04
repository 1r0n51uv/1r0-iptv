package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.content.ContentCard
import com.ir0.iptv.app.content.ContentFetcher
import com.ir0.iptv.domain.classification.Episodio
import com.ir0.iptv.domain.classification.Serie

@Composable
fun SeriesDetailScreen(card: ContentCard.SerieCard, onEpisodioClick: (Episodio) -> Unit) {
    when (card) {
        is ContentCard.SerieCard.Pronta -> SeriesDetailContent(card.serie, onEpisodioClick)
        is ContentCard.SerieCard.DaCaricare -> {
            var serie by remember(card) { mutableStateOf<Serie?>(null) }
            var fallita by remember(card) { mutableStateOf(false) }
            LaunchedEffect(card) {
                val risultato = ContentFetcher().dettaglioSerie(card)
                serie = risultato
                fallita = risultato == null
            }
            val serieCorrente = serie
            when {
                serieCorrente != null -> SeriesDetailContent(serieCorrente, onEpisodioClick)
                fallita -> SeriesDetailError(card.title)
                else -> LoadingScreen()
            }
        }
    }
}

@Composable
private fun SeriesDetailError(titolo: String) {
    MaterialTheme {
        Surface(color = Color(0xFF14161A)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF14161A))
                    .padding(32.dp)
            ) {
                item {
                    Text(
                        text = titolo,
                        color = Color(0xFFF2F2F0),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    Text(
                        text = "Impossibile caricare gli episodi di questa Serie.",
                        color = Color(0xFF9AA0AA),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SeriesDetailContent(serie: Serie, onEpisodioClick: (Episodio) -> Unit) {
    MaterialTheme {
        Surface(color = Color(0xFF14161A)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF14161A))
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Text(
                        text = serie.name,
                        color = Color(0xFFF2F2F0),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                serie.seasons.forEach { stagione ->
                    item {
                        Text(
                            text = stagione.number?.let { "Stagione $it" } ?: "Senza numero",
                            color = Color(0xFFFFB454),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(top = 14.dp, bottom = 6.dp)
                        )
                    }
                    items(stagione.episodes) { episodio ->
                        Text(
                            text = episodio.episodeNumber?.let { "$it. ${episodio.title}" } ?: episodio.title,
                            color = Color(0xFFF2F2F0),
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEpisodioClick(episodio) }
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
