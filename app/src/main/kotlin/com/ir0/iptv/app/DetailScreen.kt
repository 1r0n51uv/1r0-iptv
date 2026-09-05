package com.ir0.iptv.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.theme.LocalAccento
import coil.compose.AsyncImage
import com.ir0.iptv.app.content.ContentFetcher
import com.ir0.iptv.app.playback.RichiestaRiproduzione
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.classification.Episodio
import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.classification.Stagione
import com.ir0.iptv.domain.playback.NavigazioneSerie
import com.ir0.iptv.domain.playback.ProssimaVisione
import com.ir0.iptv.domain.playback.ProssimaVisioneResolver
import com.ir0.iptv.domain.playback.RegistroVisti
import com.ir0.iptv.domain.playback.TipoVisto
import com.ir0.iptv.domain.playback.Visto

private val registro = RegistroVisti()
private val resolver = ProssimaVisioneResolver()
private val navigazione = NavigazioneSerie()

@Composable
fun DetailScreen(
    card: ContentCard,
    visti: List<Visto>,
    preferito: Boolean,
    onCambiaPreferito: () -> Unit,
    onRiproduci: (RichiestaRiproduzione, Long) -> Unit,
    onRiproduciCon: (RichiestaRiproduzione) -> Unit
) {
    when (card) {
        is ContentCard.Film -> DettaglioFilm(card, visti, preferito, onCambiaPreferito, onRiproduci, onRiproduciCon)

        is ContentCard.SerieCard.Pronta ->
            DettaglioSerie(card, card.serie, visti, preferito, onCambiaPreferito, onRiproduci, onRiproduciCon)

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
                serieCorrente != null -> DettaglioSerie(
                    card, serieCorrente, visti, preferito, onCambiaPreferito, onRiproduci, onRiproduciCon
                )

                fallita -> DettaglioErrore(card.title)
                else -> LoadingScreen()
            }
        }

        is ContentCard.Canale -> DettaglioErrore(card.title)
    }
}

@Composable
private fun DettaglioFilm(
    card: ContentCard.Film,
    visti: List<Visto>,
    preferito: Boolean,
    onCambiaPreferito: () -> Unit,
    onRiproduci: (RichiestaRiproduzione, Long) -> Unit,
    onRiproduciCon: (RichiestaRiproduzione) -> Unit
) {
    val richiesta = RichiestaRiproduzione(
        titolo = card.title,
        streamUrl = card.streamUrl,
        tipo = TipoVisto.FILM,
        posterUrl = card.imageUrl
    )
    val posizione = registro.posizioneDiRipresa(visti, card.chiaveIdentita)
    val percentuale = registro.percentuale(visti, card.chiaveIdentita)

    Pagina {
        Testata(
            copertina = card.imageUrl,
            etichetta = "FILM",
            coloreEtichetta = Color(0xFF3B82F6),
            titolo = card.title,
            meta = card.categoria,
            plot = card.plot
        ) {
            PulsanteAzione(
                testo = posizione?.let { "Riprendi da ${durata(it)}" } ?: "Riproduci",
                principale = true,
                onClick = { onRiproduci(richiesta, posizione ?: 0L) }
            )
            PulsantePreferito(preferito, onCambiaPreferito)
            PulsanteAzione(testo = "Riproduci con…", onClick = { onRiproduciCon(richiesta) })
        }
        if (percentuale > 0) {
            BarraProgresso(percentuale, modifier = Modifier.width(340.dp))
        }
    }
}

@Composable
private fun DettaglioSerie(
    card: ContentCard.SerieCard,
    serie: Serie,
    visti: List<Visto>,
    preferito: Boolean,
    onCambiaPreferito: () -> Unit,
    onRiproduci: (RichiestaRiproduzione, Long) -> Unit,
    onRiproduciCon: (RichiestaRiproduzione) -> Unit
) {
    val prossima = remember(serie, visti) { resolver.risolvi(serie, visti) }
    var stagioneSelezionata by remember(serie) {
        mutableStateOf(navigazione.stagioneIniziale(serie, visti))
    }

    fun richiestaDi(episodio: Episodio) = RichiestaRiproduzione(
        titolo = episodio.title,
        streamUrl = episodio.url,
        tipo = TipoVisto.EPISODIO,
        serie = serie.name,
        posterUrl = serie.poster ?: card.imageUrl
    )

    Pagina {
        Testata(
            copertina = serie.poster ?: card.imageUrl,
            etichetta = "SERIE",
            coloreEtichetta = Color(0xFF8B5CF6),
            titolo = serie.name,
            meta = listOfNotNull(
                serie.seasons.size.takeIf { it > 0 }?.let { "$it ${if (it == 1) "stagione" else "stagioni"}" },
                serie.seasons.sumOf { it.episodes.size }.takeIf { it > 0 }?.let { "$it episodi" }
            ).joinToString(" · ").ifBlank { null },
            plot = serie.plot
        ) {
            when (prossima) {
                is ProssimaVisione.Riprendi -> PulsanteAzione(
                    testo = "Riprendi ${prossima.episodio.title}",
                    principale = true,
                    onClick = { onRiproduci(richiestaDi(prossima.episodio), prossima.posizioneMs) }
                )

                is ProssimaVisione.Inizia -> PulsanteAzione(
                    testo = "Riproduci ${prossima.episodio.title}",
                    principale = true,
                    onClick = { onRiproduci(richiestaDi(prossima.episodio), 0L) }
                )

                ProssimaVisione.Completata -> {
                    val primo = serie.seasons.firstOrNull()?.episodes?.firstOrNull()
                    if (primo != null) {
                        PulsanteAzione(
                            testo = "Rivedi dall'inizio",
                            principale = true,
                            onClick = { onRiproduci(richiestaDi(primo), 0L) }
                        )
                    }
                }
            }
            PulsantePreferito(preferito, onCambiaPreferito)
        }

        if (serie.seasons.isNotEmpty()) {
            SelettoreStagioni(
                stagioni = serie.seasons,
                selezionata = stagioneSelezionata,
                onSeleziona = { stagioneSelezionata = it }
            )
        }

        val stagione = stagioneSelezionata ?: serie.seasons.firstOrNull()
        if (stagione != null) {
            if (stagione.number == null) {
                Text(
                    text = "Episodi rilevati dalla Sorgente senza numero di stagione o episodio riconoscibile.",
                    color = Color(0xFF6D7380),
                    fontSize = 13.sp
                )
            }
            CarouselEpisodi(
                episodi = stagione.episodes,
                posterSerie = serie.poster ?: card.imageUrl,
                visti = visti,
                onEpisodioClick = { episodio ->
                    onRiproduci(richiestaDi(episodio), registro.posizioneDiRipresa(visti, episodio.url) ?: 0L)
                },
                onEpisodioRiproduciCon = { episodio -> onRiproduciCon(richiestaDi(episodio)) }
            )
        }
    }
}

@Composable
private fun Pagina(contenuto: @Composable () -> Unit) {
    MaterialTheme {
        Surface(color = Color(0xFF14161A)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF14161A))
                    .padding(36.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                contenuto()
            }
        }
    }
}

@Composable
private fun Testata(
    copertina: String?,
    etichetta: String,
    coloreEtichetta: Color,
    titolo: String,
    meta: String?,
    plot: String?,
    azioni: @Composable () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(340.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF262B33))
        ) {
            if (copertina != null) {
                AsyncImage(
                    model = copertina,
                    contentDescription = titolo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = etichetta,
                color = Color(0xFF14161A),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(coloreEtichetta, RoundedCornerShape(4.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            )
            Text(text = titolo, color = Color(0xFFF2F2F0), fontSize = 30.sp, fontWeight = FontWeight.Bold)
            if (meta != null) {
                Text(text = meta, color = Color(0xFF9AA0AA), fontSize = 15.sp)
            }
            if (plot != null) {
                Text(
                    text = plot,
                    color = Color(0xFFC7CAD0),
                    fontSize = 14.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(640.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { azioni() }
        }
    }
}

@Composable
private fun PulsanteAzione(testo: String, principale: Boolean = false, onClick: () -> Unit) {
    var infocato by remember { mutableStateOf(false) }
    val sfondo = when {
        principale -> LocalAccento.current
        infocato -> Color(0xFF3A404A)
        else -> Color(0xFF262B33)
    }
    Text(
        text = testo,
        color = if (principale) Color(0xFF14161A) else Color(0xFFF2F2F0),
        fontSize = 14.sp,
        fontWeight = if (principale) FontWeight.Bold else FontWeight.SemiBold,
        modifier = Modifier
            .onFocusChanged { infocato = it.isFocused }
            .clickable(onClick = onClick)
            .background(sfondo, RoundedCornerShape(8.dp))
            .border(2.dp, if (infocato) Color(0xFFF2F2F0) else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    )
}

@Composable
private fun PulsantePreferito(preferito: Boolean, onClick: () -> Unit) {
    var infocato by remember { mutableStateOf(false) }
    Text(
        text = if (preferito) "★ Nei Preferiti" else "☆ Preferiti",
        color = if (preferito) LocalAccento.current else Color(0xFFF2F2F0),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .onFocusChanged { infocato = it.isFocused }
            .clickable(onClick = onClick)
            .background(Color(0xFF262B33), RoundedCornerShape(8.dp))
            .border(2.dp, if (infocato) Color(0xFFF2F2F0) else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    )
}

@Composable
private fun SelettoreStagioni(
    stagioni: List<Stagione>,
    selezionata: Stagione?,
    onSeleziona: (Stagione) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(stagioni) { stagione ->
            val attiva = stagione.number == selezionata?.number
            Text(
                text = stagione.number?.let { "Stagione $it" } ?: "Altri episodi",
                color = if (attiva) Color(0xFF14161A) else Color(0xFFC7CAD0),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { onSeleziona(stagione) }
                    .background(
                        if (attiva) LocalAccento.current else Color(0xFF1F232A),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            )
        }
    }
}

@Composable
private fun CarouselEpisodi(
    episodi: List<Episodio>,
    posterSerie: String?,
    visti: List<Visto>,
    onEpisodioClick: (Episodio) -> Unit,
    onEpisodioRiproduciCon: (Episodio) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(episodi) { episodio ->
            CardEpisodio(
                episodio = episodio,
                immagine = episodio.immagine ?: posterSerie,
                percentuale = registro.percentuale(visti, episodio.url),
                onClick = { onEpisodioClick(episodio) },
                onLongClick = { onEpisodioRiproduciCon(episodio) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CardEpisodio(
    episodio: Episodio,
    immagine: String?,
    percentuale: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var infocato by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .width(240.dp)
            .onFocusChanged { infocato = it.isFocused }
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(135.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF262B33))
                .border(
                    2.dp,
                    if (infocato) LocalAccento.current else Color.Transparent,
                    RoundedCornerShape(8.dp)
                )
        ) {
            if (immagine != null) {
                AsyncImage(
                    model = immagine,
                    contentDescription = episodio.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            if (percentuale > 0) {
                BarraProgresso(
                    percentuale = percentuale,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }
        Text(
            text = episodio.episodeNumber?.let { "$it. ${episodio.title}" } ?: episodio.title,
            color = Color(0xFFF2F2F0),
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BarraProgresso(percentuale: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(4.dp)
            .clip(CircleShape)
            .background(Color(0xFF3A3F48))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(percentuale / 100f)
                .fillMaxHeight()
                .background(LocalAccento.current)
        )
    }
}

@Composable
private fun DettaglioErrore(titolo: String) {
    Pagina {
        Text(text = titolo, color = Color(0xFFF2F2F0), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Impossibile caricare i dettagli di questo contenuto.",
            color = Color(0xFF9AA0AA),
            fontSize = 16.sp
        )
    }
}

private fun durata(ms: Long): String {
    val secondiTotali = ms / 1000
    val ore = secondiTotali / 3600
    val minuti = (secondiTotali % 3600) / 60
    val secondi = secondiTotali % 60
    return if (ore > 0) {
        "%d:%02d:%02d".format(ore, minuti, secondi)
    } else {
        "%d:%02d".format(minuti, secondi)
    }
}
