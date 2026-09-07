package com.ir0.iptv.app

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.theme.LocalAccento

/** Scheletro delle righe invece di una scritta al centro: il catalogo di una Sorgente grande
 * ci mette parecchio, e uno schermo quasi vuoto sembra un'app bloccata. Un'onda di luce scorre
 * di continuo sui blocchi e un indicatore gira accanto al messaggio, cosi' si vede sempre che
 * l'app sta lavorando anche prima che arrivi il primo contenuto. */
@Composable
fun LoadingScreen(messaggio: String = "Caricamento contenuti…") {
    val transizione = rememberInfiniteTransition(label = "loadingShimmer")
    val avanzamento by transizione.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "loadingShimmerProgress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
            .shimmer(avanzamento)
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = LocalAccento.current,
                strokeWidth = 2.dp
            )
            Text(text = messaggio, color = Color(0xFF9AA0AA), fontSize = 16.sp)
        }
        repeat(3) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1F232A))
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(6) {
                        Box(
                            modifier = Modifier
                                .width(200.dp)
                                .height(112.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1F232A))
                        )
                    }
                }
            }
        }
    }
}

/** Passa una banda chiara diagonale sopra il contenuto gia' disegnato, spinta da [avanzamento]
 * (0..1). Non tocca il layout: e' solo un velo che si muove. */
private fun Modifier.shimmer(avanzamento: Float): Modifier = drawWithContent {
    drawContent()
    val larghezzaBanda = size.width * 0.35f
    val x = -larghezzaBanda + (size.width + larghezzaBanda * 2f) * avanzamento
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(Color.Transparent, Color(0x14FFFFFF), Color.Transparent),
            start = Offset(x, 0f),
            end = Offset(x + larghezzaBanda, size.height)
        ),
        blendMode = BlendMode.Plus
    )
}
