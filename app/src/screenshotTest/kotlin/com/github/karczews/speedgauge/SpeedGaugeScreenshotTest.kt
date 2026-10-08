package com.github.karczews.speedgauge

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.github.karczews.speedgauge.ui.theme.SpeedGaugeTheme

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SpeedGaugeAtRestPreview() {
    SpeedGaugeTheme {
        SpeedGauge(speedKmh = 0f)
    }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SpeedGaugeCruisingPreview() {
    SpeedGaugeTheme {
        SpeedGauge(speedKmh = 90f)
    }
}

@PreviewTest
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SpeedGaugeMaxPreview() {
    SpeedGaugeTheme {
        SpeedGauge(speedKmh = 260f)
    }
}
