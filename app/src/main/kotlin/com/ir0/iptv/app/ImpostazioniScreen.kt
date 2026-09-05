package com.ir0.iptv.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.webpanel.QrCodeGenerator
import com.ir0.iptv.domain.source.Sorgente

private const val QR_CODE_SIZE_PX = 512

@Composable
fun ImpostazioniScreen(
    sorgenti: List<Sorgente>,
    webPanelAddress: String?,
    activeIndex: Int,
    onSidebarClick: (Int) -> Unit
) {
    AppScaffold(activeIndex = activeIndex, onSidebarClick = onSidebarClick) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 36.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            item {
                Text(text = "Impostazioni", color = Color(0xFFF2F2F0), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SORGENTI CONFIGURATE",
                        color = Color(0xFFFFB454),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    if (sorgenti.isEmpty()) {
                        Text(text = "Nessuna Sorgente configurata.", color = Color(0xFF9AA0AA), fontSize = 15.sp)
                    } else {
                        sorgenti.forEach { sorgente ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1F232A), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Text(text = sorgente.nome, color = Color(0xFFF2F2F0), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                val dettagli = when (sorgente) {
                                    is Sorgente.M3u -> "M3U · ${sorgente.url}"
                                    is Sorgente.Xtream -> "Xtream · ${sorgente.connection.host}:${sorgente.connection.port}"
                                }
                                Text(text = dettagli, color = Color(0xFF9AA0AA), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "AGGIUNGI UNA NUOVA SORGENTE",
                        color = Color(0xFFFFB454),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Apri il Pannello Web da un telefono o computer collegato alla stessa rete Wi-Fi.",
                        color = Color(0xFF9AA0AA),
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(
                            modifier = Modifier
                                .background(Color(0xFF1F232A), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF262B33), RoundedCornerShape(10.dp))
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "INDIRIZZO PANNELLO WEB", color = Color(0xFF9AA0AA), fontSize = 12.sp, letterSpacing = 0.5.sp)
                            Text(
                                text = webPanelAddress ?: "Indirizzo non disponibile: verifica la connessione Wi-Fi",
                                color = Color(0xFFF2F2F0),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (webPanelAddress != null) {
                            val qrBitmap = remember(webPanelAddress) { QrCodeGenerator.generate(webPanelAddress, QR_CODE_SIZE_PX) }
                            if (qrBitmap != null) {
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .background(Color(0xFFF2F2F0), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = "QR code per aprire il Pannello Web",
                                        modifier = Modifier.size(136.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
