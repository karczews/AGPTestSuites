package com.github.karczews.speedgauge

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.karczews.speedgauge.ui.theme.GaugeBackground
import com.github.karczews.speedgauge.ui.theme.GaugeBezelInner
import com.github.karczews.speedgauge.ui.theme.GaugeBezelOuter
import com.github.karczews.speedgauge.ui.theme.GaugeHub
import com.github.karczews.speedgauge.ui.theme.GaugeHubCenter
import com.github.karczews.speedgauge.ui.theme.GaugeMajorTick
import com.github.karczews.speedgauge.ui.theme.GaugeMinorTick
import com.github.karczews.speedgauge.ui.theme.GaugeNeedle
import com.github.karczews.speedgauge.ui.theme.GaugeNeedleShadow
import com.github.karczews.speedgauge.ui.theme.GaugeNumeral
import com.github.karczews.speedgauge.ui.theme.GaugeStripBackground
import com.github.karczews.speedgauge.ui.theme.GaugeStripLabel
import com.github.karczews.speedgauge.ui.theme.GaugeStripValue
import com.github.karczews.speedgauge.ui.theme.SpeedGaugeTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

const val MAX_SPEED_KMH = 260f
const val START_ANGLE_DEG = 135f
const val SWEEP_DEG = 270f

const val GAUGE_MARGIN_FACTOR = 0.94f

fun speedToAngle(
    speedKmh: Float,
    startAngleDeg: Float = START_ANGLE_DEG,
    sweepDeg: Float = SWEEP_DEG,
    maxSpeedKmh: Float = MAX_SPEED_KMH,
): Float {
    val clamped = speedKmh.coerceIn(0f, maxSpeedKmh)
    return startAngleDeg + (clamped / maxSpeedKmh) * sweepDeg
}

@Composable
fun SpeedGauge(
    speedKmh: Float,
    modifier: Modifier = Modifier,
    outsideTempC: String = "21°C",
    tripLabel: String = "TRIP B",
    tripValue: String = "0.0",
) {
    Column(modifier = modifier.background(GaugeBackground)) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            drawGaugeDial()
            drawNeedle(speedKmh)
        }
        TripComputerStrip(
            outsideTempC = outsideTempC,
            tripLabel = tripLabel,
            tripValue = tripValue,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp),
        )
    }
}

private fun DrawScope.drawGaugeDial() {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val radius = min(size.width, size.height) / 2f * GAUGE_MARGIN_FACTOR
    val center = Offset(cx, cy)

    // Outer copper bezel.
    drawArc(
        color = GaugeBezelOuter,
        startAngle = START_ANGLE_DEG,
        sweepAngle = SWEEP_DEG,
        useCenter = false,
        topLeft = Offset(cx - radius, cy - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
        style = Stroke(width = radius * 0.035f),
    )
    // Thin bright inner edge like the reference cluster.
    drawArc(
        color = GaugeBezelInner,
        startAngle = START_ANGLE_DEG,
        sweepAngle = SWEEP_DEG,
        useCenter = false,
        topLeft = Offset(cx - radius * 0.965f, cy - radius * 0.965f),
        size = androidx.compose.ui.geometry.Size(radius * 1.93f, radius * 1.93f),
        style = Stroke(width = radius * 0.008f),
    )

    val majorTickOuter = radius * 0.95f
    val majorTickInner = radius * 0.83f
    val minorTickOuter = radius * 0.95f
    val minorTickInner = radius * 0.89f
    val labelRadius = radius * 0.68f

    var speed = 0f
    while (speed <= MAX_SPEED_KMH) {
        val angle = speedToAngle(speed)
        val rad = Math.toRadians(angle.toDouble())
        val isMajor = (speed % 20f) == 0f
        val inner = if (isMajor) majorTickInner else minorTickInner
        val outer = if (isMajor) majorTickOuter else minorTickOuter
        drawLine(
            color = if (isMajor) GaugeMajorTick else GaugeMinorTick,
            start = Offset(
                cx + inner * cos(rad).toFloat(),
                cy + inner * sin(rad).toFloat(),
            ),
            end = Offset(
                cx + outer * cos(rad).toFloat(),
                cy + outer * sin(rad).toFloat(),
            ),
            strokeWidth = if (isMajor) radius * 0.028f else radius * 0.016f,
            cap = StrokeCap.Butt,
        )
        speed += 10f
    }

    // Numerals 0..260 every 20.
    val paint = Paint().apply {
        isAntiAlias = true
        color = GaugeNumeral.toArgb()
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = radius * 0.11f
    }
    var label = 0
    while (label <= MAX_SPEED_KMH.toInt()) {
        val angle = speedToAngle(label.toFloat())
        val rad = Math.toRadians(angle.toDouble())
        val x = cx + labelRadius * cos(rad).toFloat()
        // +1/3 text size to optically center vertically.
        val y = cy + labelRadius * sin(rad).toFloat() + paint.textSize / 3f
        drawContext.canvas.nativeCanvas.drawText(label.toString(), x, y, paint)
        label += 20
    }
}

private fun DrawScope.drawNeedle(speedKmh: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val radius = min(size.width, size.height) / 2f * GAUGE_MARGIN_FACTOR
    val center = Offset(cx, cy)
    val angleRad = Math.toRadians(speedToAngle(speedKmh).toDouble())
    val dir = Offset(cos(angleRad).toFloat(), sin(angleRad).toFloat())
    val tip = center + dir * radius * 0.78f
    val tail = center - dir * radius * 0.18f

    // Dark under-stroke for the glowing-plastic look.
    drawLine(
        color = GaugeNeedleShadow,
        start = tail,
        end = tip,
        strokeWidth = radius * 0.055f,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = GaugeNeedle,
        start = tail,
        end = tip,
        strokeWidth = radius * 0.032f,
        cap = StrokeCap.Round,
    )
    drawCircle(color = GaugeHub, radius = radius * 0.075f, center = center)
    drawCircle(
        color = GaugeNeedle,
        radius = radius * 0.075f,
        center = center,
        style = Stroke(width = radius * 0.015f),
    )
    drawCircle(color = GaugeHubCenter, radius = radius * 0.03f, center = center)
}

@Composable
private fun TripComputerStrip(
    outsideTempC: String,
    tripLabel: String,
    tripValue: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(GaugeStripBackground, RoundedCornerShape(4.dp))
            .padding(horizontal = 18.dp, vertical = 6.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column {
                Text(text = "OUTSIDE", color = GaugeStripLabel, fontSize = 9.sp)
                Text(text = outsideTempC, color = GaugeStripValue, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(28.dp))
            Column {
                Text(text = "TRIP", color = GaugeStripLabel, fontSize = 9.sp)
                Text(text = tripLabel.removePrefix("TRIP ").ifEmpty { "B" }, color = GaugeStripLabel, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(28.dp))
            Text(text = tripValue, color = GaugeStripValue, fontSize = 22.sp)
        }
    }
}

@Composable
fun SpeedGaugeScreen(modifier: Modifier = Modifier) {
    var speed by remember { mutableFloatStateOf(15f) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GaugeBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        SpeedGauge(
            speedKmh = speed,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "${speed.toInt()} km/h",
            color = GaugeStripValue,
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 16.dp),
        )
        Slider(
            value = speed,
            onValueChange = { speed = it },
            valueRange = 0f..MAX_SPEED_KMH,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SpeedGaugePreview() {
    SpeedGaugeTheme {
        SpeedGauge(speedKmh = 15f, modifier = Modifier.background(GaugeBackground))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SpeedGaugeScreenPreview() {
    SpeedGaugeTheme {
        SpeedGaugeScreen()
    }
}
