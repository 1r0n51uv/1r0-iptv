package com.ir0.iptv.app

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.app.content.ContentFetcher
import com.ir0.iptv.app.playback.RichiestaRiproduzione
import com.ir0.iptv.app.playback.VistoRepository
import com.ir0.iptv.app.webpanel.QrCodeGenerator
import com.ir0.iptv.app.webpanel.SorgenteRepository
import com.ir0.iptv.app.webpanel.WebPanelServer
import com.ir0.iptv.domain.classification.Episodio
import com.ir0.iptv.domain.playback.TipoVisto
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sorgenteRepository = SorgenteRepository(applicationContext)
        val vistoRepository = VistoRepository(applicationContext)
        setContent {
            var sorgenti by remember { mutableStateOf(sorgenteRepository.elenco()) }
            LaunchedEffect(Unit) {
                while (sorgenti.isEmpty()) {
                    delay(SORGENTI_POLL_INTERVAL_MS)
                    sorgenti = sorgenteRepository.elenco()
                }
            }
            val webPanelAddress = remember { localWebPanelAddress() }

            if (sorgenti.isEmpty()) {
                OnboardingScreen(webPanelAddress = webPanelAddress)
            } else {
                ContentScreen(sorgenti, webPanelAddress, vistoRepository)
            }
        }
    }
}

@Composable
private fun ContentScreen(
    sorgenti: List<Sorgente>,
    webPanelAddress: String?,
    vistoRepository: VistoRepository
) {
    var catalogo by remember(sorgenti) { mutableStateOf<ContentCatalog?>(null) }
    LaunchedEffect(sorgenti) {
        catalogo = ContentFetcher().catalogo(sorgenti)
    }
    val catalogoCorrente = catalogo
    if (catalogoCorrente == null) {
        LoadingScreen()
        return
    }

    var backStack by remember { mutableStateOf(listOf<Screen>(Screen.Home)) }
    BackHandler(enabled = backStack.size > 1) {
        backStack = backStack.dropLast(1)
    }

    val onSidebarClick: (Int) -> Unit = { index -> backStack = listOf(screenForSidebarIndex(index)) }

    when (val schermata = backStack.last()) {
        is Screen.Home -> HomeScreen(
            catalogo = catalogoCorrente,
            activeIndex = sidebarIndexForScreen(schermata),
            onSidebarClick = onSidebarClick,
            onCanaleClick = { backStack = backStack + Screen.Player(it.toRichiesta()) },
            onFilmClick = { backStack = backStack + Screen.Player(it.toRichiesta()) },
            onSerieClick = { backStack = backStack + Screen.SeriesDetail(it) }
        )

        is Screen.Canali -> CanaliScreen(
            catalogo = catalogoCorrente,
            activeIndex = sidebarIndexForScreen(schermata),
            onSidebarClick = onSidebarClick,
            onCanaleClick = { backStack = backStack + Screen.Player(it.toRichiesta()) }
        )

        is Screen.Film -> FilmScreen(
            catalogo = catalogoCorrente,
            activeIndex = sidebarIndexForScreen(schermata),
            onSidebarClick = onSidebarClick,
            onFilmClick = { backStack = backStack + Screen.Player(it.toRichiesta()) }
        )

        is Screen.Serie -> SerieScreen(
            catalogo = catalogoCorrente,
            activeIndex = sidebarIndexForScreen(schermata),
            onSidebarClick = onSidebarClick,
            onSerieClick = { backStack = backStack + Screen.SeriesDetail(it) }
        )

        is Screen.Preferiti -> PreferitiScreen(
            activeIndex = sidebarIndexForScreen(schermata),
            onSidebarClick = onSidebarClick
        )

        is Screen.Impostazioni -> ImpostazioniScreen(
            sorgenti = sorgenti,
            webPanelAddress = webPanelAddress,
            activeIndex = sidebarIndexForScreen(schermata),
            onSidebarClick = onSidebarClick
        )

        is Screen.SeriesDetail -> SeriesDetailScreen(
            card = schermata.card,
            onEpisodioClick = { episodio ->
                backStack = backStack + Screen.Player(episodio.toRichiesta(schermata.card))
            }
        )

        is Screen.Player -> {
            val richiesta = schermata.richiesta
            PlayerScreen(
                richiesta = richiesta,
                posizioneIniziale = remember(richiesta) {
                    vistoRepository.posizioneDiRipresa(richiesta.chiaveIdentita) ?: 0L
                },
                onProgresso = { posizioneMs, durataMs ->
                    vistoRepository.registraProgresso(richiesta, posizioneMs, durataMs)
                }
            )
        }
    }
}

private fun ContentCard.Canale.toRichiesta() =
    RichiestaRiproduzione(titolo = title, streamUrl = streamUrl, posterUrl = imageUrl)

private fun ContentCard.Film.toRichiesta() = RichiestaRiproduzione(
    titolo = title,
    streamUrl = streamUrl,
    tipo = TipoVisto.FILM,
    posterUrl = imageUrl
)

private fun Episodio.toRichiesta(serie: ContentCard.SerieCard) = RichiestaRiproduzione(
    titolo = title,
    streamUrl = url,
    tipo = TipoVisto.EPISODIO,
    serie = serie.title,
    posterUrl = serie.imageUrl
)

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
                            color = Color(0xFFFFB454),
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
