package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.model.CandlestickPattern
import com.example.data.model.MacroSentiment
import com.example.data.model.MarketTimeframe
import com.example.data.model.SMCSweep
import com.example.data.model.SMCStructureShift
import com.example.data.model.TradingInputState
import com.example.data.model.TradingSignalOutput
import com.example.data.presets.PresetScenario
import com.example.data.presets.TradingPresets
import com.example.ui.components.CandlestickPreview
import com.example.ui.components.HeroBannerHeader
import com.example.ui.components.SignalResultCard
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SignalGold
import com.example.ui.theme.SignalGreen
import com.example.ui.theme.SignalRed
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceBorderHighlight
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun SignalGeneratorScreen(
    inputState: TradingInputState,
    signalOutput: TradingSignalOutput?,
    isEvaluating: Boolean,
    onUpdateInput: ((TradingInputState) -> TradingInputState) -> Unit,
    onLoadPreset: (PresetScenario) -> Unit,
    onEvaluate: () -> Unit,
    onSaveToJournal: () -> Unit,
    onOpenTemplateDialog: () -> Unit,
    onSetChartImageUri: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Android zero-permission Photo Picker for chart screenshots
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        onSetChartImageUri(uri?.toString())
    }

    var showIndicatorsSection by remember { mutableStateOf(true) }
    var showSMCSection by remember { mutableStateOf(true) }
    var showPriceActionSection by remember { mutableStateOf(false) }
    var showNewsSection by remember { mutableStateOf(false) }
    var showScalpBreakoutSection by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Hero Terminal Header
        HeroBannerHeader(isAIEngineActive = true)

        Spacer(modifier = Modifier.height(14.dp))

        // Preset Scenarios Carousel
        Text(
            text = "QUICK CONFLUENCE PRESETS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TradingPresets.presets.forEach { preset ->
                val isSelected = inputState.asset == preset.state.asset && inputState.timeframe == preset.state.timeframe
                Box(
                    modifier = Modifier
                        .background(if (isSelected) Color(0xFF1E2D48) else SurfaceDark, RoundedCornerShape(10.dp))
                        .border(
                            1.dp,
                            if (isSelected) NeonCyan else SurfaceBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onLoadPreset(preset) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = preset.title,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = preset.expectedOutcome,
                            color = if (preset.expectedOutcome.contains("UP")) SignalGreen else if (preset.expectedOutcome.contains("DOWN")) SignalRed else SignalGold,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Asset, Price, & Timeframe Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputState.asset,
                onValueChange = { newAsset ->
                    onUpdateInput { it.copy(asset = newAsset.uppercase()) }
                },
                label = { Text("Asset Pair", fontSize = 11.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("asset_input_field"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            OutlinedTextField(
                value = if (inputState.currentPrice > 0) inputState.currentPrice.toString() else "",
                onValueChange = { str ->
                    val p = str.toDoubleOrNull() ?: 0.0
                    onUpdateInput { it.copy(currentPrice = p) }
                },
                label = { Text("Price", fontSize = 11.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("price_input_field"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            // Input Template Dialog Button
            IconButton(
                onClick = onOpenTemplateDialog,
                modifier = Modifier
                    .size(48.dp)
                    .background(SurfaceElevated, RoundedCornerShape(10.dp))
                    .border(1.dp, SurfaceBorderHighlight, RoundedCornerShape(10.dp))
                    .testTag("open_template_button")
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = "Paste Input Template",
                    tint = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Timeframe selector pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MarketTimeframe.entries.forEach { tf ->
                val selected = inputState.timeframe == tf
                FilterChip(
                    selected = selected,
                    onClick = { onUpdateInput { it.copy(timeframe = tf) } },
                    label = { Text(tf.label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan,
                        selectedLabelColor = Color(0xFF001F26),
                        containerColor = SurfaceDark,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = SurfaceBorder,
                        selectedBorderColor = NeonCyan
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interactive Candlestick Canvas Preview
        CandlestickPreview(input = inputState, signalOutput = signalOutput)

        Spacer(modifier = Modifier.height(14.dp))

        // Chart Screenshot Attachment Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (inputState.chartImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = inputState.chartImageUri),
                        contentDescription = "Attached Chart",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, NeonCyan, RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chart Screenshot Attached",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Multimodal AI will evaluate chart structure & candles",
                            color = TextMuted,
                            fontSize = 10.5.sp
                        )
                    }
                    IconButton(onClick = { onSetChartImageUri(null) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Screenshot",
                            tint = SignalRed
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Attach Chart Screenshot (Optional)",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Zero-permission picker for TradingView / exchange charts",
                            color = TextMuted,
                            fontSize = 10.5.sp
                        )
                    }
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceElevated,
                            contentColor = NeonCyan
                        ),
                        modifier = Modifier.testTag("pick_chart_image_button")
                    ) {
                        Text("Pick", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // MODULE 1: MULTI-INDICATOR CONFLUENCE
        ModuleCollapsibleHeader(
            icon = Icons.Default.Timeline,
            title = "1. Multi-Indicator Confluence",
            statusSummary = "RSI ${inputState.rsi.toInt()} • ${inputState.macdStatus} • ${inputState.volumeRatio}x Vol",
            isExpanded = showIndicatorsSection,
            onToggle = { showIndicatorsSection = !showIndicatorsSection }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // RSI Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "RSI (14):", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        text = "${inputState.rsi.toInt()}  ${if (inputState.rsi < 35) "(Oversold / Reversal)" else if (inputState.rsi > 65) "(Overbought / Rejection)" else "(Neutral Zone)"}",
                        color = if (inputState.rsi < 35) SignalGreen else if (inputState.rsi > 65) SignalRed else NeonCyan,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = inputState.rsi.toFloat(),
                    onValueChange = { onUpdateInput { s -> s.copy(rsi = it.toDouble()) } },
                    valueRange = 5f..95f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = SurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // MACD Chip Group
                Text(text = "MACD Status:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Bullish Cross", "Bullish Histogram", "Bearish Cross", "Bearish Histogram").forEach { macd ->
                        val sel = inputState.macdStatus == macd
                        FilterChip(
                            selected = sel,
                            onClick = { onUpdateInput { s -> s.copy(macdStatus = macd) } },
                            label = { Text(macd, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (macd.contains("Bullish")) SignalGreen else SignalRed,
                                selectedLabelColor = Color(0xFF001F26),
                                containerColor = SurfaceElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // EMA 20/50/200 Stack
                Text(text = "EMA Alignment (20/50/200):", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Bullish Stack (20>50>200)",
                        "Bearish Stack (20<50<200)",
                        "Neutral / Mixed"
                    ).forEach { ema ->
                        val sel = inputState.emaAlignment == ema
                        FilterChip(
                            selected = sel,
                            onClick = { onUpdateInput { s -> s.copy(emaAlignment = ema) } },
                            label = { Text(ema, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (ema.contains("Bullish")) SignalGreen else if (ema.contains("Bearish")) SignalRed else SignalGold,
                                selectedLabelColor = Color(0xFF001F26),
                                containerColor = SurfaceElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Volume Expansion Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Volume vs Avg:", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        text = String.format(Locale.US, "%.1fx avg  %s", inputState.volumeRatio, if (inputState.volumeRatio >= 1.5) "(Confirmed ≥1.5x)" else "(Below Threshold)"),
                        color = if (inputState.volumeRatio >= 1.5) SignalGreen else SignalGold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = inputState.volumeRatio.toFloat(),
                    onValueChange = { onUpdateInput { s -> s.copy(volumeRatio = (it * 10).toInt() / 10.0) } },
                    valueRange = 0.5f..4.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = if (inputState.volumeRatio >= 1.5) SignalGreen else SignalGold,
                        activeTrackColor = if (inputState.volumeRatio >= 1.5) SignalGreen else SignalGold,
                        inactiveTrackColor = SurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // MODULE 4: SMART MONEY CONCEPTS (SMC / ICT)
        ModuleCollapsibleHeader(
            icon = Icons.Default.Layers,
            title = "4. Smart Money Concepts (SMC / ICT)",
            statusSummary = "${inputState.smcSweep.label} • ${inputState.smcStructureShift.label}",
            isExpanded = showSMCSection,
            onToggle = { showSMCSection = !showSMCSection }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Liquidity Sweep:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SMCSweep.entries.forEach { sweep ->
                        val sel = inputState.smcSweep == sweep
                        FilterChip(
                            selected = sel,
                            onClick = { onUpdateInput { s -> s.copy(smcSweep = sweep) } },
                            label = { Text(sweep.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = Color(0xFF001F26),
                                containerColor = SurfaceElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Market Structure Shift (MSS):", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SMCStructureShift.entries.forEach { mss ->
                        val sel = inputState.smcStructureShift == mss
                        FilterChip(
                            selected = sel,
                            onClick = { onUpdateInput { s -> s.copy(smcStructureShift = mss) } },
                            label = { Text(mss.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (mss.name.contains("BULLISH")) SignalGreen else if (mss.name.contains("BEARISH")) SignalRed else NeonCyan,
                                selectedLabelColor = Color(0xFF001F26),
                                containerColor = SurfaceElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "FVG / Order Block Mitigated:", color = TextPrimary, fontSize = 12.sp)
                        Text(text = "Mitigation triggers Market Entry vs Limit", color = TextMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = inputState.isMitigated,
                        onCheckedChange = { onUpdateInput { s -> s.copy(isMitigated = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan, checkedTrackColor = Color(0xFF004450))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // MODULE 2: PRICE ACTION & STRUCTURE
        ModuleCollapsibleHeader(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            title = "2. Price Action & Candlestick Patterns",
            statusSummary = "${inputState.trendStructure} • ${inputState.candlestickPattern.label}",
            isExpanded = showPriceActionSection,
            onToggle = { showPriceActionSection = !showPriceActionSection }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Trend Structure:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Higher Highs / Higher Lows", "Lower Highs / Lower Lows", "Consolidation / Range").forEach { trend ->
                        val sel = inputState.trendStructure == trend
                        FilterChip(
                            selected = sel,
                            onClick = { onUpdateInput { s -> s.copy(trendStructure = trend) } },
                            label = { Text(trend, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (trend.contains("Higher")) SignalGreen else if (trend.contains("Lower")) SignalRed else SignalGold,
                                selectedLabelColor = Color(0xFF001F26),
                                containerColor = SurfaceElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Candlestick Pattern:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CandlestickPattern.entries.forEach { pat ->
                        val sel = inputState.candlestickPattern == pat
                        FilterChip(
                            selected = sel,
                            onClick = { onUpdateInput { s -> s.copy(candlestickPattern = pat) } },
                            label = { Text(pat.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = Color(0xFF001F26),
                                containerColor = SurfaceElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = if (inputState.supportLevel > 0) inputState.supportLevel.toString() else "",
                        onValueChange = { str ->
                            onUpdateInput { s -> s.copy(supportLevel = str.toDoubleOrNull() ?: 0.0) }
                        },
                        label = { Text("Support Level", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SignalGreen,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )

                    OutlinedTextField(
                        value = if (inputState.resistanceLevel > 0) inputState.resistanceLevel.toString() else "",
                        onValueChange = { str ->
                            onUpdateInput { s -> s.copy(resistanceLevel = str.toDoubleOrNull() ?: 0.0) }
                        },
                        label = { Text("Resistance Level", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SignalRed,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // MODULE 3: NEWS & MACRO SENTIMENT
        ModuleCollapsibleHeader(
            icon = Icons.Default.Newspaper,
            title = "3. Fundamental & News Sentiment",
            statusSummary = "${inputState.newsSentiment.label}",
            isExpanded = showNewsSection,
            onToggle = { showNewsSection = !showNewsSection }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "Macro Stance:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MacroSentiment.entries.forEach { sent ->
                        val sel = inputState.newsSentiment == sent
                        FilterChip(
                            selected = sel,
                            onClick = { onUpdateInput { s -> s.copy(newsSentiment = sent) } },
                            label = { Text(sent.label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (sent) {
                                    MacroSentiment.BULLISH, MacroSentiment.DOVISH -> SignalGreen
                                    MacroSentiment.BEARISH, MacroSentiment.HAWKISH -> SignalRed
                                    MacroSentiment.HIGH_IMPACT_NEWS -> SignalGold
                                    else -> NeonCyan
                                },
                                selectedLabelColor = Color(0xFF001F26),
                                containerColor = SurfaceElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = inputState.newsHeadline,
                    onValueChange = { text -> onUpdateInput { s -> s.copy(newsHeadline = text) } },
                    label = { Text("News Context / Catalyst", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = SurfaceBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // MODULE 5 & 6: BREAKOUT VALIDATION & SCALPING
        ModuleCollapsibleHeader(
            icon = Icons.Default.Speed,
            title = "5 & 6. Breakout & Scalping Momentum",
            statusSummary = "Breakout Setup: ${if (inputState.isBreakoutSetup) "YES" else "NO"} • HTF Aligned: ${if (inputState.htfTrendAligned) "YES" else "NO"}",
            isExpanded = showScalpBreakoutSection,
            onToggle = { showScalpBreakoutSection = !showScalpBreakoutSection }
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Breakout Trading Mode:", color = TextPrimary, fontSize = 12.sp)
                        Text(text = "Requires candle body close beyond key level", color = TextMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = inputState.isBreakoutSetup,
                        onCheckedChange = { onUpdateInput { s -> s.copy(isBreakoutSetup = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                    )
                }

                if (inputState.isBreakoutSetup) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Candle Body Closed Beyond Level:", color = TextPrimary, fontSize = 12.sp)
                            Text(text = "Prevents fakeouts / liquidity wick grabs", color = TextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = inputState.candleClosedBeyondLevel,
                            onCheckedChange = { onUpdateInput { s -> s.copy(candleClosedBeyondLevel = it) } },
                            colors = SwitchDefaults.colors(checkedThumbColor = SignalGreen)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Higher Timeframe (1H/Daily) Aligned:", color = TextPrimary, fontSize = 12.sp)
                        Text(text = "Conflict Resolution rule forces HOLD if misaligned", color = TextMuted, fontSize = 10.sp)
                    }
                    Switch(
                        checked = inputState.htfTrendAligned,
                        onCheckedChange = { onUpdateInput { s -> s.copy(htfTrendAligned = it) } },
                        colors = SwitchDefaults.colors(checkedThumbColor = SignalGreen)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // MAIN ACTION: GENERATE CONFLUENCE SIGNAL
        Button(
            onClick = onEvaluate,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("generate_signal_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = Color(0xFF001F26)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isEvaluating
        ) {
            if (isEvaluating) {
                CircularProgressIndicator(
                    color = Color(0xFF001F26),
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "RUNNING CONFLUENCE DECISION ENGINE...",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.5.sp,
                    letterSpacing = 1.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "GENERATE CONFLUENCE SIGNAL",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    letterSpacing = 1.2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // EVALUATED SIGNAL RESULT DISPLAY
        if (signalOutput != null) {
            SignalResultCard(
                output = signalOutput,
                onSaveToJournal = onSaveToJournal
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ModuleCollapsibleHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    statusSummary: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .border(1.dp, if (isExpanded) SurfaceBorderHighlight else SurfaceBorder, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = statusSummary,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = TextSecondary
            )
        }

        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SurfaceBorder)
            )
            content()
        }
    }
}
