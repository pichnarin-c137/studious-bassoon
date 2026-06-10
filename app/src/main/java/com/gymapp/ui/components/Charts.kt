package com.gymapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.gymapp.data.model.DaySession
import com.gymapp.data.model.HourLoad
import com.gymapp.ui.theme.Spacing
import com.gymapp.ui.theme.accentInk
import com.gymapp.util.DateTimeUtil

/**
 * This-week ticks: a thin lime vertical tick on a logged day, a small muted dot on a missed day,
 * with the weekday initial below. Built from layout primitives (no chart dependency).
 */
@Composable
fun WeekBarChart(days: List<DaySession>, modifier: Modifier = Modifier) {
    val tickArea = 40.dp
    val miss = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        days.forEach { day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Box(
                    modifier = Modifier.height(tickArea).fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    if (day.done) {
                        Box(
                            Modifier
                                .width(4.dp)
                                .height(tickArea)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.accentInk),
                        )
                    } else {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(miss))
                    }
                }
                Text(
                    text = DateTimeUtil.weekdayInitial(day.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Hourly gym busy-ness: one bar per open hour scaled to its load, the current hour in lime and the
 * rest muted — so a walk-in member can eyeball a quiet time. Layout primitives, no chart lib.
 */
@Composable
fun BusynessStrip(hourly: List<HourLoad>, modifier: Modifier = Modifier) {
    val maxLoad = (hourly.maxOfOrNull { it.load } ?: 1).coerceAtLeast(1)
    val muted = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f)
    Row(
        modifier = modifier.fillMaxWidth().height(44.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        hourly.forEach { h ->
            val frac = h.load / maxLoad.toFloat()
            Box(
                Modifier
                    .weight(1f)
                    .height((8 + 32 * frac).dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (h.current) MaterialTheme.colorScheme.accentInk else muted),
            )
        }
    }
}

/**
 * Rising volume-lifted trend: a lime polyline scaled to the window's min/max, a faint flat lime
 * area fill beneath it, and a dot on the latest point. Hand-drawn on a [Canvas].
 */
@Composable
fun TrendLineChart(
    values: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.accentInk,
) {
    Canvas(modifier.fillMaxWidth().height(140.dp)) {
        if (values.size < 2) return@Canvas
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0.0 } ?: 1.0
        val pad = size.height * 0.12f
        val usableH = size.height - pad * 2
        val stepX = size.width / (values.size - 1)

        fun pointAt(index: Int): Offset {
            val x = stepX * index
            val norm = ((values[index] - min) / range).toFloat()
            val y = pad + usableH * (1f - norm)
            return Offset(x, y)
        }

        val line = Path()
        values.indices.forEach { i ->
            val p = pointAt(i)
            if (i == 0) line.moveTo(p.x, p.y) else line.lineTo(p.x, p.y)
        }

        // Faint flat fill under the line (translucent solid, not a gradient).
        val fill = Path().apply {
            addPath(line)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(fill, color = lineColor.copy(alpha = 0.10f))
        drawPath(
            path = line,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawCircle(color = lineColor, radius = 4.dp.toPx(), center = pointAt(values.lastIndex))
    }
}
