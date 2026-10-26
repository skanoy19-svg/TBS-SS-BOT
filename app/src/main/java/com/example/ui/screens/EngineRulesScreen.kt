package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SignalGold
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EngineRulesScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        // Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "QUANT ENGINE SPECIFICATION",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Core Evaluation Modules & Mathematical Confluence",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Strict Rules Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SignalGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceDark
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "STRICT SIGNAL RULES (MANDATORY)",
                    color = SignalGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                RuleBullet(
                    title = "High Probability Rule (≥80% Threshold):",
                    desc = "Signals (UP / DOWN) are strictly generated ONLY when overall multi-module confidence score reaches 80% or greater. Below 80%, the system mandates HOLD / NEUTRAL."
                )

                RuleBullet(
                    title = "Conflict Resolution Policy:",
                    desc = "If Daily/1H macro trends conflict with 15M/5M setups, or if high-impact news contradicts technicals, output is forced to HOLD / NEUTRAL."
                )

                RuleBullet(
                    title = "Precise Risk Management:",
                    desc = "Every valid signal includes exact calculated Entry Zone, Stop Loss beyond structural liquidity/OB, Take Profit 1 (1:1.8), Take Profit 2 (1:2.8), and Risk-to-Reward ratio."
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "6 CORE EVALUATION MODULES",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        ModuleDocCard(
            number = "01",
            icon = Icons.Default.Timeline,
            title = "Multi-Indicator Confluence",
            bullets = listOf(
                "Cross-checks RSI (14), MACD (12,26,9), and Moving Averages (EMA 20/50/200).",
                "Volume Expansion validation (minimum 1.5x - 2.0x 20-period average volume).",
                "Requires at least 3 distinct indicator confirmations before triggering UP/DOWN bias."
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        ModuleDocCard(
            number = "02",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            title = "Price Action & Chart Analysis",
            bullets = listOf(
                "Evaluates trend structure: Higher Highs & Higher Lows (Bullish) vs Lower Highs & Lower Lows (Bearish).",
                "Detects reversal candlestick formations: Engulfing, Pin bar / Hammer, Shooting Star, Morning / Evening Star.",
                "Multimodal chart screenshot input analyzed for key support and resistance zones."
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        ModuleDocCard(
            number = "03",
            icon = Icons.Default.Newspaper,
            title = "Fundamental & News Sentiment",
            bullets = listOf(
                "Evaluates macro sentiment: Hawkish/Dovish Fed, risk-on/risk-off dynamics.",
                "High-impact events (CPI, FOMC, NFP) automatically override short-term technicals.",
                "Prevents entering prior to major volatility events."
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        ModuleDocCard(
            number = "04",
            icon = Icons.Default.Layers,
            title = "Smart Money Concepts (SMC / ICT)",
            bullets = listOf(
                "Identifies Liquidity Sweeps (Asian/London Session Highs & Lows, Equal Highs/Lows).",
                "Detects Market Structure Shifts (MSS) and Change of Character (CHoCH).",
                "Zones entries at Fair Value Gaps (FVG) and mitigated Order Blocks (OB)."
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        ModuleDocCard(
            number = "05",
            icon = Icons.Default.Bolt,
            title = "Breakout Validation",
            bullets = listOf(
                "Requires volume expansion of at least 1.7x - 2.0x average volume.",
                "Enforces candle body close beyond key level to prevent liquidity wicks and fakeouts.",
                "Penalizes false breakouts as trapped liquidity."
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        ModuleDocCard(
            number = "06",
            icon = Icons.Default.Speed,
            title = "Scalping & Short-Term Momentum",
            bullets = listOf(
                "For lower timeframes (1m-5m), monitors 9/21 EMA dynamic crossovers.",
                "Detects rapid momentum spikes and order flow velocity.",
                "Demands alignment with higher timeframe (1h/4h) trend to prevent trading against the tide."
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // JSON Schema Reference
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            color = SurfaceDark
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "STANDARDIZED OUTPUT SCHEMA",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = """
{
  "signal": "UP | DOWN | HOLD",
  "confidence_score": "0% - 100%",
  "execution_type": "Market Entry | Limit Entry | No Trade",
  "timeframe": "15m",
  "entry_zone": "0.0000",
  "stop_loss": "0.0000",
  "take_profit_1": "0.0000",
  "take_profit_2": "0.0000",
  "risk_reward_ratio": "1:2.5",
  "primary_strategy_used": "SMC / Multi-Indicator / Breakout / Scalping",
  "trade_rationale": "Brief 1-2 sentence explanation of signal confluence."
}
""".trimIndent(),
                    color = Color(0xFF64D2FF),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    lineHeight = 15.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RuleBullet(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = SignalGold,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
        }
        Text(
            text = desc,
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(start = 19.dp)
        )
    }
}

@Composable
private fun ModuleDocCard(
    number: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    bullets: List<String>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceDark
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(SurfaceElevated, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = number, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = icon, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            bullets.forEach { bullet ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(text = "•", color = NeonCyan, fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
                    Text(text = bullet, color = TextSecondary, fontSize = 11.5.sp, lineHeight = 16.sp)
                }
            }
        }
    }
}
