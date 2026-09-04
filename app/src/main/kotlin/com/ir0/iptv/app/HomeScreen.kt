package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.domain.source.m3u.M3uEntry

@Composable
fun HomeScreen(canali: List<M3uEntry>) {
    MaterialTheme {
        Surface(color = Color(0xFF14161A)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF14161A))
                    .padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "1r0 IPTV",
                    color = Color(0xFFF2F2F0),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Canali",
                    color = Color(0xFFF2F2F0),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (canali.isEmpty()) {
                    Text(
                        text = "Nessun canale trovato nelle Sorgenti configurate.",
                        color = Color(0xFF9AA0AA),
                        fontSize = 16.sp
                    )
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(canali) { canale -> ChannelCard(canale) }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    MaterialTheme {
        Surface(color = Color(0xFF14161A)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF14161A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Caricamento contenuti…",
                    color = Color(0xFF9AA0AA),
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ChannelCard(canale: M3uEntry) {
    Column(
        modifier = Modifier.width(200.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(110.dp)
                .background(Color(0xFF262B33), RoundedCornerShape(8.dp))
        )
        Text(
            text = canale.title,
            color = Color(0xFFF2F2F0),
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
