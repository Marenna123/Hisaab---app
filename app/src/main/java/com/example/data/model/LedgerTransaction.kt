package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ledger_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["personId"])]
)
data class LedgerTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personId: Long,
    val amount: Double,
    val type: TransactionType,
    val direction: TransactionDirection,
    val note: String = "",
    val rawVoiceText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
