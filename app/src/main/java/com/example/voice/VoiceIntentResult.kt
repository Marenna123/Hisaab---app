package com.example.voice

import com.example.data.model.Person
import com.example.data.model.TransactionDirection
import com.example.data.model.TransactionType

sealed class VoiceIntentResult {
    data class ExecuteTransaction(
        val person: Person,
        val amount: Double,
        val type: TransactionType,
        val direction: TransactionDirection,
        val rawSpeech: String,
        val explanation: String,
        val teluguExplanation: String
    ) : VoiceIntentResult()

    data class BalanceCheck(
        val person: Person,
        val rawSpeech: String,
        val queryDescription: String
    ) : VoiceIntentResult()

    data class PromptNewPerson(
        val candidateName: String,
        val pendingAmount: Double?,
        val pendingType: TransactionType?,
        val pendingDirection: TransactionDirection?,
        val rawSpeech: String,
        val question: String,
        val teluguQuestion: String
    ) : VoiceIntentResult()

    data class AmbiguousIntent(
        val person: Person,
        val amount: Double,
        val rawSpeech: String,
        val question: String,
        val teluguQuestion: String,
        val optionA: PendingOption,
        val optionB: PendingOption
    ) : VoiceIntentResult()

    data class Unknown(
        val rawSpeech: String,
        val message: String,
        val teluguMessage: String
    ) : VoiceIntentResult()
}

data class PendingOption(
    val label: String,
    val teluguLabel: String,
    val type: TransactionType,
    val direction: TransactionDirection
)
