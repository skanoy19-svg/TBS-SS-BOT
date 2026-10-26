package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TradingSignalOutput
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonCyanGlow
import com.example.ui.theme.SignalGold
import com.example.ui.theme.SignalGoldGlow
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalGreenGlow
import com.example.ui.theme.SignalRed
import com.example.ui.theme.SignalRedGlow
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceBorderHighlight
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SignalResultCard(
    output: TradingSignalOutput,
    onSaveToJournal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showRawJson by remember { mutableStateOf(false) }
    var showConfluenceDetails by remember { mutableStateOf(false) }

    val signalColor = when (output.signal) {
        "UP" -> SignalGreen
        "DOWN" -> SignalRed
        else -> SignalGold
    }

    val signalGlow = when (output.signal) {
        "UP" -> SignalGreenGlow
        "DOWN" -> SignalRedGlow
        else -> SignalGoldGlow
    }

    val signalIcon = when (output.signal) {
        "UP" -> Icons.Default.ArrowUpward
        "DOWN" -> Icons.Default.ArrowDownward
        else -> Icons.Default.RemoveCircleOutline
    }

    val rawJsonText = remember(output) {
        """
{
  "signal": "${output.signal}",
  "confidence_score": "${output.confidence_score}",
  "execution_type": "${output.execution_type}",
  "timeframe": "${output.timeframe}",
  "entry_zone": "${output.entry_zone}",
  "stop_loss": "${output.stop_loss}",
  "take_profit_1": "${output.take_profit_1}",
  "take_profit_2": "${output.take_profit_2}",
  "risk_reward_ratio": "${output.risk_reward_ratio}",
  "primary_strategy_used": "${output.primary_strategy_used}",
  "trade_rationale": "${output.trade_rationale}"
}
""".trimIndent()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("signal_result_card")
            .border(1.5.dp, signalColor.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceDark,
        tonalElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Engine banner badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(NeonCyanGlow, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = output.engineSource.uppercase(),
                        color = NeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Timeframe badge
                Box(
                    modifier = Modifier
                        .background(SurfaceElevated, RoundedCornerShape(6.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "TF: ${output.timeframe}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Decision Hero
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(signalGlow, RoundedCornerShape(12.dp))
                    .border(1.dp, signalColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(signalColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = signalIcon,
                        contentDescription = output.signal,
                        tint = Color(0xFF0A0F1D),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SIGNAL: ",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = output.signal,
                            color = signalColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                    }
                    Text(
                        text = output.execution_type,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Confidence pill
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "CONFIDENCE",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = output.confidence_score,
                        color = signalColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (output.confidenceInt >= 80) "≥ 80% VALID" else "< 80% FILTER",
                        color = if (output.confidenceInt >= 80) SignalGreen else SignalGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Confidence Progress Bar
            val progress = (output.confidenceInt / 100f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = signalColor,
                trackColor = SurfaceElevated,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Key Levels Table (Only if not HOLD, or displayed with caution if HOLD)
            if (output.signal != "HOLD") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceElevated, RoundedCornerShape(10.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "RISK MANAGEMENT & TARGET LEVELS",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LevelItem("ENTRY ZONE", output.entry_zone, TextPrimary)
                        LevelItem("STOP LOSS", output.stop_loss, SignalRed)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LevelItem("TAKE PROFIT 1", output.take_profit_1, SignalGreen)
                        LevelItem("TAKE PROFIT 2", output.take_profit_2, SignalGreen)
                        LevelItem("RISK / REWARD", output.risk_reward_ratio, NeonCyan)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceElevated, RoundedCornerShape(10.dp))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SignalGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NO ORDER PLACED • Strict Capital Preservation Active",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Strategy
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRIMARY STRATEGY: ",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = output.primary_strategy_used,
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Trade Rationale
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C121F), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "CONFLUENCE RATIONALE",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = output.trade_rationale,
                        color = TextPrimary,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expandable Confluence Matrix Telemetry
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showConfluenceDetails = !showConfluenceDetails }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "View 6 Core Modules Breakdown",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (showConfluenceDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = NeonCyan
                )
            }

            AnimatedVisibility(visible = showConfluenceDetails) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(SurfaceElevated, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    ModuleCheckItem("1. Multi-Indicator Confluence (≥3 Confirmations)", output.indicatorConfluencePassed)
                    ModuleCheckItem("2. Price Action & Candlestick Pattern", output.priceActionPassed)
                    ModuleCheckItem("3. Macro / News Bias (Hawkish/Dovish/Event)", !output.macroConflict)
                    ModuleCheckItem("4. Smart Money Concepts (Sweep, MSS, FVG/OB)", output.smcPassed)
                    ModuleCheckItem("5. Breakout Validation (Volume ≥1.7x)", output.breakoutValid)
                    ModuleCheckItem("6. Scalping Momentum & HTF Alignment", output.scalpingAligned && !output.htfConflict)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expandable Raw JSON Output
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRawJson = !showRawJson }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showRawJson) "Hide Standard JSON" else "View Standard Output JSON",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (showRawJson) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary
                )
            }

            AnimatedVisibility(visible = showRawJson) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(Color(0xFF070B14), RoundedCornerShape(8.dp))
                        .border(1.dp, SurfaceBorderHighlight, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "JSON SPECIFICATION",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy JSON",
                            tint = NeonCyan,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Trading Signal JSON", rawJsonText))
                                    Toast.makeText(context, "Copied JSON payload to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = rawJsonText,
                        color = Color(0xFF64D2FF),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Trading Signal JSON", rawJsonText))
                        Toast.makeText(context, "Copied JSON to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_json_button"),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorderHighlight))
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy JSON", color = TextPrimary, fontSize = 12.sp)
                }

                Button(
                    onClick = onSaveToJournal,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_journal_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = Color(0xFF001F26)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Log", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun LevelItem(label: String, value: String, valueColor: Color) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(
            text = value,
            color = valueColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ModuleCheckItem(title: String, passed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.RemoveCircleOutline,
            contentDescription = null,
            tint = if (passed) SignalGreen else TextMuted,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = if (passed) TextPrimary else TextMuted,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = if (passed) "CONFIRMED" else "STANDBY",
            color = if (passed) SignalGreen else TextMuted,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
