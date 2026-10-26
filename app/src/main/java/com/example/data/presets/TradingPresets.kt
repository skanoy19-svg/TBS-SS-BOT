package com.example.data.presets

import com.example.data.model.CandlestickPattern
import com.example.data.model.MacroSentiment
import com.example.data.model.MarketTimeframe
import com.example.data.model.SMCSweep
import com.example.data.model.SMCStructureShift
import com.example.data.model.TradingInputState

data class PresetScenario(
    val id: String,
    val title: String,
    val assetSymbol: String,
    val expectedOutcome: String,
    val description: String,
    val state: TradingInputState
)

object TradingPresets {

    val presets = listOf(
        PresetScenario(
            id = "btc_bullish_smc",
            title = "BTC 15m SMC Sweep + MSS",
            assetSymbol = "BTCUSDT",
            expectedOutcome = "UP (88% Confluence)",
            description = "Session Low swept, Bullish MSS confirmed, RSI oversold bounce with 1.9x volume expansion.",
            state = TradingInputState(
                asset = "BTCUSDT",
                currentPrice = 67450.0,
                timeframe = MarketTimeframe.TF_15M,
                rsi = 34.0,
                macdStatus = "Bullish Cross",
                emaAlignment = "Bullish Stack (20>50>200)",
                volumeRatio = 1.9,
                trendStructure = "Higher Highs / Higher Lows",
                candlestickPattern = CandlestickPattern.PIN_BAR_HAMMER,
                supportLevel = 66800.0,
                resistanceLevel = 68400.0,
                newsSentiment = MacroSentiment.DOVISH,
                newsHeadline = "Federal Reserve signals pause on rate hikes, risk assets rally",
                smcSweep = SMCSweep.SESSION_LOW_SWEEP,
                smcStructureShift = SMCStructureShift.BULLISH_MSS,
                fvgObLevel = 67200.0,
                isMitigated = true,
                isBreakoutSetup = false,
                candleClosedBeyondLevel = true,
                ema9Cross21 = "Bullish Cross",
                momentumSpike = true,
                htfTrendAligned = true
            )
        ),
        PresetScenario(
            id = "gold_bearish_rejection",
            title = "Gold (XAUUSD) 1h Bearish Rejection",
            assetSymbol = "XAUUSD",
            expectedOutcome = "DOWN (85% Confluence)",
            description = "Equal Highs purged, Bearish Engulfing from resistance, Hawkish yields surge.",
            state = TradingInputState(
                asset = "XAUUSD",
                currentPrice = 2642.50,
                timeframe = MarketTimeframe.TF_1H,
                rsi = 72.0,
                macdStatus = "Bearish Histogram",
                emaAlignment = "Bearish Stack (20<50<200)",
                volumeRatio = 2.1,
                trendStructure = "Lower Highs / Lower Lows",
                candlestickPattern = CandlestickPattern.BEARISH_ENGULFING,
                supportLevel = 2610.0,
                resistanceLevel = 2655.0,
                newsSentiment = MacroSentiment.HAWKISH,
                newsHeadline = "Treasury yields spike following stronger-than-expected jobs data",
                smcSweep = SMCSweep.EQUAL_HIGHS_SWEEP,
                smcStructureShift = SMCStructureShift.BEARISH_MSS,
                fvgObLevel = 2648.0,
                isMitigated = true,
                isBreakoutSetup = false,
                candleClosedBeyondLevel = false,
                ema9Cross21 = "Bearish Cross",
                momentumSpike = true,
                htfTrendAligned = true
            )
        ),
        PresetScenario(
            id = "eurusd_scalp",
            title = "EURUSD 5m Scalp Momentum",
            assetSymbol = "EURUSD",
            expectedOutcome = "UP (84% Scalp)",
            description = "5m 9/21 EMA golden cross, momentum spike, aligned with 1h trend direction.",
            state = TradingInputState(
                asset = "EURUSD",
                currentPrice = 1.0875,
                timeframe = MarketTimeframe.TF_5M,
                rsi = 46.0,
                macdStatus = "Bullish Cross",
                emaAlignment = "Bullish Stack (20>50>200)",
                volumeRatio = 1.8,
                trendStructure = "Higher Highs / Higher Lows",
                candlestickPattern = CandlestickPattern.BULLISH_ENGULFING,
                supportLevel = 1.0850,
                resistanceLevel = 1.0910,
                newsSentiment = MacroSentiment.BULLISH,
                newsHeadline = "ECB holds rates steady, optimistic Eurozone economic outlook",
                smcSweep = SMCSweep.SESSION_LOW_SWEEP,
                smcStructureShift = SMCStructureShift.BULLISH_MSS,
                fvgObLevel = 1.0868,
                isMitigated = true,
                isBreakoutSetup = true,
                candleClosedBeyondLevel = true,
                ema9Cross21 = "Bullish Cross",
                momentumSpike = true,
                htfTrendAligned = true
            )
        ),
        PresetScenario(
            id = "macro_conflict_hold",
            title = "ETH 15m Conflict -> Strict HOLD",
            assetSymbol = "ETHUSDT",
            expectedOutcome = "HOLD / NEUTRAL (<80% or Conflict)",
            description = "Lower timeframe bounce conflicts with 4H downtrend + upcoming high impact CPI release.",
            state = TradingInputState(
                asset = "ETHUSDT",
                currentPrice = 2580.0,
                timeframe = MarketTimeframe.TF_15M,
                rsi = 51.0,
                macdStatus = "Bullish Histogram",
                emaAlignment = "Neutral / Mixed",
                volumeRatio = 1.1,
                trendStructure = "Consolidation / Range",
                candlestickPattern = CandlestickPattern.NONE,
                supportLevel = 2520.0,
                resistanceLevel = 2650.0,
                newsSentiment = MacroSentiment.HIGH_IMPACT_NEWS,
                newsHeadline = "US CPI Inflation report releasing in 30 minutes; major volatility expected",
                smcSweep = SMCSweep.NONE,
                smcStructureShift = SMCStructureShift.NONE,
                fvgObLevel = 2570.0,
                isMitigated = false,
                isBreakoutSetup = false,
                candleClosedBeyondLevel = false,
                ema9Cross21 = "Neutral",
                momentumSpike = false,
                htfTrendAligned = false // Conflicts with HTF!
            )
        )
    )

    fun formatToInputTemplate(state: TradingInputState): String {
        return """
[ASSET]: ${state.asset} (Price: ${state.currentPrice})
[TIMEFRAME]: ${state.timeframe.label}
[INDICATORS]: RSI: ${state.rsi.toInt()} | MACD: ${state.macdStatus} | EMAs: ${state.emaAlignment} | Volume: ${state.volumeRatio}x
[SMC DATA]: Sweep: ${state.smcSweep.label} | FVG/OB Level: ${state.fvgObLevel} | MSS: ${state.smcStructureShift.label}
[BREAKOUT LEVEL]: Resistance: ${state.resistanceLevel} | Support: ${state.supportLevel}
[NEWS SENTIMENT]: ${state.newsSentiment.label}
[ATTACHMENT]: ${if (state.chartImageUri != null) "Screenshot Attached" else "None"}
""".trimIndent()
    }
}
