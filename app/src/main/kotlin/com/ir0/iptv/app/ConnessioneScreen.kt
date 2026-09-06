package com.ir0.iptv.app

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.theme.LocalAccento
import com.ir0.iptv.app.webpanel.QrCodeGenerator

private const val QR_CODE_SIZE_PX = 768
private val QR_SIZE_MAX = 360.dp
private val COLONNA_SPAZIO = 56.dp

/**
 * Il punto sempre raggiungibile per aprire il Pannello Web: inquadri il QR col telefono e gestisci
 * Sorgenti, credenziali Xtream e impostazioni da una tastiera vera. Serve soprattutto quando un
 * abbonamento Xtream scade e il catalogo resta vuoto: da qui si arriva al pannello anche senza un
 * solo contenuto scaricabile, per aggiornare host, username e password.
 */
@Composable
fun ConnessioneScreen(indirizzo: String?) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        val larghezzaTotale = maxWidth
        val qrSize = minOf(QR_SIZE_MAX, maxHeight, larghezzaTotale / 2)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(COLONNA_SPAZIO)
        ) {
            Column(
                modifier = Modifier.width((larghezzaTotale - qrSize - COLONNA_SPAZIO).coerceAtLeast(280.dp)),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "PANNELLO WEB",
                    color = LocalAccento.current,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Gestisci Sorgenti e credenziali",
                    color = Color(0xFFF2F2F0),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Inquadra il codice con un telefono o computer sulla stessa rete Wi-Fi " +
                        "per aprire il Pannello Web. Da lì cambi host, username e password di un " +
                        "account Xtream Codes, aggiungi playlist M3U e sistemi le impostazioni.",
                    color = Color(0xFF9AA0AA),
                    fontSize = 16.sp
                )
                Text(
                    text = "Se un abbonamento è scaduto e il catalogo risulta vuoto, questa resta " +
                        "l'unica strada per rimettere a posto le credenziali: nessun contenuto da " +
                        "scaricare serve per arrivarci.",
                    color = Color(0xFF6D7380),
                    fontSize = 14.sp
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
                        text = indirizzo ?: "Indirizzo non disponibile: verifica la connessione Wi-Fi",
                        color = Color(0xFFF2F2F0),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            if (indirizzo != null) {
                val qrBitmap = remember(indirizzo) {
                    QrCodeGenerator.generate(indirizzo, QR_CODE_SIZE_PX)
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

@Preview(device = "id:tv_1080p")
@Composable
private fun ConnessioneScreenPreview() {
    ConnessioneScreen(indirizzo = "http://192.168.1.42:8080")
}
