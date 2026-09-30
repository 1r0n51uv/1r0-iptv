package com.ir0.iptv.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import com.ir0.iptv.app.theme.Palette
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.theme.LocalAccento
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ir0.iptv.app.content.ContentFetcher
import com.ir0.iptv.app.content.DettaglioCache
import com.ir0.iptv.app.util.DownsampleBlurTransformation
import com.ir0.iptv.app.playback.RichiestaRiproduzione
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.DettaglioEsteso
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

/**
 * Riporta la pagina di Dettaglio in cima, tenendocela per qualche frame: dare il focus al
 * pulsante Play innesca un bring-into-view che sposta la pagina e taglia la cover, e sopra il
 * pulsante non c'è nulla di focusabile con cui risalire col D-pad. Il ciclo su più frame ha la
 * meglio su quell'animazione, che può ripartire subito dopo il primo scrollTo.
 */
private suspend fun ScrollState.riportaInCimaPerQualcheFrame() {
    repeat(6) {
        withFrameNanos {}
        if (value != 0) scrollTo(0)
    }
}

@Composable
fun DetailScreen(
    card: ContentCard,
    visti: List<Visto>,
    preferito: Boolean,
    onCambiaPreferito: () -> Unit,
    onRiproduci: (RichiestaRiproduzione, Long, List<RichiestaRiproduzione>) -> Unit,
    onRiproduciCon: (RichiestaRiproduzione) -> Unit,
    /** Azzera i Visti la cui Chiave di Identita' e' tra quelle date: un singolo Episodio o
     * tutti gli Episodi di una Stagione (vedi VistoRepository.rimuoviVisti). */
    onResetVisti: (Set<String>) -> Unit = {}
) {
    when (card) {
        is ContentCard.Film -> DettaglioFilm(card, visti, preferito, onCambiaPreferito, onRiproduci, onRiproduciCon)

        is ContentCard.SerieCard.Pronta ->
            DettaglioSerie(card, card.serie, visti, preferito, onCambiaPreferito, onRiproduci, onRiproduciCon, onResetVisti)

        is ContentCard.SerieCard.DaCaricare -> {
            var serie by remember(card) { mutableStateOf(DettaglioCache.serie(card.chiaveIdentita)) }
            var fallita by remember(card) { mutableStateOf(false) }
            LaunchedEffect(card) {
                if (serie != null) return@LaunchedEffect
                val risultato = ContentFetcher().dettaglioSerie(card)
                if (risultato != null) DettaglioCache.salvaSerie(card.chiaveIdentita, risultato)
                serie = risultato
                fallita = risultato == null
            }
            val serieCorrente = serie
            when {
                serieCorrente != null -> DettaglioSerie(
                    card, serieCorrente, visti, preferito, onCambiaPreferito, onRiproduci, onRiproduciCon, onResetVisti
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
    onRiproduci: (RichiestaRiproduzione, Long, List<RichiestaRiproduzione>) -> Unit,
    onRiproduciCon: (RichiestaRiproduzione) -> Unit
) {
    val richiesta = RichiestaRiproduzione(
        titolo = card.title,
        streamUrl = card.streamUrl,
        tipo = TipoVisto.FILM,
        posterUrl = card.imageUrl,
        chiaveIdentita = card.chiaveIdentita
    )
    val posizione = registro.posizioneDiRipresa(visti, card.chiaveIdentita)
    val percentuale = registro.percentuale(visti, card.chiaveIdentita)
    val focusPrincipale = remember(card) { FocusRequester() }

    var dettagli by remember(card) { mutableStateOf(DettaglioCache.film(card.chiaveIdentita)) }
    // Le Sorgenti M3U non hanno un DettaglioEsteso da recuperare: niente scheletro per loro.
    var caricandoDettagli by remember(card) { mutableStateOf(dettagli == null && card.xtream != null) }
    LaunchedEffect(card) {
        if (dettagli != null) {
            caricandoDettagli = false
            return@LaunchedEffect
        }
        val risultato = card.xtream?.let { ContentFetcher().dettaglioFilm(it) }
        if (risultato != null) DettaglioCache.salvaFilm(card.chiaveIdentita, risultato)
        dettagli = risultato
        caricandoDettagli = false
    }

    Pagina(sfondo = card.imageUrl, focusIniziale = focusPrincipale, chiaveRifocus = card.chiaveIdentita) { statoScorrimento ->
        val scope = rememberCoroutineScope()
        val riportaInCima: () -> Unit = { scope.launch { statoScorrimento.riportaInCimaPerQualcheFrame() } }
        Testata(
            copertina = card.imageUrl,
            etichetta = "FILM",
            coloreEtichetta = Color(0xFF3B82F6),
            titolo = card.title,
            meta = card.categoria,
            plot = card.plot ?: dettagli?.trama,
            dettagli = dettagli,
            caricandoDettagli = caricandoDettagli
        ) {
            PulsanteAzione(
                // Un Film mai visto e' "Play"; ripreso mostra solo "Riprendi", senza il minutaggio.
                testo = if (posizione != null) "Riprendi" else "Play",
                principale = true,
                icona = Icons.Filled.PlayArrow,
                focusRequester = focusPrincipale,
                onInfocato = riportaInCima,
                onClick = { onRiproduci(richiesta, posizione ?: 0L, emptyList()) }
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
    onRiproduci: (RichiestaRiproduzione, Long, List<RichiestaRiproduzione>) -> Unit,
    onRiproduciCon: (RichiestaRiproduzione) -> Unit,
    onResetVisti: (Set<String>) -> Unit
) {
    val prossima = remember(serie, visti) { resolver.risolvi(serie, visti) }
    var stagioneSelezionata by remember(serie) {
        mutableStateOf(navigazione.stagioneIniziale(serie, visti))
    }
    val focusPrincipale = remember(serie) { FocusRequester() }

    val primoEpisodio = serie.seasons.flatMap { it.episodes }.firstOrNull()

    /** "S{stagione} E{episodio}" quando entrambi i numeri sono noti (le Sorgenti M3U senza
     * pattern riconoscibile non li hanno). */
    fun siglaDi(episodio: Episodio): String? {
        val stagione = serie.seasons.firstOrNull { st -> st.episodes.any { it.url == episodio.url } }?.number
        val numero = episodio.episodeNumber
        return if (stagione != null && numero != null) "S$stagione E$numero" else null
    }

    fun etichettaRiprendi(episodio: Episodio) = "Riprendi" + (siglaDi(episodio)?.let { " $it" } ?: "")

    /** Il primo Episodio in assoluto e' un "Play" secco (come un Film mai visto); dal secondo in
     * poi si aggiunge la sigla, cosi' si sa da dove riparte senza aprire la lista. */
    fun etichettaPlay(episodio: Episodio) =
        if (episodio.url == primoEpisodio?.url) "Play" else "Play" + (siglaDi(episodio)?.let { " $it" } ?: "")

    fun richiestaDi(episodio: Episodio) = RichiestaRiproduzione(
        titolo = episodio.title,
        streamUrl = episodio.url,
        tipo = TipoVisto.EPISODIO,
        serie = serie.name,
        chiaveIdentita = episodio.chiaveIdentita,
        // Immagine: prima l'Episodio, poi la copertina della Stagione, poi la locandina della Serie.
        posterUrl = episodio.immagine
            ?: navigazione.stagioneDi(serie, episodio)?.immagine
            ?: serie.poster
            ?: card.imageUrl
    )

    /** Gli Episodi dopo questo, gia' pronti da riprodurre: il player li fa partire da solo
     * quando la riproduzione arriva in fondo. */
    fun codaDopo(episodio: Episodio): List<RichiestaRiproduzione> =
        navigazione.episodiSuccessivi(serie, episodio.url).map(::richiestaDi)

    Pagina(
        sfondo = serie.poster ?: card.imageUrl,
        focusIniziale = focusPrincipale,
        chiaveRifocus = prossima
    ) { statoScorrimento ->
        val scope = rememberCoroutineScope()
        val riportaInCima: () -> Unit = { scope.launch { statoScorrimento.riportaInCimaPerQualcheFrame() } }
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
                    testo = etichettaRiprendi(prossima.episodio),
                    principale = true,
                    icona = Icons.Filled.PlayArrow,
                    focusRequester = focusPrincipale,
                    onInfocato = riportaInCima,
                    onClick = {
                        onRiproduci(
                            richiestaDi(prossima.episodio),
                            prossima.posizioneMs,
                            codaDopo(prossima.episodio)
                        )
                    }
                )

                is ProssimaVisione.Inizia -> PulsanteAzione(
                    testo = etichettaPlay(prossima.episodio),
                    principale = true,
                    icona = Icons.Filled.PlayArrow,
                    focusRequester = focusPrincipale,
                    onInfocato = riportaInCima,
                    onClick = {
                        onRiproduci(richiestaDi(prossima.episodio), 0L, codaDopo(prossima.episodio))
                    }
                )

                ProssimaVisione.Completata -> {
                    if (primoEpisodio != null) {
                        PulsanteAzione(
                            testo = "Play",
                            principale = true,
                            icona = Icons.Filled.PlayArrow,
                            focusRequester = focusPrincipale,
                            onInfocato = riportaInCima,
                            onClick = { onRiproduci(richiestaDi(primoEpisodio), 0L, codaDopo(primoEpisodio)) }
                        )
                    }
                }
            }
            PulsantePreferito(preferito, onCambiaPreferito)
            // Sulle card (Dashboard/Sfoglia) c'e' gia' "Togli da Continua a guardare", ma solo
            // quando la Serie vi compare. Qui, sulla sua pagina, l'azione e' sempre a portata di
            // mano quando c'e' almeno un Episodio visto: utile per un rewatch o per pulizia,
            // senza dover prima trovare la card giusta altrove.
            if (visti.any { it.serie == serie.name }) {
                PulsanteAzione(
                    testo = "Segna serie come non vista",
                    icona = Icons.Filled.RemoveCircleOutline,
                    onClick = {
                        onResetVisti(serie.seasons.flatMap { it.episodes }.map { it.chiaveIdentita }.toSet())
                    }
                )
            }
        }

        if (serie.seasons.isNotEmpty()) {
            SelettoreStagioni(
                stagioni = serie.seasons,
                selezionata = stagioneSelezionata,
                visti = visti,
                onSeleziona = { stagioneSelezionata = it },
                onSegnaStagioneNonVista = { stagione ->
                    onResetVisti(stagione.episodes.map { it.chiaveIdentita }.toSet())
                }
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
                    onRiproduci(
                        richiestaDi(episodio),
                        registro.posizioneDiRipresa(visti, episodio.chiaveIdentita) ?: 0L,
                        codaDopo(episodio)
                    )
                },
                onEpisodioRiproduciCon = { episodio -> onRiproduciCon(richiestaDi(episodio)) },
                onEpisodioSegnaNonVisto = { episodio -> onResetVisti(setOf(episodio.chiaveIdentita)) }
            )
        }
    }
}

/**
 * @param focusIniziale pulsante su cui portare il D-pad all'apertura (Play/Riprendi).
 * @param chiaveRifocus quando cambia, si rifà il focus iniziale (es. si torna dal player e la
 *   prossima visione è diversa).
 * @param contenuto riceve lo [ScrollState] della pagina: il pulsante principale lo usa per
 *   riportare in cima la cover ogni volta che prende il focus (all'apertura e risalendo dagli
 *   episodi), visto che sopra di lui non c'è nulla di focusabile con cui scorrere all'insù.
 */
@Composable
private fun Pagina(
    sfondo: String? = null,
    focusIniziale: FocusRequester? = null,
    chiaveRifocus: Any? = null,
    contenuto: @Composable (ScrollState) -> Unit
) {
    val statoScorrimento = rememberScrollState()
    if (focusIniziale != null) {
        LaunchedEffect(focusIniziale, chiaveRifocus) {
            withFrameNanos {}
            // Il focus sul pulsante principale fa scattare da solo `riportaInCima` (onInfocato),
            // che rimette la cover in cima.
            runCatching { focusIniziale.requestFocus() }
        }
    }
    MaterialTheme {
        Surface(color = Color(0xFF0A0B0E)) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (sfondo != null) {
                    // La cover del contenuto, appena sfocata, allargata a destra e sfumata verso
                    // sinistra e verso il basso: fa da scenografia alla pagina senza coprire il testo.
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(sfondo)
                            .transformations(DownsampleBlurTransformation(targetWidth = 480, radius = 2, passes = 1))
                            .crossfade(600)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .fillMaxWidth(0.7f)
                            .fillMaxHeight(0.85f)
                            .sfumaBordiScenografia(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    0f to Palette.inchiostro,
                                    0.35f to Palette.inchiostro.copy(alpha = 0.9f),
                                    0.7f to Palette.inchiostro.copy(alpha = 0.45f),
                                    1f to Palette.inchiostro.copy(alpha = 0.25f)
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    0.45f to Color.Transparent,
                                    0.85f to Palette.inchiostro.copy(alpha = 0.95f),
                                    1f to Palette.inchiostro
                                )
                            )
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(statoScorrimento)
                        .padding(36.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    contenuto(statoScorrimento)
                }
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
    dettagli: DettaglioEsteso? = null,
    /** In attesa del DettaglioEsteso (solo Film da Sorgenti Xtream): mostra uno scheletro al
     * posto del pannello cast/regista/genere, invece di uno spazio vuoto che poi "salta" fuori. */
    caricandoDettagli: Boolean = false,
    azioni: @Composable () -> Unit
) {
    val entrata = remember(titolo) { Animatable(0f) }
    LaunchedEffect(titolo) { entrata.animateTo(1f, tween(durationMillis = 800, easing = EasingCinema)) }
    Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(340.dp)
                .graphicsLayer {
                    alpha = entrata.value
                    val scala = 0.94f + 0.06f * entrata.value
                    scaleX = scala
                    scaleY = scala
                }
                .shadow(elevation = 36.dp, shape = RoundedCornerShape(12.dp), clip = false)
                .clip(RoundedCornerShape(12.dp))
                .background(Palette.superficieAlta)
        ) {
            if (copertina != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(copertina).crossfade(400).build(),
                    contentDescription = titolo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = etichetta,
                color = Color(0xFF0A0B0E),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(coloreEtichetta, RoundedCornerShape(4.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            )
            Text(
                text = titolo,
                color = Palette.testo,
                fontSize = 42.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.8).sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(680.dp)
            )
            if (meta != null) {
                Text(text = meta, color = Color(0xFF9AA0AA), fontSize = 15.sp)
            }
            if (plot != null) {
                Text(
                    text = plot,
                    color = Color(0xFFC7CAD0),
                    fontSize = 16.sp,
                    lineHeight = 23.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(640.dp)
                )
            }
            if (dettagli != null && !dettagli.isEmpty) {
                DettagliEstesi(dettagli)
            } else if (caricandoDettagli) {
                ScheletroDettagliEstesi()
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { azioni() }
        }
    }
}

@Composable
private fun ScheletroDettagliEstesi() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(13.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E2027))
        )
        repeat(2) {
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(13.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF1E2027))
            )
        }
    }
}

@Composable
private fun DettagliEstesi(dettagli: DettaglioEsteso) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val riepilogo = listOfNotNull(
            dettagli.anno,
            dettagli.durata,
            dettagli.valutazione?.let { "★ %.1f".format(it) }
        ).joinToString(" · ")
        if (riepilogo.isNotBlank()) {
            Text(text = riepilogo, color = Color(0xFF9AA0AA), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        dettagli.regista?.let { RigaMetaEstesa("Regia", it) }
        dettagli.cast?.let { RigaMetaEstesa("Cast", it) }
        dettagli.genere?.let { RigaMetaEstesa("Genere", it) }
    }
}

@Composable
private fun RigaMetaEstesa(etichetta: String, valore: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "$etichetta:", color = Color(0xFF6D7380), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(
            text = valore,
            color = Color(0xFFC7CAD0),
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(520.dp)
        )
    }
}

@Composable
private fun PulsanteAzione(
    testo: String,
    principale: Boolean = false,
    icona: ImageVector? = null,
    focusRequester: FocusRequester? = null,
    onInfocato: () -> Unit = {},
    onClick: () -> Unit
) {
    var infocato by remember { mutableStateOf(false) }
    // A fuoco qualunque pulsante diventa bianco pieno (testo inchiostro); a riposo il principale
    // resta d'accento, gli altri sono "vetro" traslucido sopra la scenografia.
    val sfondo by animateColorAsState(
        targetValue = when {
            infocato -> Palette.testo
            principale -> LocalAccento.current
            else -> Palette.testo.copy(alpha = 0.14f)
        },
        animationSpec = tween(durationMillis = 180),
        label = "fondoPulsanteAzione"
    )
    val colore = if (infocato) Palette.inchiostro else Palette.testo
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .widthIn(max = 420.dp)
            .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
            .onFocusChanged {
                infocato = it.isFocused
                if (it.isFocused) onInfocato()
            }
            .zoomInFocus(infocato, RoundedCornerShape(10.dp), scalaMax = 1.06f, ombraMax = 14.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(sfondo)
            .padding(horizontal = 22.dp, vertical = 11.dp)
    ) {
        if (icona != null) {
            Icon(imageVector = icona, contentDescription = null, tint = colore, modifier = Modifier.size(18.dp))
        }
        Text(
            text = testo,
            color = colore,
            fontSize = 14.sp,
            fontWeight = if (principale) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PulsantePreferito(preferito: Boolean, onClick: () -> Unit) {
    var infocato by remember { mutableStateOf(false) }
    Text(
        text = if (preferito) "★ Nei Preferiti" else "☆ Preferiti",
        color = when {
            infocato -> Palette.inchiostro
            preferito -> LocalAccento.current
            else -> Palette.testo
        },
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .onFocusChanged { infocato = it.isFocused }
            .zoomInFocus(infocato, RoundedCornerShape(10.dp), scalaMax = 1.06f, ombraMax = 14.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .background(if (infocato) Palette.testo else Palette.testo.copy(alpha = 0.14f))
            .padding(horizontal = 22.dp, vertical = 11.dp)
    )
}

private fun nomeStagione(stagione: Stagione): String = stagione.number?.let { "Stagione $it" } ?: "Altri episodi"

/** Il dropdown Stagioni mostra al massimo 6 voci (48.dp l'una + 8.dp di padding sopra e sotto);
 * oltre, si scorre. Non si adatta a quante ne entrano nello schermo. */
private val ALTEZZA_VOCE_DROPDOWN_STAGIONI = 48.dp
private val ALTEZZA_MAX_DROPDOWN_STAGIONI = ALTEZZA_VOCE_DROPDOWN_STAGIONI * 6 + 16.dp

@Composable
private fun SelettoreStagioni(
    stagioni: List<Stagione>,
    selezionata: Stagione?,
    visti: List<Visto>,
    onSeleziona: (Stagione) -> Unit,
    onSegnaStagioneNonVista: (Stagione) -> Unit
) {
    var espanso by remember { mutableStateOf(false) }
    var infocato by remember { mutableStateOf(false) }
    var menuAperto by remember { mutableStateOf(false) }
    val accento = LocalAccento.current
    val stagioneCorrente = selezionata ?: stagioni.firstOrNull()
    val haVistoStagione = stagioneCorrente != null &&
        stagioneCorrente.episodes.any { ep -> visti.any { it.chiaveIdentita == ep.chiaveIdentita } }

    // Aprendo il selettore da una Stagione avanzata (es. la decima di venti) il dropdown partiva
    // sempre scrollato in cima, lontano dalla voce gia' scelta: bisognava scorrere a mano per
    // ritrovarla. Qui si centra subito sulla voce corrente, invece di lasciare che il dropdown
    // apra semplicemente dall'inizio.
    val statoScrollDropdown = rememberScrollState()
    val densita = LocalDensity.current
    LaunchedEffect(espanso) {
        if (!espanso) return@LaunchedEffect
        val indice = stagioni.indexOfFirst { it.number == selezionata?.number }
        if (indice <= 0) return@LaunchedEffect
        withFrameNanos { } // il dropdown deve essersi gia' composto prima di poterlo scorrere.
        val altezzaVoce = with(densita) { ALTEZZA_VOCE_DROPDOWN_STAGIONI.toPx() }
        val altezzaVisibile = with(densita) { ALTEZZA_MAX_DROPDOWN_STAGIONI.toPx() }
        val destinazione = (indice * altezzaVoce - (altezzaVisibile - altezzaVoce) / 2)
            .toInt()
            .coerceAtLeast(0)
        statoScrollDropdown.scrollTo(destinazione)
    }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .onFocusChanged { infocato = it.isFocused }
                .pressabile(
                    onClick = { espanso = true },
                    onLongClick = { menuAperto = true }.takeIf { haVistoStagione }
                )
                .background(Color(0xFF17191F), RoundedCornerShape(8.dp))
                .border(2.dp, if (infocato || espanso) accento else Color.Transparent, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Text(
                text = selezionata?.let { nomeStagione(it) } ?: "Stagioni",
                color = Color(0xFFF2F2F0),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color(0xFF9AA0AA))
        }
        MaterialTheme(colorScheme = darkColorScheme(surface = Color(0xFF17191F), onSurface = Color(0xFFF2F2F0))) {
            DropdownMenu(
                expanded = espanso,
                onDismissRequest = { espanso = false },
                scrollState = statoScrollDropdown,
                modifier = Modifier.heightIn(max = ALTEZZA_MAX_DROPDOWN_STAGIONI)
            ) {
                stagioni.forEach { stagione ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = nomeStagione(stagione),
                                color = if (stagione.number == selezionata?.number) accento else Color(0xFFF2F2F0)
                            )
                        },
                        onClick = {
                            onSeleziona(stagione)
                            espanso = false
                        }
                    )
                }
            }
        }
    }
    if (menuAperto && stagioneCorrente != null) {
        MenuStagione(
            stagione = stagioneCorrente,
            titolo = nomeStagione(stagioneCorrente),
            onSegnaNonVista = {
                menuAperto = false
                onSegnaStagioneNonVista(stagioneCorrente)
            },
            onChiudi = { menuAperto = false }
        )
    }
}

@Composable
private fun CarouselEpisodi(
    episodi: List<Episodio>,
    posterSerie: String?,
    visti: List<Visto>,
    onEpisodioClick: (Episodio) -> Unit,
    onEpisodioRiproduciCon: (Episodio) -> Unit,
    onEpisodioSegnaNonVisto: (Episodio) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        // Spazio sopra/sotto e ai lati perche' la card in focus, ingrandita, non venga tagliata
        // (lo zoom cresce dal centro orizzontalmente): mancava il padding orizzontale, le card
        // agli estremi del carousel restavano tagliate.
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        items(episodi) { episodio ->
            val immagine = episodio.immagine ?: posterSerie
            CardEpisodio(
                episodio = episodio,
                immagine = immagine,
                percentuale = registro.percentuale(visti, episodio.chiaveIdentita),
                haVisto = visti.any { it.chiaveIdentita == episodio.chiaveIdentita },
                onClick = { onEpisodioClick(episodio) },
                onRiproduciCon = { onEpisodioRiproduciCon(episodio) },
                onSegnaNonVisto = { onEpisodioSegnaNonVisto(episodio) }
            )
        }
    }
}

@Composable
private fun CardEpisodio(
    episodio: Episodio,
    immagine: String?,
    percentuale: Int,
    haVisto: Boolean,
    onClick: () -> Unit,
    onRiproduciCon: () -> Unit,
    onSegnaNonVisto: () -> Unit
) {
    var infocato by remember { mutableStateOf(false) }
    var menuAperto by remember { mutableStateOf(false) }
    val forma = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .width(240.dp)
            .onFocusChanged { infocato = it.isFocused }
            .pressabile(onClick = onClick, onLongClick = { menuAperto = true }),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(135.dp)
                .zoomInFocus(infocato, forma, origine = TransformOrigin(0.5f, 1f))
                .clip(forma)
                .background(Color(0xFF1E2027))
                .border(
                    2.dp,
                    if (infocato) LocalAccento.current else Color.Transparent,
                    forma
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
    if (menuAperto) {
        MenuEpisodio(
            episodio = episodio,
            copertina = immagine,
            haVisto = haVisto,
            onRiproduciCon = {
                menuAperto = false
                onRiproduciCon()
            },
            onSegnaNonVisto = {
                menuAperto = false
                onSegnaNonVisto()
            },
            onChiudi = { menuAperto = false }
        )
    }
}

@Composable
private fun BarraProgresso(percentuale: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(4.dp)
            .clip(CircleShape)
            .background(Color(0xFF34373F))
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
    Pagina { _ ->
        Text(text = titolo, color = Color(0xFFF2F2F0), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Impossibile caricare i dettagli di questo contenuto.",
            color = Color(0xFF9AA0AA),
            fontSize = 16.sp
        )
    }
}
