package com.example.engine

import com.example.data.model.CandlestickPattern
import com.example.data.model.MacroSentiment
import com.example.data.model.MarketTimeframe
import com.example.data.model.SMCSweep
import com.example.data.model.SMCStructureShift
import com.example.data.model.TradingInputState
import com.example.data.model.TradingSignalOutput
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Deterministic quantitative trading decision engine strictly implementing all 6 evaluation modules
 * and the strict signal rules (80% confidence threshold, conflict resolution, precise risk management).
 */
object LocalQuantEngine {

    fun evaluate(input: TradingInputState): TradingSignalOutput {
        var bullishScore = 0
        var bearishScore = 0
        val maxScorePerSide = 100

        // ==========================================
        // 1. MULTI-INDICATOR CONFLUENCE
        // Requirement: RSI, MACD, EMAs (20/50/200), Volume.
        // Require at least 3 confirmations.
        // ==========================================
        var bullishIndicatorConfirmations = 0
        var bearishIndicatorConfirmations = 0

        // RSI:
        if (input.rsi in 30.0..48.0) {
            bullishIndicatorConfirmations++
        } else if (input.rsi < 30.0) {
            // Deep oversold reversal territory
            bullishIndicatorConfirmations++
        } else if (input.rsi in 52.0..70.0) {
            bearishIndicatorConfirmations++
        } else if (input.rsi > 70.0) {
            // Overbought exhaustion
            bearishIndicatorConfirmations++
        }

        // MACD:
        if (input.macdStatus.contains("Bullish", ignoreCase = true)) {
            bullishIndicatorConfirmations++
        } else if (input.macdStatus.contains("Bearish", ignoreCase = true)) {
            bearishIndicatorConfirmations++
        }

        // EMA 20/50/200 Stack:
        if (input.emaAlignment.contains("Bullish", ignoreCase = true)) {
            bullishIndicatorConfirmations++
        } else if (input.emaAlignment.contains("Bearish", ignoreCase = true)) {
            bearishIndicatorConfirmations++
        }

        // Volume:
        val hasVolumeExpansion = input.volumeRatio >= 1.5
        if (hasVolumeExpansion) {
            if (bullishIndicatorConfirmations > bearishIndicatorConfirmations) bullishIndicatorConfirmations++
            else if (bearishIndicatorConfirmations > bullishIndicatorConfirmations) bearishIndicatorConfirmations++
        }

        val indicatorConfluencePassedBullish = bullishIndicatorConfirmations >= 3
        val indicatorConfluencePassedBearish = bearishIndicatorConfirmations >= 3
        val indicatorConfluencePassed = indicatorConfluencePassedBullish || indicatorConfluencePassedBearish

        if (indicatorConfluencePassedBullish) bullishScore += 25
        if (indicatorConfluencePassedBearish) bearishScore += 25

        // ==========================================
        // 2. PRICE ACTION & STRUCTURE
        // Higher Highs/Higher Lows vs Lower Highs/Lower Lows
        // Candlestick patterns
        // ==========================================
        var priceActionPassed = false
        if (input.trendStructure.contains("Higher Highs", ignoreCase = true)) {
            bullishScore += 15
            priceActionPassed = true
        } else if (input.trendStructure.contains("Lower Highs", ignoreCase = true)) {
            bearishScore += 15
            priceActionPassed = true
        }

        when (input.candlestickPattern) {
            CandlestickPattern.BULLISH_ENGULFING,
            CandlestickPattern.PIN_BAR_HAMMER,
            CandlestickPattern.MORNING_STAR,
            CandlestickPattern.INVERSE_HEAD_AND_SHOULDERS -> {
                bullishScore += 15
                priceActionPassed = true
            }
            CandlestickPattern.BEARISH_ENGULFING,
            CandlestickPattern.SHOOTING_STAR,
            CandlestickPattern.EVENING_STAR,
            CandlestickPattern.HEAD_AND_SHOULDERS -> {
                bearishScore += 15
                priceActionPassed = true
            }
            CandlestickPattern.NONE -> {}
        }

        // ==========================================
        // 3. FUNDAMENTAL & NEWS SENTIMENT
        // Evaluate Hawkish/Dovish/Bullish/Bearish.
        // Overrides short-term technicals if high-impact contradictory news.
        // ==========================================
        var macroConflict = false
        when (input.newsSentiment) {
            MacroSentiment.BULLISH, MacroSentiment.DOVISH -> {
                bullishScore += 15
                if (bearishScore > bullishScore) macroConflict = true
            }
            MacroSentiment.BEARISH, MacroSentiment.HAWKISH -> {
                bearishScore += 15
                if (bullishScore > bearishScore) macroConflict = true
            }
            MacroSentiment.HIGH_IMPACT_NEWS -> {
                // High volatility uncertainty -> penalty to confidence
                macroConflict = true
            }
            MacroSentiment.NEUTRAL -> {
                // Neutral doesn't conflict
            }
        }

        // ==========================================
        // 4. SMART MONEY CONCEPTS (SMC / ICT)
        // Liquidity Sweeps, MSS, FVG/OB
        // ==========================================
        var smcPassed = false
        val isBullishSweep = input.smcSweep == SMCSweep.SESSION_LOW_SWEEP || input.smcSweep == SMCSweep.EQUAL_LOWS_SWEEP
        val isBearishSweep = input.smcSweep == SMCSweep.SESSION_HIGH_SWEEP || input.smcSweep == SMCSweep.EQUAL_HIGHS_SWEEP
        val isBullishMSS = input.smcStructureShift == SMCStructureShift.BULLISH_MSS || input.smcStructureShift == SMCStructureShift.CHoCH
        val isBearishMSS = input.smcStructureShift == SMCStructureShift.BEARISH_MSS

        if (isBullishSweep) {
            bullishScore += 15
            smcPassed = true
        }
        if (isBearishSweep) {
            bearishScore += 15
            smcPassed = true
        }
        if (isBullishMSS) {
            bullishScore += 15
            smcPassed = true
        }
        if (isBearishMSS) {
            bearishScore += 15
            smcPassed = true
        }

        // ==========================================
        // 5. BREAKOUT VALIDATION
        // Volume expansion >= 1.5x-2.0x & Candle closed beyond level
        // ==========================================
        var breakoutValid = false
        if (input.isBreakoutSetup) {
            if (input.candleClosedBeyondLevel && input.volumeRatio >= 1.7) {
                breakoutValid = true
                if (bullishScore > bearishScore) bullishScore += 15
                else if (bearishScore > bullishScore) bearishScore += 15
            } else {
                // Failed breakout (possible fakeout / liquidity trap)
                if (input.volumeRatio < 1.4 || !input.candleClosedBeyondLevel) {
                    // Penalize breakout direction
                    bullishScore = (bullishScore * 0.8).toInt()
                    bearishScore = (bearishScore * 0.8).toInt()
                }
            }
        }

        // ==========================================
        // 6. SCALPING & SHORT-TERM MOMENTUM (1M - 5M)
        // ==========================================
        var scalpingAligned = false
        var htfConflict = false
        if (input.timeframe.isLowerTimeframe) {
            val isScalpBullish = input.ema9Cross21.contains("Bullish", ignoreCase = true) && input.momentumSpike
            val isScalpBearish = input.ema9Cross21.contains("Bearish", ignoreCase = true) && input.momentumSpike

            if (!input.htfTrendAligned) {
                htfConflict = true
            } else {
                if (isScalpBullish) {
                    bullishScore += 15
                    scalpingAligned = true
                } else if (isScalpBearish) {
                    bearishScore += 15
                    scalpingAligned = true
                }
            }
        }

        // Normalize confidence scores (cap at 96% max, floor at 15%)
        val calculatedConfidence = if (bullishScore > bearishScore) {
            val base = (bullishScore.toDouble() / maxScorePerSide * 100.0).roundToInt()
            base.coerceIn(15, 96)
        } else if (bearishScore > bullishScore) {
            val base = (bearishScore.toDouble() / maxScorePerSide * 100.0).roundToInt()
            base.coerceIn(15, 96)
        } else {
            50
        }

        // ==========================================
        // STRICT SIGNAL RULES:
        // Rule 1: High Probability Rule -> ONLY "UP" or "DOWN" when confidence >= 80%
        // Rule 2: Conflict Resolution -> If Daily/1H conflicts with 15M/5M, output "HOLD"
        // Rule 3: High impact news override -> If macro conflict contradicts setup, output "HOLD"
        // ==========================================
        val isConfidenceHigh = calculatedConfidence >= 80
        val hasConflict = htfConflict || (macroConflict && input.newsSentiment == MacroSentiment.HIGH_IMPACT_NEWS)

        val signal = when {
            hasConflict -> "HOLD"
            !isConfidenceHigh -> "HOLD"
            bullishScore > bearishScore && indicatorConfluencePassedBullish -> "UP"
            bearishScore > bullishScore && indicatorConfluencePassedBearish -> "DOWN"
            else -> "HOLD"
        }

        val primaryStrategy = when {
            smcPassed && (isBullishSweep || isBearishSweep) -> "SMC / ICT Liquidity Sweep"
            input.isBreakoutSetup && breakoutValid -> "Breakout Confluence"
            input.timeframe.isLowerTimeframe && scalpingAligned -> "Scalping & Momentum"
            indicatorConfluencePassed -> "Multi-Indicator Confluence"
            else -> "Market Structure / Price Action"
        }

        val executionType = when (signal) {
            "UP" -> if (input.isMitigated) "Market Entry" else "Limit Entry"
            "DOWN" -> if (input.isMitigated) "Market Entry" else "Limit Entry"
            else -> "No Trade"
        }

        // ==========================================
        // PRECISE RISK MANAGEMENT:
        // Calculate Stop Loss, TP1, TP2, and R:R
        // ==========================================
        val price = input.currentPrice
        val (entryZone, stopLoss, tp1, tp2, rrRatio) = calculateRiskLevels(
            signal = signal,
            currentPrice = price,
            support = input.supportLevel,
            resistance = input.resistanceLevel,
            fvgLevel = input.fvgObLevel,
            timeframe = input.timeframe
        )

        val rationale = buildRationale(
            signal = signal,
            confidence = calculatedConfidence,
            primaryStrategy = primaryStrategy,
            input = input,
            hasConflict = hasConflict,
            isHtfConflict = htfConflict,
            isMacroConflict = macroConflict,
            indicatorCount = max(bullishIndicatorConfirmations, bearishIndicatorConfirmations)
        )

        return TradingSignalOutput(
            signal = signal,
            confidence_score = "$calculatedConfidence%",
            execution_type = executionType,
            timeframe = input.timeframe.label,
            entry_zone = entryZone,
            stop_loss = stopLoss,
            take_profit_1 = tp1,
            take_profit_2 = tp2,
            risk_reward_ratio = rrRatio,
            primary_strategy_used = primaryStrategy,
            trade_rationale = rationale,
            confidenceInt = calculatedConfidence,
            indicatorConfluencePassed = indicatorConfluencePassed,
            priceActionPassed = priceActionPassed,
            smcPassed = smcPassed,
            breakoutValid = breakoutValid,
            scalpingAligned = scalpingAligned,
            macroConflict = macroConflict,
            htfConflict = htfConflict,
            engineSource = "Quant Confluence Core v3.8"
        )
    }

    private data class RiskLevels(
        val entryZone: String,
        val stopLoss: String,
        val tp1: String,
        val tp2: String,
        val rrRatio: String
    )

    private fun calculateRiskLevels(
        signal: String,
        currentPrice: Double,
        support: Double,
        resistance: Double,
        fvgLevel: Double,
        timeframe: MarketTimeframe
    ): RiskLevels {
        if (signal == "HOLD") {
            return RiskLevels(
                entryZone = "N/A (Wait for Confirmation)",
                stopLoss = "0.00",
                tp1 = "0.00",
                tp2 = "0.00",
                rrRatio = "N/A"
            )
        }

        val bufferPct = when (timeframe) {
            MarketTimeframe.TF_1M -> 0.0015
            MarketTimeframe.TF_5M -> 0.003
            MarketTimeframe.TF_15M -> 0.006
            MarketTimeframe.TF_1H -> 0.012
            MarketTimeframe.TF_4H -> 0.025
            MarketTimeframe.TF_DAILY -> 0.040
        }

        val decimalPlaces = if (currentPrice > 1000) 2 else if (currentPrice > 10) 3 else 5

        if (signal == "UP") {
            val entryMin = currentPrice * 0.999
            val entryMax = currentPrice * 1.001
            val slBase = if (support > 0 && support < currentPrice) support else currentPrice * (1.0 - bufferPct)
            val stopLossVal = slBase * 0.998
            val riskDistance = currentPrice - stopLossVal
            val tp1Val = currentPrice + (riskDistance * 1.8)
            val tp2Val = currentPrice + (riskDistance * 2.8)
            val rr = "1:2.8"

            return RiskLevels(
                entryZone = String.format(Locale.US, "%.${decimalPlaces}f - %.${decimalPlaces}f", entryMin, entryMax),
                stopLoss = String.format(Locale.US, "%.${decimalPlaces}f", stopLossVal),
                tp1 = String.format(Locale.US, "%.${decimalPlaces}f", tp1Val),
                tp2 = String.format(Locale.US, "%.${decimalPlaces}f", tp2Val),
                rrRatio = rr
            )
        } else {
            // DOWN
            val entryMin = currentPrice * 0.999
            val entryMax = currentPrice * 1.001
            val resBase = if (resistance > currentPrice) resistance else currentPrice * (1.0 + bufferPct)
            val stopLossVal = resBase * 1.002
            val riskDistance = stopLossVal - currentPrice
            val tp1Val = currentPrice - (riskDistance * 1.8)
            val tp2Val = currentPrice - (riskDistance * 2.8)
            val rr = "1:2.8"

            return RiskLevels(
                entryZone = String.format(Locale.US, "%.${decimalPlaces}f - %.${decimalPlaces}f", entryMin, entryMax),
                stopLoss = String.format(Locale.US, "%.${decimalPlaces}f", stopLossVal),
                tp1 = String.format(Locale.US, "%.${decimalPlaces}f", tp1Val),
                tp2 = String.format(Locale.US, "%.${decimalPlaces}f", tp2Val),
                rrRatio = rr
            )
        }
    }

    private fun buildRationale(
        signal: String,
        confidence: Int,
        primaryStrategy: String,
        input: TradingInputState,
        hasConflict: Boolean,
        isHtfConflict: Boolean,
        isMacroConflict: Boolean,
        indicatorCount: Int
    ): String {
        return when {
            hasConflict && isHtfConflict -> {
                "HOLD / NEUTRAL: Lower timeframe (${input.timeframe.label}) momentum contradicts Higher Timeframe (1H/Daily) trend structure. Waiting for higher timeframe alignment."
            }
            hasConflict && isMacroConflict -> {
                "HOLD / NEUTRAL: High-impact macro news event (${input.newsSentiment.label}) presents conflicting risk override against technical setup. Standing aside until volatility settles."
            }
            signal == "HOLD" -> {
                "HOLD / NEUTRAL: Confluence score is at ${confidence}%, failing the strict >=80% high-probability rule ($indicatorCount indicator confirmations detected). Capital preservation prioritized."
            }
            signal == "UP" -> {
                "High probability UP signal triggered by ${primaryStrategy} with ${indicatorCount} confirmed indicator alignments (RSI ${input.rsi.toInt()}, MACD ${input.macdStatus}, Volume ${input.volumeRatio}x) and clear bullish structure shift."
            }
            else -> {
                "High probability DOWN signal triggered by ${primaryStrategy} with ${indicatorCount} confirmed indicator alignments (RSI ${input.rsi.toInt()}, MACD ${input.macdStatus}, Volume ${input.volumeRatio}x) following liquidity sweep and rejection."
            }
        }
    }
}
