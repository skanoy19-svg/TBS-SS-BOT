package com.example.data.model

/**
 * Output format matching the unified trading signal generator specification.
 */
data class TradingSignalOutput(
    val signal: String, // "UP" | "DOWN" | "HOLD"
    val confidence_score: String, // e.g. "88%"
    val execution_type: String, // "Market Entry" | "Limit Entry" | "No Trade"
    val timeframe: String,
    val entry_zone: String,
    val stop_loss: String,
    val take_profit_1: String,
    val take_profit_2: String,
    val risk_reward_ratio: String,
    val primary_strategy_used: String,
    val trade_rationale: String,
    // Telemetry fields for module breakdown in UI:
    val confidenceInt: Int = 0,
    val indicatorConfluencePassed: Boolean = false,
    val priceActionPassed: Boolean = false,
    val smcPassed: Boolean = false,
    val breakoutValid: Boolean = false,
    val scalpingAligned: Boolean = false,
    val macroConflict: Boolean = false,
    val htfConflict: Boolean = false,
    val engineSource: String = "Quant Confluence Core"
)

enum class SignalDirection {
    UP,
    DOWN,
    HOLD
}

enum class MarketTimeframe(val label: String, val isLowerTimeframe: Boolean) {
    TF_1M("1m", true),
    TF_5M("5m", true),
    TF_15M("15m", false),
    TF_1H("1h", false),
    TF_4H("4h", false),
    TF_DAILY("Daily", false)
}

enum class MacroSentiment(val label: String) {
    NEUTRAL("Neutral / Baseline"),
    BULLISH("Bullish / Risk-On"),
    BEARISH("Bearish / Risk-Off"),
    HAWKISH("Hawkish Fed / High Yields"),
    DOVISH("Dovish Fed / Liquidity Boost"),
    HIGH_IMPACT_NEWS("High Impact Event (CPI / FOMC)")
}

enum class SMCSweep(val label: String) {
    NONE("No Sweep Detected"),
    SESSION_LOW_SWEEP("Asian / London Low Swept (Liquidity Grab)"),
    SESSION_HIGH_SWEEP("Asian / London High Swept (Liquidity Grab)"),
    EQUAL_HIGHS_SWEEP("Equal Highs (EQH) Purged"),
    EQUAL_LOWS_SWEEP("Equal Lows (EQL) Purged")
}

enum class SMCStructureShift(val label: String) {
    NONE("Structure Intact"),
    BULLISH_MSS("Bullish MSS (Market Structure Shift)"),
    BEARISH_MSS("Bearish MSS (Market Structure Shift)"),
    CHoCH("CHoCH (Change of Character)")
}

enum class CandlestickPattern(val label: String) {
    NONE("Neutral / In-Range"),
    BULLISH_ENGULFING("Bullish Engulfing"),
    BEARISH_ENGULFING("Bearish Engulfing"),
    PIN_BAR_HAMMER("Bullish Pin Bar / Hammer"),
    SHOOTING_STAR("Bearish Pin Bar / Shooting Star"),
    MORNING_STAR("Morning Star Reversal"),
    EVENING_STAR("Evening Star Reversal"),
    HEAD_AND_SHOULDERS("Head & Shoulders Top"),
    INVERSE_HEAD_AND_SHOULDERS("Inverse Head & Shoulders")
}

data class TradingInputState(
    val asset: String = "BTCUSDT",
    val currentPrice: Double = 67450.0,
    val timeframe: MarketTimeframe = MarketTimeframe.TF_15M,
    // Module 1: Indicators
    val rsi: Double = 34.0, // 0 - 100
    val macdStatus: String = "Bullish Cross", // Bullish Cross, Bearish Cross, Bullish Histogram, Bearish Histogram
    val emaAlignment: String = "Bullish Stack (20>50>200)", // Bullish Stack (20>50>200), Bearish Stack (20<50<200), Neutral / Mixed
    val volumeRatio: Double = 1.9, // x times average volume
    // Module 2: Price Action
    val trendStructure: String = "Higher Highs / Higher Lows", // Higher Highs / Higher Lows, Lower Highs / Lower Lows, Consolidation
    val candlestickPattern: CandlestickPattern = CandlestickPattern.PIN_BAR_HAMMER,
    val supportLevel: Double = 66800.0,
    val resistanceLevel: Double = 68200.0,
    // Module 3: News / Macro
    val newsSentiment: MacroSentiment = MacroSentiment.DOVISH,
    val newsHeadline: String = "Fed signals accommodative stance; inflation cooling",
    // Module 4: Smart Money Concepts (SMC)
    val smcSweep: SMCSweep = SMCSweep.SESSION_LOW_SWEEP,
    val smcStructureShift: SMCStructureShift = SMCStructureShift.BULLISH_MSS,
    val fvgObLevel: Double = 67200.0, // Fair Value Gap / Order Block level
    val isMitigated: Boolean = true,
    // Module 5: Breakout
    val isBreakoutSetup: Boolean = false,
    val candleClosedBeyondLevel: Boolean = true,
    // Module 6: Scalping (1m-5m)
    val ema9Cross21: String = "Bullish Cross", // Bullish Cross, Bearish Cross, Neutral
    val momentumSpike: Boolean = true,
    val htfTrendAligned: Boolean = true,
    // Chart screenshot attachment URI (optional)
    val chartImageUri: String? = null
)
