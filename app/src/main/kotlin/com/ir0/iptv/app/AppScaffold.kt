package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ir0.iptv.app.util.DownsampleBlurTransformation

/** Shared shell for every top-level sidebar destination: left [Sidebar], an optional blurred
 * backdrop image, and a top bar above the page's own [content].
 *
 * Every screen switch (`when (backStack.last())` in MainActivity) unmounts the previous
 * composable subtree entirely, destroying whatever had D-pad focus. Falling back to focusing
 * the active sidebar icon here guarantees the remote always has *something* focused on
 * arrival; a page whose own content wants first focus (Home's hero, a catalog grid's first
 * card) requests it in a LaunchedEffect that runs after this one and wins. */
@Composable
fun AppScaffold(
    activeIndex: Int,
    onSidebarClick: (Int) -> Unit,
    backgroundImageUrl: String? = null,
    content: @Composable () -> Unit
) {
    val sidebarFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { sidebarFocusRequester.requestFocus() }

    MaterialTheme {
        Surface(color = Color(0xFF14161A)) {
            Box(Modifier.fillMaxSize()) {
                if (backgroundImageUrl != null) {
                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(backgroundImageUrl)
                            .transformations(DownsampleBlurTransformation())
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.45f
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xCC14161A))
                    )
                }

                Row(Modifier.fillMaxSize()) {
                    Sidebar(
                        activeIndex = activeIndex,
                        onItemClick = onSidebarClick,
                        activeFocusRequester = sidebarFocusRequester
                    )
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        TopBar()
                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 36.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "1r0 IPTV", color = Color(0xFFF2F2F0), fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
