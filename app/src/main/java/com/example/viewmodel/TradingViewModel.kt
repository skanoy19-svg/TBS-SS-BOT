package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SignalEntity
import com.example.data.model.QuotexPresetSS
import com.example.data.model.QuotexScreenshotAnalysis
import com.example.data.model.TradingInputState
import com.example.data.model.TradingSignalOutput
import com.example.data.presets.PresetScenario
import com.example.data.presets.TradingPresets
import com.example.engine.GeminiTradingEngine
import com.example.engine.LocalQuantEngine
import com.example.engine.QuotexSSEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TerminalTab(val label: String) {
    QUOTEX_SCAN("Quotex SS"),
    TERMINAL("Confluence"),
    JOURNAL("Signal Log"),
    MATRIX_DOCS("Engine Rules")
}

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val signalDao = db.signalDao()
    private val geminiEngine = GeminiTradingEngine(application)
    private val quotexSSEngine = QuotexSSEngine(application)

    // Quotex SS Specific State
    val quotexPresets: List<QuotexPresetSS> = quotexSSEngine.sampleQuotexPresets

    private val _quotexAnalysis = MutableStateFlow<QuotexScreenshotAnalysis?>(quotexPresets.first().sampleAnalysis)
    val quotexAnalysis: StateFlow<QuotexScreenshotAnalysis?> = _quotexAnalysis.asStateFlow()

    private val _isQuotexScanning = MutableStateFlow(false)
    val isQuotexScanning: StateFlow<Boolean> = _isQuotexScanning.asStateFlow()

    // Confluence Terminal State
    private val _inputState = MutableStateFlow(TradingPresets.presets.first().state)
    val inputState: StateFlow<TradingInputState> = _inputState.asStateFlow()

    private val _signalOutput = MutableStateFlow<TradingSignalOutput?>(null)
    val signalOutput: StateFlow<TradingSignalOutput?> = _signalOutput.asStateFlow()

    private val _isEvaluating = MutableStateFlow(false)
    val isEvaluating: StateFlow<Boolean> = _isEvaluating.asStateFlow()

    private val _activeTab = MutableStateFlow(TerminalTab.QUOTEX_SCAN)
    val activeTab: StateFlow<TerminalTab> = _activeTab.asStateFlow()

    private val _showTemplateDialog = MutableStateFlow(false)
    val showTemplateDialog: StateFlow<Boolean> = _showTemplateDialog.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val historySignals: StateFlow<List<SignalEntity>> = signalDao.getAllSignals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initial evaluation
        evaluateCurrentInput()
    }

    fun selectTab(tab: TerminalTab) {
        _activeTab.value = tab
    }

    fun setShowTemplateDialog(show: Boolean) {
        _showTemplateDialog.value = show
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // Quotex Actions
    fun scanQuotexScreenshot(imageUri: String?, asset: String, timeframe: String) {
        viewModelScope.launch {
            _isQuotexScanning.value = true
            try {
                val result = quotexSSEngine.analyzeScreenshot(imageUri, asset, timeframe)
                _quotexAnalysis.value = result
                _toastMessage.value = "Quotex SS Signal: ${result.signal.label} (${result.confidence}% Win Rate)"
            } catch (e: Exception) {
                _toastMessage.value = "Scan error: ${e.message}"
            } finally {
                _isQuotexScanning.value = false
            }
        }
    }

    fun loadQuotexPreset(preset: QuotexPresetSS) {
        _quotexAnalysis.value = preset.sampleAnalysis
        _toastMessage.value = "Loaded ${preset.title}"
    }

    fun saveQuotexSignalLog(analysis: QuotexScreenshotAnalysis) {
        viewModelScope.launch {
            val entity = SignalEntity(
                asset = analysis.asset,
                timeframe = analysis.timeframe,
                signal = analysis.signal.name,
                confidenceScore = "${analysis.confidence}%",
                executionType = analysis.entryTiming,
                entryZone = analysis.supportLevel,
                stopLoss = "Binary Option (Fixed Expiry)",
                takeProfit1 = "Payout 85%-98%",
                takeProfit2 = analysis.martingaleAdvice,
                riskRewardRatio = "Binary 1:0.85+",
                primaryStrategy = analysis.patternDetected,
                tradeRationale = analysis.rationale,
                jsonPayload = "Quotex SS Signal: ${analysis.signal.label} | ${analysis.recommendedExpiry} | ${analysis.martingaleAdvice}"
            )
            signalDao.insertSignal(entity)
            _toastMessage.value = "Quotex Signal logged to Tracker!"
        }
    }

    // Confluence Terminal Actions
    fun updateInput(reducer: (TradingInputState) -> TradingInputState) {
        _inputState.value = reducer(_inputState.value)
    }

    fun loadPreset(preset: PresetScenario) {
        _inputState.value = preset.state
        evaluateCurrentInput()
    }

    fun setChartImageUri(uri: String?) {
        _inputState.value = _inputState.value.copy(chartImageUri = uri)
    }

    fun evaluateCurrentInput() {
        viewModelScope.launch {
            _isEvaluating.value = true
            try {
                val output = geminiEngine.generateSignal(_inputState.value)
                _signalOutput.value = output
            } catch (e: Exception) {
                val fallback = LocalQuantEngine.evaluate(_inputState.value)
                _signalOutput.value = fallback
            } finally {
                _isEvaluating.value = false
            }
        }
    }

    fun saveCurrentSignalToJournal() {
        val current = _signalOutput.value ?: return
        viewModelScope.launch {
            val entity = SignalEntity(
                asset = _inputState.value.asset,
                timeframe = current.timeframe,
                signal = current.signal,
                confidenceScore = current.confidence_score,
                executionType = current.execution_type,
                entryZone = current.entry_zone,
                stopLoss = current.stop_loss,
                takeProfit1 = current.take_profit_1,
                takeProfit2 = current.take_profit_2,
                riskRewardRatio = current.risk_reward_ratio,
                primaryStrategy = current.primary_strategy_used,
                tradeRationale = current.trade_rationale,
                jsonPayload = """
{
  "signal": "${current.signal}",
  "confidence_score": "${current.confidence_score}",
  "execution_type": "${current.execution_type}",
  "timeframe": "${current.timeframe}",
  "entry_zone": "${current.entry_zone}",
  "stop_loss": "${current.stop_loss}",
  "take_profit_1": "${current.take_profit_1}",
  "take_profit_2": "${current.take_profit_2}",
  "risk_reward_ratio": "${current.risk_reward_ratio}",
  "primary_strategy_used": "${current.primary_strategy_used}",
  "trade_rationale": "${current.trade_rationale}"
}
                """.trimIndent()
            )
            signalDao.insertSignal(entity)
            _toastMessage.value = "Signal saved to Trading Journal!"
        }
    }

    fun deleteSignal(id: Long) {
        viewModelScope.launch {
            signalDao.deleteSignal(id)
            _toastMessage.value = "Signal removed from Journal."
        }
    }

    fun updateSignalOutcome(signal: SignalEntity, outcome: String) {
        viewModelScope.launch {
            signalDao.updateSignal(signal.copy(outcomeStatus = outcome))
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            signalDao.clearAllSignals()
            _toastMessage.value = "Journal cleared."
        }
    }
}
