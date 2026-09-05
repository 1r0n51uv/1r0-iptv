package com.ir0.iptv.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private enum class SidebarGlyph { HOME, CANALI, FILM, SERIE, FAVORITE, SETTINGS }

/** Index of the pinned bottom icon; 0-4 are the evenly-spaced top icons. Kept in sync with
 * [screenForSidebarIndex] / [sidebarIndexForScreen] in Screen.kt. */
const val SIDEBAR_SETTINGS_INDEX = 5

@Composable
fun Sidebar(
    activeIndex: Int,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    activeFocusRequester: FocusRequester? = null
) {
    Column(
        modifier = modifier
            .width(88.dp)
            .fillMaxHeight()
            .background(Color(0xFF191C22)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(Modifier.padding(top = 24.dp))
        listOf(SidebarGlyph.HOME, SidebarGlyph.CANALI, SidebarGlyph.FILM, SidebarGlyph.SERIE, SidebarGlyph.FAVORITE)
            .forEachIndexed { index, glyph ->
                SidebarButton(
                    glyph = glyph,
                    active = index == activeIndex,
                    onClick = { onItemClick(index) },
                    focusRequester = if (index == activeIndex) activeFocusRequester else null
                )
            }
        Box(Modifier.weight(1f))
        SidebarButton(
            glyph = SidebarGlyph.SETTINGS,
            active = activeIndex == SIDEBAR_SETTINGS_INDEX,
            onClick = { onItemClick(SIDEBAR_SETTINGS_INDEX) },
            focusRequester = if (activeIndex == SIDEBAR_SETTINGS_INDEX) activeFocusRequester else null
        )
        Box(Modifier.padding(bottom = 24.dp))
    }
}

@Composable
private fun SidebarButton(glyph: SidebarGlyph, active: Boolean, onClick: () -> Unit, focusRequester: FocusRequester? = null) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) Color(0xFFFFB454) else Color.Transparent)
            .then(
                if (isFocused && !active) {
                    Modifier.border(2.dp, Color(0xFFFFB454), RoundedCornerShape(10.dp))
                } else {
                    Modifier
                }
            )
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        SidebarGlyphIcon(glyph = glyph, color = if (active) Color(0xFF14161A) else Color(0xFF9AA0AA))
    }
}

@Composable
private fun SidebarGlyphIcon(glyph: SidebarGlyph, color: Color) {
    Canvas(modifier = Modifier.size(19.dp)) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        val w = size.width
        val h = size.height
        when (glyph) {
            SidebarGlyph.HOME -> {
                val roof = Path().apply {
                    moveTo(w * 0.05f, h * 0.5f)
                    lineTo(w * 0.5f, h * 0.05f)
                    lineTo(w * 0.95f, h * 0.5f)
                }
                drawPath(roof, color = color, style = stroke)
                drawRect(
                    color = color,
                    topLeft = Offset(w * 0.2f, h * 0.48f),
                    size = Size(w * 0.6f, h * 0.47f),
                    style = stroke
                )
            }

            SidebarGlyph.CANALI -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.05f, h * 0.15f),
                    size = Size(w * 0.9f, h * 0.65f),
                    cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
                    style = stroke
                )
                drawLine(color, Offset(w * 0.3f, h * 0.95f), Offset(w * 0.7f, h * 0.95f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.5f, h * 0.8f), Offset(w * 0.5f, h * 0.95f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            SidebarGlyph.FILM -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.05f, h * 0.08f),
                    size = Size(w * 0.9f, h * 0.22f),
                    cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
                    style = stroke
                )
                val stripeXs = listOf(0.24f, 0.48f, 0.72f)
                stripeXs.forEach { fraction ->
                    drawLine(
                        color,
                        Offset(w * fraction, h * 0.08f),
                        Offset(w * (fraction - 0.08f), h * 0.30f),
                        strokeWidth = stroke.width
                    )
                }
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.05f, h * 0.34f),
                    size = Size(w * 0.9f, h * 0.58f),
                    cornerRadius = CornerRadius(w * 0.06f, w * 0.06f),
                    style = stroke
                )
            }

            SidebarGlyph.SERIE -> {
                listOf(0.28f, 0.5f, 0.72f).forEach { cy ->
                    val chevron = Path().apply {
                        moveTo(w * 0.5f, h * (cy - 0.12f))
                        lineTo(w * 0.92f, h * cy)
                        lineTo(w * 0.5f, h * (cy + 0.12f))
                        lineTo(w * 0.08f, h * cy)
                        close()
                    }
                    drawPath(chevron, color = color, style = stroke)
                }
            }

            SidebarGlyph.FAVORITE -> {
                val r = w * 0.22f
                drawCircle(color = color, radius = r, center = Offset(w * 0.32f, h * 0.36f), style = stroke)
                drawCircle(color = color, radius = r, center = Offset(w * 0.68f, h * 0.36f), style = stroke)
                val bottom = Path().apply {
                    moveTo(w * 0.12f, h * 0.42f)
                    lineTo(w * 0.5f, h * 0.92f)
                    lineTo(w * 0.88f, h * 0.42f)
                }
                drawPath(bottom, color = color, style = stroke)
            }

            SidebarGlyph.SETTINGS -> {
                drawCircle(color = color, radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawCircle(color = color, radius = w * 0.4f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
            }
        }
    }
}
