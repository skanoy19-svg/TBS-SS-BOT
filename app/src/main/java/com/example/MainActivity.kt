package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.presets.TradingPresets
import com.example.ui.components.InputTemplateDialog
import com.example.ui.screens.EngineRulesScreen
import com.example.ui.screens.QuotexScanScreen
import com.example.ui.screens.SignalGeneratorScreen
import com.example.ui.screens.SignalJournalScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.viewmodel.TerminalTab
import com.example.viewmodel.TradingViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TradingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContent(viewModel: TradingViewModel) {
    val context = LocalContext.current
    val activeTab by viewModel.activeTab.collectAsState()
    val quotexAnalysis by viewModel.quotexAnalysis.collectAsState()
    val isQuotexScanning by viewModel.isQuotexScanning.collectAsState()
    val inputState by viewModel.inputState.collectAsState()
    val signalOutput by viewModel.signalOutput.collectAsState()
    val isEvaluating by viewModel.isEvaluating.collectAsState()
    val showTemplateDialog by viewModel.showTemplateDialog.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val historySignals by viewModel.historySignals.collectAsState()

    // Handle Toast events
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // BackHandler: Return to Quotex SS tab if in another tab
    if (activeTab != TerminalTab.QUOTEX_SCAN) {
        BackHandler {
            viewModel.selectTab(TerminalTab.QUOTEX_SCAN)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .border(0.5.dp, SurfaceBorder),
                containerColor = SurfaceDark,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == TerminalTab.QUOTEX_SCAN,
                    onClick = { viewModel.selectTab(TerminalTab.QUOTEX_SCAN) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Quotex SS",
                            tint = if (activeTab == TerminalTab.QUOTEX_SCAN) NeonCyan else TextMuted
                        )
                    },
                    label = {
                        Text(
                            text = "Quotex SS",
                            fontSize = 11.sp,
                            fontWeight = if (activeTab == TerminalTab.QUOTEX_SCAN) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = NeonCyan,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0x3300E5FF)
                    ),
                    modifier = Modifier.testTag("nav_tab_quotex")
                )

                NavigationBarItem(
                    selected = activeTab == TerminalTab.TERMINAL,
                    onClick = { viewModel.selectTab(TerminalTab.TERMINAL) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "Confluence",
                            tint = if (activeTab == TerminalTab.TERMINAL) NeonCyan else TextMuted
                        )
                    },
                    label = {
                        Text(
                            text = "Confluence",
                            fontSize = 11.sp,
                            fontWeight = if (activeTab == TerminalTab.TERMINAL) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = NeonCyan,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0x3300E5FF)
                    ),
                    modifier = Modifier.testTag("nav_tab_terminal")
                )

                NavigationBarItem(
                    selected = activeTab == TerminalTab.JOURNAL,
                    onClick = { viewModel.selectTab(TerminalTab.JOURNAL) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Journal",
                            tint = if (activeTab == TerminalTab.JOURNAL) NeonCyan else TextMuted
                        )
                    },
                    label = {
                        Text(
                            text = "Signals (${historySignals.size})",
                            fontSize = 11.sp,
                            fontWeight = if (activeTab == TerminalTab.JOURNAL) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = NeonCyan,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0x3300E5FF)
                    ),
                    modifier = Modifier.testTag("nav_tab_journal")
                )

                NavigationBarItem(
                    selected = activeTab == TerminalTab.MATRIX_DOCS,
                    onClick = { viewModel.selectTab(TerminalTab.MATRIX_DOCS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Rules",
                            tint = if (activeTab == TerminalTab.MATRIX_DOCS) NeonCyan else TextMuted
                        )
                    },
                    label = {
                        Text(
                            text = "Rules & MTG",
                            fontSize = 11.sp,
                            fontWeight = if (activeTab == TerminalTab.MATRIX_DOCS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedTextColor = NeonCyan,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color(0x3300E5FF)
                    ),
                    modifier = Modifier.testTag("nav_tab_rules")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                TerminalTab.QUOTEX_SCAN -> {
                    QuotexScanScreen(
                        currentAnalysis = quotexAnalysis,
                        isScanning = isQuotexScanning,
                        presets = viewModel.quotexPresets,
                        onScanScreenshot = { uri, asset, tf ->
                            viewModel.scanQuotexScreenshot(uri, asset, tf)
                        },
                        onLoadPreset = { preset ->
                            viewModel.loadQuotexPreset(preset)
                        },
                        onSaveSignalLog = { analysis ->
                            viewModel.saveQuotexSignalLog(analysis)
                        }
                    )
                }
                TerminalTab.TERMINAL -> {
                    SignalGeneratorScreen(
                        inputState = inputState,
                        signalOutput = signalOutput,
                        isEvaluating = isEvaluating,
                        onUpdateInput = { viewModel.updateInput(it) },
                        onLoadPreset = { viewModel.loadPreset(it) },
                        onEvaluate = { viewModel.evaluateCurrentInput() },
                        onSaveToJournal = { viewModel.saveCurrentSignalToJournal() },
                        onOpenTemplateDialog = { viewModel.setShowTemplateDialog(true) },
                        onSetChartImageUri = { viewModel.setChartImageUri(it) }
                    )
                }
                TerminalTab.JOURNAL -> {
                    SignalJournalScreen(
                        signals = historySignals,
                        onDeleteSignal = { viewModel.deleteSignal(it) },
                        onUpdateOutcome = { signal, outcome -> viewModel.updateSignalOutcome(signal, outcome) },
                        onClearAll = { viewModel.clearAllHistory() }
                    )
                }
                TerminalTab.MATRIX_DOCS -> {
                    EngineRulesScreen()
                }
            }

            // Input Template Dialog
            if (showTemplateDialog) {
                InputTemplateDialog(
                    initialText = TradingPresets.formatToInputTemplate(inputState),
                    onDismiss = { viewModel.setShowTemplateDialog(false) },
                    onApplyTemplate = { parsedState ->
                        viewModel.updateInput { parsedState }
                        viewModel.evaluateCurrentInput()
                    }
                )
            }
        }
    }
}
