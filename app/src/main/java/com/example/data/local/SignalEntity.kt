package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signals")
data class SignalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val asset: String,
    val timeframe: String,
    val signal: String, // UP | DOWN | HOLD
    val confidenceScore: String,
    val executionType: String,
    val entryZone: String,
    val stopLoss: String,
    val takeProfit1: String,
    val takeProfit2: String,
    val riskRewardRatio: String,
    val primaryStrategy: String,
    val tradeRationale: String,
    val jsonPayload: String,
    val outcomeStatus: String = "ACTIVE" // ACTIVE, TARGET_1_HIT, TARGET_2_HIT, STOPPED_OUT, INVALIDATED
)
