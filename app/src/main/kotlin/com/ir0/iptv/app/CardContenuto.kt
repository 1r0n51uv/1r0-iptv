package com.ir0.iptv.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ir0.iptv.domain.catalog.ContentCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardContenuto(
    card: ContentCard,
    percentuale: Int = 0,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    var infocata by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.width(200.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(112.dp)
                .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
                .onFocusChanged { infocata = it.isFocused }
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF262B33))
                .border(
                    2.dp,
                    if (infocata) Color(0xFFFFB454) else Color.Transparent,
                    RoundedCornerShape(8.dp)
                )
        ) {
            val imageUrl = card.imageUrl
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = card.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            if (percentuale > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3A3F48))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentuale / 100f)
                            .fillMaxHeight()
                            .background(Color(0xFFFFB454))
                    )
                }
            }
        }
        Text(
            text = card.title,
            color = if (infocata) Color(0xFFFFB454) else Color(0xFFF2F2F0),
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
