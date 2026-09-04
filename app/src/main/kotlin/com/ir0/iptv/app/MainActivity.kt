package com.ir0.iptv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.webpanel.WebPanelServer
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Collections

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OnboardingScreen(webPanelAddress = remember { localWebPanelAddress() })
        }
    }
}

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF14161A))
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 560.dp),
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
            }
        }
    }
}

@Preview(device = "id:tv_1080p")
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(webPanelAddress = "http://192.168.1.42:8080")
}
