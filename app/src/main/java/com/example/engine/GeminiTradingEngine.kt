package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.TradingInputState
import com.example.data.model.TradingSignalOutput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Multimodal AI Decision Engine calling Gemini 3.5 Flash REST API with the unified trading engine prompt.
 * Falls back to LocalQuantEngine seamlessly when offline or if no API key is provided.
 */
class GeminiTradingEngine(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun generateSignal(input: TradingInputState): TradingSignalOutput = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d("GeminiEngine", "No valid Gemini API key configured in BuildConfig. Using deterministic Local Quant Engine.")
            return@withContext LocalQuantEngine.evaluate(input).copy(
                engineSource = "Quant Confluence Core (Offline/Built-in)"
            )
        }

        try {
            val systemInstruction = """
SYSTEM INSTRUCTION: UNIFIED TRADING SIGNAL GENERATOR ENGINE

Role & Objective:
You are an advanced quantitative trading strategist and multimodal AI decision engine. Your primary objective is to evaluate multi-source trading data (technical indicators, chart screenshots, market news, SMC/ICT setups, breakout levels, and scalping metrics) to generate high-probability trading signals (UP, DOWN, or HOLD/NEUTRAL).

CORE EVALUATION MODULES:
1. MULTI-INDICATOR CONFLUENCE: Check alignment across RSI, MACD, Moving Averages (EMA 20/50/200), and Volume. Require at least 3 indicator confirmations before signaling UP/DOWN.
2. PRICE ACTION & CHART ANALYSIS (Multimodal/Image): Analyze trend structure (Higher Highs/Higher Lows vs Lower Highs/Lower Lows). Identify key Support/Resistance zones and candlestick patterns.
3. FUNDAMENTAL & NEWS SENTIMENT: Evaluate high-impact news summaries for Hawkish/Dovish or Bullish/Bearish bias. Override short-term technicals if high-impact news contradicts the setup.
4. SMART MONEY CONCEPTS (SMC / ICT): Identify Liquidity Sweeps, MSS, Fair Value Gaps (FVG), and Order Blocks (OB).
5. BREAKOUT VALIDATION: Verify volume expansion (minimum 1.5x - 2x average volume). Confirm candle body close beyond key level to avoid fakeouts.
6. SCALPING & SHORT-TERM MOMENTUM: For lower timeframes (1M-5M), ensure alignment between 9/21 EMA crossover, momentum spikes, and higher timeframe trend direction.

STRICT SIGNAL RULES:
- High Probability Rule: Generate "UP" or "DOWN" ONLY when overall confidence >= 80%.
- Conflict Resolution: If Daily/1H trends conflict with 15M/5M setups, output "HOLD / NEUTRAL" (use signal: "HOLD").
- Risk Management: Every valid signal MUST include precise Stop Loss and Take Profit levels.

REQUIRED OUTPUT FORMAT (Return strictly ONLY raw JSON, no extra markdown or surrounding commentary):
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
""".trimIndent()

            val userPromptText = """
[ASSET]: ${input.asset} (Current Reference Price: ${input.currentPrice})
[TIMEFRAME]: ${input.timeframe.label}
[INDICATORS]: RSI: ${input.rsi} | MACD: ${input.macdStatus} | EMAs: ${input.emaAlignment} | Volume: ${input.volumeRatio}x avg
[SMC DATA]: Sweep: ${input.smcSweep.label} | FVG/OB Level: ${input.fvgObLevel} | MSS: ${input.smcStructureShift.label}
[BREAKOUT LEVEL]: Resistance: ${input.resistanceLevel} | Support: ${input.supportLevel} | Closed Beyond: ${input.candleClosedBeyondLevel}
[NEWS SENTIMENT]: ${input.newsSentiment.label} (${input.newsHeadline})
[LOWER TF SCALPING]: 9/21 EMA: ${input.ema9Cross21} | Momentum Spike: ${input.momentumSpike} | HTF Aligned: ${input.htfTrendAligned}
[ATTACHMENT]: ${if (input.chartImageUri != null) "Chart image provided below" else "None"}

Please evaluate all 6 modules and provide the strict JSON signal decision.
""".trimIndent()

            // Construct Gemini REST JSON payload
            val root = JSONObject()

            // System instruction
            val sysObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", systemInstruction))
            sysObj.put("parts", sysParts)
            root.put("systemInstruction", sysObj)

            // User parts
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            partsArray.put(JSONObject().put("text", userPromptText))

            // Multimodal image support if user attached an image
            if (input.chartImageUri != null) {
                val base64Image = encodeImageToBase64(input.chartImageUri)
                if (base64Image != null) {
                    val inlineData = JSONObject().apply {
                        put("mimeType", "image/jpeg")
                        put("data", base64Image)
                    }
                    partsArray.put(JSONObject().put("inlineData", inlineData))
                }
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            root.put("contents", contentsArray)

            // Generation config with JSON response
            val genConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.2) // Low temperature for deterministic quantitative accuracy
            }
            root.put("generationConfig", genConfig)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val body = root.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.w("GeminiEngine", "Gemini API error code ${response.code}: $responseString. Falling back to local quant engine.")
                return@withContext LocalQuantEngine.evaluate(input).copy(
                    engineSource = "Quant Confluence Core (Fallback)"
                )
            }

            val parsedOutput = parseGeminiResponse(responseString, input)
            return@withContext parsedOutput
        } catch (e: Exception) {
            Log.e("GeminiEngine", "Exception calling Gemini API: ${e.message}. Falling back to local quant engine.", e)
            return@withContext LocalQuantEngine.evaluate(input).copy(
                engineSource = "Quant Confluence Core (Offline Mode)"
            )
        }
    }

    private fun parseGeminiResponse(responseJson: String, input: TradingInputState): TradingSignalOutput {
        try {
            val root = JSONObject(responseJson)
            val candidates = root.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: ""

            val cleanJson = cleanJsonString(text)
            val signalObj = JSONObject(cleanJson)

            val rawSignal = signalObj.optString("signal", "HOLD").trim().uppercase()
            val normalizedSignal = when {
                rawSignal.contains("UP") -> "UP"
                rawSignal.contains("DOWN") -> "DOWN"
                else -> "HOLD"
            }

            val confidenceStr = signalObj.optString("confidence_score", "75%")
            val confInt = confidenceStr.replace("%", "").trim().toIntOrNull() ?: 75

            // Strict enforcement of Rule 1: High probability rule >= 80%
            val finalSignal = if (confInt < 80 && (normalizedSignal == "UP" || normalizedSignal == "DOWN")) {
                "HOLD"
            } else {
                normalizedSignal
            }

            val execType = signalObj.optString("execution_type", if (finalSignal == "HOLD") "No Trade" else "Market Entry")
            val tf = signalObj.optString("timeframe", input.timeframe.label)
            val entryZone = signalObj.optString("entry_zone", "${input.currentPrice}")
            val sl = signalObj.optString("stop_loss", "0.00")
            val tp1 = signalObj.optString("take_profit_1", "0.00")
            val tp2 = signalObj.optString("take_profit_2", "0.00")
            val rr = signalObj.optString("risk_reward_ratio", "1:2.5")
            val strategy = signalObj.optString("primary_strategy_used", "SMC / Multi-Indicator Confluence")
            val rationale = signalObj.optString("trade_rationale", "Confluence alignment analyzed across all 6 modules.")

            // Local telemetry checks to populate module indicators
            val localEval = LocalQuantEngine.evaluate(input)

            return TradingSignalOutput(
                signal = finalSignal,
                confidence_score = "$confInt%",
                execution_type = execType,
                timeframe = tf,
                entry_zone = entryZone,
                stop_loss = sl,
                take_profit_1 = tp1,
                take_profit_2 = tp2,
                risk_reward_ratio = rr,
                primary_strategy_used = strategy,
                trade_rationale = rationale,
                confidenceInt = confInt,
                indicatorConfluencePassed = localEval.indicatorConfluencePassed,
                priceActionPassed = localEval.priceActionPassed,
                smcPassed = localEval.smcPassed,
                breakoutValid = localEval.breakoutValid,
                scalpingAligned = localEval.scalpingAligned,
                macroConflict = localEval.macroConflict,
                htfConflict = localEval.htfConflict,
                engineSource = "Gemini 3.5 Multimodal AI"
            )
        } catch (e: Exception) {
            Log.e("GeminiEngine", "Failed to parse Gemini response: ${e.message}", e)
            return LocalQuantEngine.evaluate(input).copy(
                engineSource = "Quant Confluence Core (Parsed Fallback)"
            )
        }
    }

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json")
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```")
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```")
        }
        return str.trim()
    }

    private fun encodeImageToBase64(uriString: String): String? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (bitmap == null) return null

            // Scale down if huge to conserve memory & bandwidth
            val maxDim = 1024
            val scaledBitmap = if (bitmap.width > maxDim || bitmap.height > maxDim) {
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val targetW = if (ratio > 1f) maxDim else (maxDim * ratio).toInt()
                val targetH = if (ratio > 1f) (maxDim / ratio).toInt() else maxDim
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            } else {
                bitmap
            }

            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos)
            val bytes = baos.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("GeminiEngine", "Error encoding image: ${e.message}", e)
            null
        }
    }
}
