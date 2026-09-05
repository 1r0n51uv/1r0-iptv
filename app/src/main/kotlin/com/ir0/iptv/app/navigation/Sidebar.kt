package com.ir0.iptv.app.navigation

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ir0.iptv.app.theme.LocalAccento

@Composable
fun Sidebar(
    selezionata: Destinazione,
    onSeleziona: (Destinazione) -> Unit,
    modifier: Modifier = Modifier
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
        Destinazione.entries.forEach { destinazione ->
            SidebarButton(
                destinazione = destinazione,
                active = destinazione == selezionata,
                onClick = { onSeleziona(destinazione) }
            )
        }
        Box(Modifier.weight(1f))
        Box(Modifier.padding(bottom = 24.dp))
    }
}

@Composable
private fun SidebarButton(destinazione: Destinazione, active: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val accento = LocalAccento.current
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (active) accento else Color.Transparent)
            .then(
                if (isFocused && !active) {
                    Modifier.border(2.dp, accento, RoundedCornerShape(10.dp))
                } else {
                    Modifier
                }
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        SidebarGlyphIcon(destinazione = destinazione, color = if (active) Color(0xFF14161A) else Color(0xFF9AA0AA))
    }
}

/** Draws the glyph for a [Destinazione] by name, so newly added destinations (future steps) just
 * need a case added here rather than a parallel enum kept in sync by hand. */
@Composable
private fun SidebarGlyphIcon(destinazione: Destinazione, color: Color) {
    Canvas(modifier = Modifier.size(19.dp)) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        val w = size.width
        val h = size.height
        when (destinazione.name) {
            "DASHBOARD" -> {
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

            "SFOGLIA" -> {
                val cellSize = w * 0.36f
                val gap = w * 0.1f
                listOf(
                    Offset(w * 0.06f, h * 0.06f),
                    Offset(w * 0.06f + cellSize + gap, h * 0.06f),
                    Offset(w * 0.06f, h * 0.06f + cellSize + gap),
                    Offset(w * 0.06f + cellSize + gap, h * 0.06f + cellSize + gap)
                ).forEach { topLeft ->
                    drawRoundRect(
                        color = color,
                        topLeft = topLeft,
                        size = Size(cellSize, cellSize),
                        cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
                        style = stroke
                    )
                }
            }

            "CERCA" -> {
                val r = w * 0.28f
                val center = Offset(w * 0.42f, h * 0.42f)
                drawCircle(color = color, radius = r, center = center, style = stroke)
                drawLine(
                    color,
                    Offset(center.x + r * 0.7f, center.y + r * 0.7f),
                    Offset(w * 0.92f, h * 0.92f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }

            "PREFERITI" -> {
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

            "GUIDA" -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.08f, h * 0.14f),
                    size = Size(w * 0.84f, h * 0.74f),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = stroke
                )
                drawLine(color, Offset(w * 0.08f, h * 0.38f), Offset(w * 0.92f, h * 0.38f), strokeWidth = stroke.width)
                drawLine(color, Offset(w * 0.3f, h * 0.08f), Offset(w * 0.3f, h * 0.22f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * 0.7f, h * 0.08f), Offset(w * 0.7f, h * 0.22f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }

            "IMPOSTAZIONI" -> {
                drawCircle(color = color, radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
                drawCircle(color = color, radius = w * 0.4f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
            }

            else -> drawCircle(color = color, radius = w * 0.3f, center = Offset(w * 0.5f, h * 0.5f), style = stroke)
        }
    }
}
