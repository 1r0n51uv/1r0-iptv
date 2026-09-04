package com.ir0.iptv.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private enum class SidebarGlyph { HOME, TV, SEARCH, GUIDE, FAVORITE, SETTINGS }

@Composable
fun Sidebar(activeIndex: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(88.dp)
            .fillMaxHeight()
            .background(Color(0xFF191C22)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(Modifier.padding(top = 24.dp))
        listOf(SidebarGlyph.HOME, SidebarGlyph.TV, SidebarGlyph.SEARCH, SidebarGlyph.GUIDE, SidebarGlyph.FAVORITE)
            .forEachIndexed { index, glyph -> SidebarButton(glyph = glyph, active = index == activeIndex) }
        Box(Modifier.weight(1f))
        SidebarButton(glyph = SidebarGlyph.SETTINGS, active = false)
        Box(Modifier.padding(bottom = 24.dp))
    }
}

@Composable
private fun SidebarButton(glyph: SidebarGlyph, active: Boolean) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) Color(0xFFFFB454) else Color.Transparent),
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

            SidebarGlyph.TV -> {
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

            SidebarGlyph.SEARCH -> {
                drawCircle(color = color, radius = w * 0.32f, center = Offset(w * 0.42f, h * 0.42f), style = stroke)
                drawLine(color, Offset(w * 0.68f, h * 0.68f), Offset(w * 0.92f, h * 0.92f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            SidebarGlyph.GUIDE -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.08f, h * 0.12f),
                    size = Size(w * 0.84f, h * 0.78f),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = stroke
                )
                drawLine(color, Offset(w * 0.08f, h * 0.36f), Offset(w * 0.92f, h * 0.36f), strokeWidth = stroke.width)
                drawLine(color, Offset(w * 0.33f, h * 0.06f), Offset(w * 0.33f, h * 0.2f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.67f, h * 0.06f), Offset(w * 0.67f, h * 0.2f), strokeWidth = stroke.width, cap = StrokeCap.Round)
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
