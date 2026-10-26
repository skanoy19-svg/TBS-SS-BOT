package com.example.data.model

data class QuotexScreenshotAnalysis(
    val asset: String = "EUR/USD (OTC)",
    val timeframe: String = "1M (60s)",
    val signal: QuotexSignalType = QuotexSignalType.CALL,
    val confidence: Int = 94, // e.g. 94%
    val recommendedExpiry: String = "1 Minute (Next Candle)",
    val martingaleAdvice: String = "Direct Shot (High Confidence, No MTG needed)",
    val nextCandleColor: String = "GREEN (Bullish Candle 🟢)",
    val entryTiming: String = "Enter exactly at 00:00 candle opening",
    val supportLevel: String = "1.08450",
    val resistanceLevel: String = "1.08920",
    val patternDetected: String = "Bullish Pin Bar Rejection at OTC Support",
    val rsiStochasticStatus: String = "RSI: 22 (Oversold), Stoch %K cross up",
    val bollingerBandStatus: String = "Price pierced Lower Band -> Mean Reversion",
    val rationale: String = "Strong OTC support rejection with low wick exhaustion. Multiple confluence indicators favor an immediate bullish next candle.",
    val banglaRationale: String = "সাপোর্ট লেভেলে বায়ারদের স্ট্রং উইক রিজেকশন এবং আরএসআই ওভারসোল্ড (<২৫) বাউন্স নিশ্চিত হয়েছে। পরবর্তী ১ মিনিটের ক্যান্ডেল নির্দ্বিধায় গ্রিন (CALL) হবে।",
    val imageUri: String? = null,
    val isDemoSample: Boolean = false,
    val srRejectionVerified: Boolean = true,
    val wickRatioVerified: Boolean = true,
    val rsiFilterVerified: Boolean = true,
    val bollingerVerified: Boolean = true,
    val trendAligned: Boolean = true
)

enum class QuotexSignalType(
    val label: String,
    val hindiLabel: String,
    val banglaLabel: String
) {
    CALL("CALL 🟢 (UP)", "CALL / UP (ऊपर) 🟢", "CALL / UP (উপরে) 🟢"),
    PUT("PUT 🔴 (DOWN)", "PUT / DOWN (नीचे) 🔴", "PUT / DOWN (নিচে) 🔴"),
    SKIP("WAIT / SKIP ⏸️", "वेट करें / ट्रेड न लें ⏸️", "অপেক্ষা করুন / ট্রেড নিবেন না ⏸️")
}

data class QuotexPresetSS(
    val id: String,
    val title: String,
    val asset: String,
    val expectedSignal: QuotexSignalType,
    val winRate: String,
    val description: String,
    val sampleAnalysis: QuotexScreenshotAnalysis
)
