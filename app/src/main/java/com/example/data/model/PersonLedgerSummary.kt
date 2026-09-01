package com.example.data.model

data class PersonLedgerSummary(
    val person: Person,
    val totalLent: Double,     // Total shopkeeper gave to customer (Customer took)
    val totalBorrowed: Double, // Total shopkeeper took from customer
    val totalRepaid: Double,   // Total repaid / settled
    val netBalance: Double,    // Positive = Customer owes shopkeeper (You will get), Negative = Shopkeeper owes customer (You will give), 0 = Settled
    val lastTransactionTime: Long,
    val transactionCount: Int
)

data class DashboardSummary(
    val totalReceivable: Double, // "You Will Get" (బాకీ వసూలు)
    val totalPayable: Double,    // "You Will Give" (ఇవ్వాల్సింది)
    val netBalance: Double,
    val activeCustomersCount: Int,
    val todayTransactionsCount: Int
)
