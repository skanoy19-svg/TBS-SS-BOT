package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CandlestickPattern
import com.example.data.model.MacroSentiment
import com.example.data.model.MarketTimeframe
import com.example.data.model.SMCSweep
import com.example.data.model.SMCStructureShift
import com.example.data.model.TradingInputState
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun InputTemplateDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onApplyTemplate: (TradingInputState) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    var parseError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceDark,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.IntegrationInstructions,
                        contentDescription = null,
                        tint = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INPUT TEMPLATE IMPORT",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Text(
                    text = "Paste or edit structured trading parameters matching the system prompt template:",
                    color = TextMuted,
                    fontSize = 11.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        parseError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .testTag("template_input_field"),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    )
                )

                if (parseError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = parseError ?: "",
                        color = androidx.compose.ui.graphics.Color(0xFFFF5252),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            try {
                                val parsed = parseTemplateText(text)
                                onApplyTemplate(parsed)
                                onDismiss()
                            } catch (e: Exception) {
                                parseError = "Failed to parse template: ${e.message}"
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_template_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = androidx.compose.ui.graphics.Color(0xFF001F26)
                        )
                    ) {
                        Text("Parse & Load", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun parseTemplateText(raw: String): TradingInputState {
    var asset = "BTCUSDT"
    var tf = MarketTimeframe.TF_15M
    var rsi = 45.0
    var macd = "Bullish Cross"
    var ema = "Bullish Stack (20>50>200)"
    var vol = 1.8
    var sweep = SMCSweep.NONE
    var mss = SMCStructureShift.NONE
    var fvg = 67200.0
    var res = 68000.0
    var sup = 66500.0
    var sent = MacroSentiment.NEUTRAL

    raw.lines().forEach { line ->
        val trimmed = line.trim()
        when {
            trimmed.startsWith("[ASSET]:", ignoreCase = true) -> {
                val value = trimmed.removePrefix("[ASSET]:").trim()
                asset = value.split(" ").firstOrNull()?.replace("/", "") ?: "BTCUSDT"
            }
            trimmed.startsWith("[TIMEFRAME]:", ignoreCase = true) -> {
                val value = trimmed.removePrefix("[TIMEFRAME]:").trim().lowercase()
                tf = when {
                    value.contains("1m") -> MarketTimeframe.TF_1M
                    value.contains("5m") -> MarketTimeframe.TF_5M
                    value.contains("15m") -> MarketTimeframe.TF_15M
                    value.contains("1h") -> MarketTimeframe.TF_1H
                    value.contains("4h") -> MarketTimeframe.TF_4H
                    value.contains("daily") || value.contains("1d") -> MarketTimeframe.TF_DAILY
                    else -> MarketTimeframe.TF_15M
                }
            }
            trimmed.startsWith("[INDICATORS]:", ignoreCase = true) -> {
                val value = trimmed.removePrefix("[INDICATORS]:")
                val rsiMatch = Regex("""RSI:\s*([0-9.]+)""", RegexOption.IGNORE_CASE).find(value)
                rsiMatch?.groupValues?.get(1)?.toDoubleOrNull()?.let { rsi = it }

                val volMatch = Regex("""Volume:\s*([0-9.]+)""", RegexOption.IGNORE_CASE).find(value)
                volMatch?.groupValues?.get(1)?.toDoubleOrNull()?.let { vol = it }

                if (value.contains("Bearish", ignoreCase = true)) macd = "Bearish Histogram"
                if (value.contains("Bullish", ignoreCase = true)) macd = "Bullish Cross"
            }
            trimmed.startsWith("[SMC DATA]:", ignoreCase = true) -> {
                val value = trimmed.removePrefix("[SMC DATA]:")
                if (value.contains("Low", ignoreCase = true)) sweep = SMCSweep.SESSION_LOW_SWEEP
                if (value.contains("High", ignoreCase = true)) sweep = SMCSweep.SESSION_HIGH_SWEEP
                if (value.contains("Bullish", ignoreCase = true)) mss = SMCStructureShift.BULLISH_MSS
                if (value.contains("Bearish", ignoreCase = true)) mss = SMCStructureShift.BEARISH_MSS
            }
            trimmed.startsWith("[BREAKOUT LEVEL]:", ignoreCase = true) -> {
                val value = trimmed.removePrefix("[BREAKOUT LEVEL]:")
                val resMatch = Regex("""Resistance:\s*([0-9.]+)""", RegexOption.IGNORE_CASE).find(value)
                resMatch?.groupValues?.get(1)?.toDoubleOrNull()?.let { res = it }
                val supMatch = Regex("""Support:\s*([0-9.]+)""", RegexOption.IGNORE_CASE).find(value)
                supMatch?.groupValues?.get(1)?.toDoubleOrNull()?.let { sup = it }
            }
            trimmed.startsWith("[NEWS SENTIMENT]:", ignoreCase = true) -> {
                val value = trimmed.removePrefix("[NEWS SENTIMENT]:").trim().lowercase()
                sent = when {
                    value.contains("dovish") -> MacroSentiment.DOVISH
                    value.contains("hawkish") -> MacroSentiment.HAWKISH
                    value.contains("bullish") -> MacroSentiment.BULLISH
                    value.contains("bearish") -> MacroSentiment.BEARISH
                    value.contains("cpi") || value.contains("fomc") -> MacroSentiment.HIGH_IMPACT_NEWS
                    else -> MacroSentiment.NEUTRAL
                }
            }
        }
    }

    val price = if (sup > 0 && res > 0) (sup + res) / 2.0 else 67450.0

    return TradingInputState(
        asset = asset,
        currentPrice = price,
        timeframe = tf,
        rsi = rsi,
        macdStatus = macd,
        emaAlignment = ema,
        volumeRatio = vol,
        supportLevel = sup,
        resistanceLevel = res,
        smcSweep = sweep,
        smcStructureShift = mss,
        fvgObLevel = fvg,
        newsSentiment = sent,
        candlestickPattern = if (rsi < 40) CandlestickPattern.PIN_BAR_HAMMER else CandlestickPattern.NONE
    )
}
