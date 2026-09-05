package com.ir0.iptv.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.content.ContentFetcher
import com.ir0.iptv.app.customization.PersonalizzazioneRepository
import com.ir0.iptv.app.dashboard.NuoviEpisodi
import com.ir0.iptv.app.dashboard.NuoviEpisodiRepository
import com.ir0.iptv.app.dashboard.SuggerimentiAi
import com.ir0.iptv.app.navigation.Destinazione
import com.ir0.iptv.app.navigation.Sidebar
import com.ir0.iptv.app.playback.RichiestaRiproduzione
import com.ir0.iptv.app.playback.RiproduciCon
import com.ir0.iptv.app.playback.VistoRepository
import com.ir0.iptv.app.session.StatoSessioneRepository
import com.ir0.iptv.app.settings.Impostazioni
import com.ir0.iptv.app.settings.ImpostazioniRepository
import com.ir0.iptv.app.sport.PartitaConCanale
import com.ir0.iptv.app.sport.SportInEvidenza
import com.ir0.iptv.app.theme.Accento
import com.ir0.iptv.app.theme.LocalAccento
import com.ir0.iptv.app.webpanel.PonteTv
import com.ir0.iptv.app.webpanel.QrCodeGenerator
import com.ir0.iptv.app.webpanel.SorgenteRepository
import com.ir0.iptv.app.webpanel.WebPanelServer
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.catalog.ElencoPreferiti
import com.ir0.iptv.domain.dashboard.CostruttoreDashboard
import com.ir0.iptv.domain.dashboard.MemoriaFocus
import com.ir0.iptv.domain.dashboard.RigaDashboard
import com.ir0.iptv.domain.dashboard.TipoRiga
import com.ir0.iptv.domain.source.Sorgente
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Collections
import kotlinx.coroutines.delay

private const val SORGENTI_POLL_INTERVAL_MS = 2000L

private const val QR_CODE_SIZE_PX = 768
private val MIN_TEXT_COLUMN_WIDTH = 280.dp
private val ROW_SPACING = 64.dp

private val costruttoreDashboard = CostruttoreDashboard()
private val memoriaFocus = MemoriaFocus()
private val elencoPreferiti = ElencoPreferiti()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sorgenteRepository = SorgenteRepository(applicationContext)
        val vistoRepository = VistoRepository(applicationContext)
        val personalizzazioneRepository = PersonalizzazioneRepository(applicationContext)
        val statoSessioneRepository = StatoSessioneRepository(applicationContext)
        val impostazioniRepository = ImpostazioniRepository(applicationContext)
        val nuoviEpisodi = NuoviEpisodi(NuoviEpisodiRepository(applicationContext))
        val suggerimentiAi = SuggerimentiAi()
        val sportInEvidenza = SportInEvidenza()
        setContent {
            var sorgenti by remember { mutableStateOf(sorgenteRepository.elenco()) }
            LaunchedEffect(Unit) {
                while (sorgenti.isEmpty()) {
                    delay(SORGENTI_POLL_INTERVAL_MS)
                    sorgenti = sorgenteRepository.elenco()
                }
            }

            if (sorgenti.isEmpty()) {
                OnboardingScreen(webPanelAddress = remember { localWebPanelAddress() })
            } else {
                ContentScreen(
                    sorgenti = sorgenti,
                    vistoRepository = vistoRepository,
                    personalizzazioneRepository = personalizzazioneRepository,
                    statoSessioneRepository = statoSessioneRepository,
                    impostazioniRepository = impostazioniRepository,
                    nuoviEpisodi = nuoviEpisodi,
                    suggerimentiAi = suggerimentiAi,
                    sportInEvidenza = sportInEvidenza
                )
            }
        }
    }
}

@Composable
private fun ContentScreen(
    sorgenti: List<Sorgente>,
    vistoRepository: VistoRepository,
    personalizzazioneRepository: PersonalizzazioneRepository,
    statoSessioneRepository: StatoSessioneRepository,
    impostazioniRepository: ImpostazioniRepository,
    nuoviEpisodi: NuoviEpisodi,
    suggerimentiAi: SuggerimentiAi,
    sportInEvidenza: SportInEvidenza
) {
    val context = LocalContext.current
    var catalogo by remember(sorgenti) { mutableStateOf<ContentCatalog?>(null) }
    var richiesteDiAggiornamento by remember(sorgenti) { mutableStateOf(0) }
    var inAggiornamento by remember(sorgenti) { mutableStateOf(false) }
    LaunchedEffect(sorgenti, richiesteDiAggiornamento) {
        inAggiornamento = true
        // Il catalogo vecchio resta a schermo durante un aggiornamento: azzerarlo
        // riporterebbe allo scheletro di caricamento ad ogni refresh.
        val aggiornato = ContentFetcher().catalogo(sorgenti)
        catalogo = aggiornato
        inAggiornamento = false
    }
    val catalogoCorrente = catalogo
    if (catalogoCorrente == null) {
        LoadingScreen()
        return
    }

    var impostazioni by remember { mutableStateOf(impostazioniRepository.leggi()) }
    var destinazione by remember { mutableStateOf(Destinazione.DASHBOARD) }
    var sovrapposte by remember { mutableStateOf(listOf<Screen>()) }
    BackHandler(enabled = sovrapposte.isNotEmpty()) {
        sovrapposte = sovrapposte.dropLast(1)
    }

    // Rileggere ad ogni cambio di schermata tiene aggiornate le barre di avanzamento
    // e la riga Continua a guardare dopo una riproduzione.
    val visti = remember(sovrapposte, destinazione, catalogoCorrente) { vistoRepository.elenco() }
    val personalizzazioni = remember(sovrapposte, destinazione, catalogoCorrente) {
        personalizzazioneRepository.elenco()
    }
    var rigaNuoviEpisodi by remember(catalogoCorrente) {
        mutableStateOf(RigaDashboard(TipoRiga.NUOVI_EPISODI, emptyList()))
    }
    var rigaSuggeriti by remember(catalogoCorrente) {
        mutableStateOf(RigaDashboard(TipoRiga.SUGGERITI, emptyList()))
    }
    var partiteInEvidenza by remember(catalogoCorrente) { mutableStateOf(emptyList<PartitaConCanale>()) }
    LaunchedEffect(catalogoCorrente) {
        rigaNuoviEpisodi = nuoviEpisodi.riga(catalogoCorrente, visti, personalizzazioni)
            ?: RigaDashboard(TipoRiga.NUOVI_EPISODI, emptyList())
        rigaSuggeriti = suggerimentiAi.riga(
            chiaveApi = impostazioni.chiaveApiAi,
            catalogo = catalogoCorrente,
            visti = visti,
            personalizzazioni = personalizzazioni
        ) ?: RigaDashboard(TipoRiga.SUGGERITI, emptyList())
        partiteInEvidenza = sportInEvidenza.partite(
            attivo = impostazioni.sportInDashboard,
            chiaveApi = impostazioni.chiaveApiSport,
            canali = catalogoCorrente.canali,
            limite = 20
        )
    }

    val righe = remember(catalogoCorrente, visti, personalizzazioni, rigaNuoviEpisodi, rigaSuggeriti) {
        costruttoreDashboard.costruisci(
            catalogo = catalogoCorrente,
            visti = visti,
            personalizzazioni = personalizzazioni,
            righeExtra = listOf(rigaNuoviEpisodi, rigaSuggeriti)
        )
    }

    var chiaveDaFocalizzare by remember(catalogoCorrente) {
        mutableStateOf(
            memoriaFocus.focusIniziale(
                righe = righe,
                ultimaChiave = statoSessioneRepository.ultimaChiave(),
                contenutoDiDefault = impostazioniRepository.leggi().contenutoDiDefault
            )
        )
    }

    fun apri(card: ContentCard) {
        chiaveDaFocalizzare = card.chiaveIdentita
        statoSessioneRepository.salvaUltimaChiave(card.chiaveIdentita)
        sovrapposte = sovrapposte + when (card) {
            is ContentCard.Canale -> Screen.Player(card.toRichiesta(), 0L)
            else -> Screen.Detail(card)
        }
    }

    LaunchedEffect(catalogoCorrente) { PonteTv.pubblicaCatalogo(catalogoCorrente) }
    val richiestoDalWeb by PonteTv.daAprire.collectAsState()
    LaunchedEffect(richiestoDalWeb) {
        richiestoDalWeb?.let {
            apri(it)
            PonteTv.aperturaConsumata()
        }
    }

    val sopra = sovrapposte.lastOrNull()
    if (sopra is Screen.Player) {
        PlayerScreen(
            richiesta = sopra.richiesta,
            posizioneIniziale = sopra.posizioneIniziale,
            onProgresso = { posizioneMs, durataMs ->
                vistoRepository.registraProgresso(sopra.richiesta, posizioneMs, durataMs)
            }
        )
        return
    }

    CompositionLocalProvider(LocalAccento provides Accento.daNome(impostazioni.accento).colore) {
        Row(modifier = Modifier.fillMaxSize().background(Color(0xFF14161A))) {
            Sidebar(
                selezionata = destinazione,
                onSeleziona = {
                    destinazione = it
                    sovrapposte = emptyList()
                },
                inAggiornamento = inAggiornamento,
                onAggiorna = { richiesteDiAggiornamento++ }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when (sopra) {
                    is Screen.Detail -> {
                        var preferito by remember(sopra.card) {
                            mutableStateOf(personalizzazioneRepository.preferito(sopra.card))
                        }
                        DetailScreen(
                            card = sopra.card,
                            visti = visti,
                            preferito = preferito,
                            onCambiaPreferito = { preferito = personalizzazioneRepository.cambiaPreferito(sopra.card) },
                            onRiproduci = { richiesta, posizione ->
                                sovrapposte = sovrapposte + Screen.Player(richiesta, posizione)
                            },
                            onRiproduciCon = { richiesta -> RiproduciCon.avvia(context, richiesta) }
                        )
                    }

                    else -> when (destinazione) {
                        Destinazione.DASHBOARD -> DashboardScreen(
                            righe = righe,
                            visti = visti,
                            chiaveDaFocalizzare = chiaveDaFocalizzare,
                            catalogoVuoto = catalogoCorrente.isEmpty,
                            sport = partiteInEvidenza.take(2),
                                onContenutoClick = { apri(it) },
                            onContenutoLongClick = { riproduciConDaCard(context, it) }
                        )

                        Destinazione.CANALI -> CanaliScreen(
                            catalogo = catalogoCorrente,
                            visti = visti,
                            onContenutoClick = { apri(it) },
                            onContenutoLongClick = { riproduciConDaCard(context, it) }
                        )

                        Destinazione.FILM -> FilmScreen(
                            catalogo = catalogoCorrente,
                            visti = visti,
                            onContenutoClick = { apri(it) },
                            onContenutoLongClick = { riproduciConDaCard(context, it) }
                        )

                        Destinazione.SERIE -> SerieScreen(
                            catalogo = catalogoCorrente,
                            visti = visti,
                            onContenutoClick = { apri(it) },
                            onContenutoLongClick = { riproduciConDaCard(context, it) }
                        )

                        Destinazione.GUIDA -> GuidaTvScreen(
                            catalogo = catalogoCorrente,
                            onCanaleClick = { apri(it) }
                        )

                        Destinazione.SPORT -> SportScreen(
                            catalogo = catalogoCorrente,
                            partite = partiteInEvidenza,
                            visti = visti,
                            onContenutoClick = { apri(it) },
                            onContenutoLongClick = { riproduciConDaCard(context, it) }
                        )

                        Destinazione.CERCA -> SearchScreen(
                            catalogo = catalogoCorrente,
                            visti = visti,
                            onContenutoClick = { apri(it) },
                            onContenutoLongClick = { riproduciConDaCard(context, it) }
                        )

                        Destinazione.PREFERITI -> FavoritesScreen(
                            preferiti = elencoPreferiti.preferiti(catalogoCorrente, personalizzazioni),
                            visti = visti,
                            onContenutoClick = { apri(it) },
                            onContenutoLongClick = { riproduciConDaCard(context, it) }
                        )

                        Destinazione.IMPOSTAZIONI -> ImpostazioniScreen(
                            impostazioni = impostazioni,
                            indirizzoPannelloWeb = remember { localWebPanelAddress() },
                            onCambia = { aggiornate ->
                                impostazioniRepository.salva(aggiornate)
                                impostazioni = aggiornate
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Un Canale parte subito; per Film e Serie il player esterno si sceglie dal Dettaglio. */
private fun riproduciConDaCard(context: Context, card: ContentCard) {
    if (card is ContentCard.Canale) RiproduciCon.avvia(context, card.toRichiesta())
}

private fun ContentCard.Canale.toRichiesta() =
    RichiestaRiproduzione(titolo = title, streamUrl = streamUrl, posterUrl = imageUrl)

private fun localWebPanelAddress(): String? {
    val interfaces = try {
        NetworkInterface.getNetworkInterfaces()
    } catch (e: SocketException) {
        null
    } ?: return null

    return Collections.list(interfaces)
        .asSequence()
        .mapNotNull { it.inetAddresses }
        .flatMap { Collections.list(it).asSequence() }
        .firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
        ?.hostAddress
        ?.let { "http://$it:${WebPanelServer.PORT}" }
}

@Composable
private fun OnboardingScreen(webPanelAddress: String?) {
    MaterialTheme {
        Surface(color = Color(0xFF14161A)) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF14161A))
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                val textColumnWidth = if (webPanelAddress != null) {
                    (maxWidth - maxHeight - ROW_SPACING).coerceAtLeast(MIN_TEXT_COLUMN_WIDTH)
                } else {
                    maxWidth
                }
                val qrSize = (maxWidth - textColumnWidth - ROW_SPACING).coerceIn(0.dp, maxHeight)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ROW_SPACING)
                ) {
                    Column(
                        modifier = Modifier.width(textColumnWidth),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Text(
                            text = "PRIMO AVVIO",
                            color = LocalAccento.current,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Nessuna Sorgente configurata",
                            color = Color(0xFFF2F2F0),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Apri il Pannello Web da un telefono o computer collegato alla stessa rete Wi-Fi per aggiungere la tua prima playlist M3U o il tuo account Xtream Codes.",
                            color = Color(0xFF9AA0AA),
                            fontSize = 16.sp
                        )
                        Column(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .background(Color(0xFF1F232A), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF262B33), RoundedCornerShape(10.dp))
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "INDIRIZZO PANNELLO WEB",
                                color = Color(0xFF9AA0AA),
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = webPanelAddress ?: "Indirizzo non disponibile: verifica la connessione Wi-Fi",
                                color = Color(0xFFF2F2F0),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "In attesa di connessione dal Pannello Web…",
                            color = Color(0xFF6D7380),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (webPanelAddress != null) {
                        val qrBitmap = remember(webPanelAddress) {
                            QrCodeGenerator.generate(webPanelAddress, QR_CODE_SIZE_PX)
                        }
                        if (qrBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .size(qrSize)
                                    .background(Color(0xFFF2F2F0), RoundedCornerShape(16.dp))
                                    .padding(18.dp)
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "QR code per aprire il Pannello Web",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(device = "id:tv_1080p")
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(webPanelAddress = "http://192.168.1.42:8080")
}
