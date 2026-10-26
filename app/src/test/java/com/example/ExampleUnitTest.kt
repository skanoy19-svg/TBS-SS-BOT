package com.example

import com.example.data.model.CandlestickPattern
import com.example.data.model.MacroSentiment
import com.example.data.model.QuotexSignalType
import com.example.data.model.SMCSweep
import com.example.data.model.SMCStructureShift
import com.example.data.model.TradingInputState
import com.example.data.presets.TradingPresets
import com.example.engine.LocalQuantEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBullishSMCSetupYieldsHighConfidenceUp() {
        val btcPreset = TradingPresets.presets.first { it.id == "btc_bullish_smc" }
        val output = LocalQuantEngine.evaluate(btcPreset.state)

        assertEquals("UP", output.signal)
        assertTrue("Confidence should be >= 80%", output.confidenceInt >= 80)
        assertEquals("Market Entry", output.execution_type)
        assertNotEquals("0.00", output.stop_loss)
        assertNotEquals("0.00", output.take_profit_1)
        assertNotEquals("0.00", output.take_profit_2)
        assertTrue(output.indicatorConfluencePassed)
        assertTrue(output.smcPassed)
    }

    @Test
    fun testBearishRejectionYieldsDown() {
        val goldPreset = TradingPresets.presets.first { it.id == "gold_bearish_rejection" }
        val output = LocalQuantEngine.evaluate(goldPreset.state)

        assertEquals("DOWN", output.signal)
        assertTrue("Confidence should be >= 80%", output.confidenceInt >= 80)
        assertNotEquals("0.00", output.stop_loss)
        assertTrue(output.indicatorConfluencePassed)
    }

    @Test
    fun testConflictResolutionForcesHold() {
        val conflictPreset = TradingPresets.presets.first { it.id == "macro_conflict_hold" }
        val output = LocalQuantEngine.evaluate(conflictPreset.state)

        assertEquals("HOLD", output.signal)
        assertEquals("No Trade", output.execution_type)
        assertTrue("Rationale should explain conflict or preservation", output.trade_rationale.contains("HOLD"))
    }

    @Test
    fun testStrict80PercentConfidenceThreshold() {
        // Setup with weak confluence (< 80%)
        val weakSetup = TradingInputState(
            asset = "ETHUSDT",
            currentPrice = 2500.0,
            rsi = 50.0, // completely neutral
            macdStatus = "Neutral",
            emaAlignment = "Neutral / Mixed",
            volumeRatio = 1.0,
            trendStructure = "Consolidation / Range",
            candlestickPattern = CandlestickPattern.NONE,
            newsSentiment = MacroSentiment.NEUTRAL,
            smcSweep = SMCSweep.NONE,
            smcStructureShift = SMCStructureShift.NONE
        )
        val output = LocalQuantEngine.evaluate(weakSetup)

        assertEquals("Weak setup must output HOLD", "HOLD", output.signal)
        assertEquals("No Trade", output.execution_type)
    }

    @Test
    fun testQuotexSignals() {
        val callSignal = QuotexSignalType.CALL
        val putSignal = QuotexSignalType.PUT
        val skipSignal = QuotexSignalType.SKIP

        assertTrue(callSignal.label.contains("CALL"))
        assertTrue(putSignal.label.contains("PUT"))
        assertTrue(skipSignal.label.contains("WAIT"))
    }
}
