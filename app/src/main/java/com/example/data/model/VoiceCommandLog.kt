package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_command_logs")
data class VoiceCommandLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawText: String,
    val language: String, // "te" or "en"
    val parsedIntent: String,
    val matchedPersonName: String = "",
    val amount: Double = 0.0,
    val status: String = "SUCCESS", // "SUCCESS", "AMBIGUOUS", "NEW_PERSON", "UNKNOWN"
    val timestamp: Long = System.currentTimeMillis()
)
