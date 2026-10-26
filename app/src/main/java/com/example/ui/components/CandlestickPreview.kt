package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TradingInputState
import com.example.data.model.TradingSignalOutput
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SignalGold
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

data class CandleData(
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val isBullish: Boolean
)

@Composable
fun CandlestickPreview(
    input: TradingInputState,
    signalOutput: TradingSignalOutput?,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Deterministic pseudo-random candles derived from current price & setup
    val candles = remember(input.asset, input.currentPrice, input.timeframe, input.trendStructure) {
        val basePrice = input.currentPrice.toFloat()
        val rng = Random(input.asset.hashCode() + input.timeframe.ordinal)
        val list = mutableListOf<CandleData>()
        var prevClose = basePrice * 0.985f

        val count = 18
        for (i in 0 until count) {
            val volatility = basePrice * 0.006f
            val isBullishBias = input.trendStructure.contains("Higher Highs")
            val delta = (rng.nextFloat() - (if (isBullishBias) 0.42f else 0.58f)) * volatility * 2.2f
            val open = prevClose
            val close = (open + delta).coerceAtLeast(basePrice * 0.94f)
            val high = maxOf(open, close) + rng.nextFloat() * (volatility * 0.9f)
            val low = minOf(open, close) - rng.nextFloat() * (volatility * 0.9f)
            val isBullish = close >= open
            list.add(CandleData(open, high, low, close, isBullish))
            prevClose = close
        }
        // Force the last candle close to match currentPrice
        val lastOpen = list.last().open
        val currentP = basePrice
        val lastH = maxOf(lastOpen, currentP) + (basePrice * 0.003f)
        val lastL = minOf(lastOpen, currentP) - (basePrice * 0.003f)
        list[list.lastIndex] = CandleData(lastOpen, lastH, lastL, currentP, currentP >= lastOpen)
        list
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${input.asset} • ${input.timeframe.label}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = String.format(Locale.US, "Ref: %.2f", input.currentPrice),
                    color = NeonCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val w = size.width
                val h = size.height

                // Determine min and max price on canvas
                val allHighs = candles.map { it.high } + listOf(input.resistanceLevel.toFloat())
                val allLows = candles.map { it.low } + listOf(input.supportLevel.toFloat())
                val maxP = allHighs.maxOrNull() ?: (input.currentPrice.toFloat() * 1.02f)
                val minP = allLows.minOrNull() ?: (input.currentPrice.toFloat() * 0.98f)
                val range = if (maxP - minP > 0) maxP - minP else 1f

                fun priceToY(price: Float): Float {
                    val normalized = (price - minP) / range
                    return (h - 16f) - (normalized * (h - 32f))
                }

                // Grid lines (horizontal)
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                for (step in 1..3) {
                    val y = h * (step / 4f)
                    drawLine(
                        color = Color(0xFF1E283E),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )
                }

                // Support & Resistance zones
                val resY = priceToY(input.resistanceLevel.toFloat())
                val supY = priceToY(input.supportLevel.toFloat())

                if (resY in 0f..h) {
                    drawLine(
                        color = SignalRed.copy(alpha = 0.5f),
                        start = Offset(0f, resY),
                        end = Offset(w, resY),
                        strokeWidth = 1.5f,
                        pathEffect = dashEffect
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "RES: ${input.resistanceLevel.toInt()}",
                        topLeft = Offset(w - 90f, resY - 14f),
                        style = TextStyle(color = SignalRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    )
                }

                if (supY in 0f..h) {
                    drawLine(
                        color = SignalGreen.copy(alpha = 0.5f),
                        start = Offset(0f, supY),
                        end = Offset(w, supY),
                        strokeWidth = 1.5f,
                        pathEffect = dashEffect
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "SUP: ${input.supportLevel.toInt()}",
                        topLeft = Offset(w - 90f, supY + 2f),
                        style = TextStyle(color = SignalGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    )
                }

                // Draw Candles
                val candleWidth = (w / (candles.size + 2)) * 0.65f
                val spacing = w / (candles.size + 2)

                candles.forEachIndexed { index, candle ->
                    val x = spacing * (index + 1)
                    val candleColor = if (candle.isBullish) SignalGreen else SignalRed
                    val openY = priceToY(candle.open)
                    val closeY = priceToY(candle.close)
                    val highY = priceToY(candle.high)
                    val lowY = priceToY(candle.low)

                    // Wick
                    drawLine(
                        color = candleColor,
                        start = Offset(x, highY),
                        end = Offset(x, lowY),
                        strokeWidth = 1.5f
                    )

                    // Body
                    val top = minOf(openY, closeY)
                    val bodyHeight = maxOf(abs(openY - closeY), 2f)
                    drawRect(
                        color = candleColor,
                        topLeft = Offset(x - candleWidth / 2f, top),
                        size = Size(candleWidth, bodyHeight)
                    )
                }

                // If signalOutput exists, overlay signal target lines (SL & TP)
                if (signalOutput != null && signalOutput.signal != "HOLD") {
                    val slVal = signalOutput.stop_loss.toFloatOrNull()
                    val tp1Val = signalOutput.take_profit_1.toFloatOrNull()
                    if (slVal != null && tp1Val != null) {
                        val slY = priceToY(slVal).coerceIn(4f, h - 4f)
                        val tpY = priceToY(tp1Val).coerceIn(4f, h - 4f)

                        // Draw SL line
                        drawLine(
                            color = SignalRed,
                            start = Offset(0f, slY),
                            end = Offset(w, slY),
                            strokeWidth = 2f
                        )
                        // Draw TP line
                        drawLine(
                            color = SignalGreen,
                            start = Offset(0f, tpY),
                            end = Offset(w, tpY),
                            strokeWidth = 2f
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Chart Footer Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RSI: ${input.rsi.toInt()}  |  MACD: ${input.macdStatus}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Vol: ${input.volumeRatio}x",
                    color = if (input.volumeRatio >= 1.5) SignalGreen else TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
