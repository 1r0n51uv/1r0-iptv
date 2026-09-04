package com.ir0.iptv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IptvHelloWorldScreen()
        }
    }
}

@Composable
private fun IptvHelloWorldScreen() {
    MaterialTheme {
        Surface(color = Color(0xFF14161A)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF14161A)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Hello, 1r0 IPTV!", color = Color(0xFFF2F2F0))
            }
        }
    }
}

@Preview(device = "id:tv_1080p")
@Composable
private fun IptvHelloWorldScreenPreview() {
    IptvHelloWorldScreen()
}
