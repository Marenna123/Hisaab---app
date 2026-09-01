package com.example.data.repository

import com.example.data.local.HisaabDao
import com.example.data.model.DashboardSummary
import com.example.data.model.LedgerTransaction
import com.example.data.model.Person
import com.example.data.model.PersonLedgerSummary
import com.example.data.model.TransactionDirection
import com.example.data.model.TransactionType
import com.example.data.model.VoiceCommandLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class HisaabRepository(private val dao: HisaabDao) {

    val allPersons: Flow<List<Person>> = dao.getAllPersonsFlow()
    val allTransactions: Flow<List<LedgerTransaction>> = dao.getAllTransactionsFlow()
    val voiceLogs: Flow<List<VoiceCommandLog>> = dao.getAllVoiceLogsFlow()

    val personsWithSummary: Flow<List<PersonLedgerSummary>> =
        combine(dao.getAllPersonsFlow(), dao.getAllTransactionsFlow()) { persons, transactions ->
            val transactionsByPerson = transactions.groupBy { it.personId }

            persons.map { person ->
                val pTransactions = transactionsByPerson[person.id] ?: emptyList()
                var totalLent = 0.0
                var totalBorrowed = 0.0
                var totalRepaid = 0.0

                pTransactions.forEach { tx ->
                    when (tx.type) {
                        TransactionType.LENT -> totalLent += tx.amount
                        TransactionType.BORROWED -> totalBorrowed += tx.amount
                        TransactionType.REPAYMENT -> {
                            if (tx.direction == TransactionDirection.RECEIVED) {
                                totalRepaid += tx.amount
                            } else {
                                // Shopkeeper paid back borrowed money
                                totalBorrowed -= tx.amount
                            }
                        }
                    }
                }

                // Net balance: positive = person owes shopkeeper (You will get), negative = shopkeeper owes person
                val netBalance = (totalLent - totalRepaid) - totalBorrowed
                val latestTime = pTransactions.maxOfOrNull { it.timestamp } ?: person.createdAt

                PersonLedgerSummary(
                    person = person,
                    totalLent = totalLent,
                    totalBorrowed = totalBorrowed,
                    totalRepaid = totalRepaid,
                    netBalance = netBalance,
                    lastTransactionTime = latestTime,
                    transactionCount = pTransactions.size
                )
            }.sortedByDescending { it.lastTransactionTime }
        }

    val dashboardSummary: Flow<DashboardSummary> = personsWithSummary.map { summaries ->
        var totalReceivable = 0.0
        var totalPayable = 0.0

        summaries.forEach { s ->
            if (s.netBalance > 0) {
                totalReceivable += s.netBalance
            } else if (s.netBalance < 0) {
                totalPayable += kotlin.math.abs(s.netBalance)
            }
        }

        DashboardSummary(
            totalReceivable = totalReceivable,
            totalPayable = totalPayable,
            netBalance = totalReceivable - totalPayable,
            activeCustomersCount = summaries.count { it.transactionCount > 0 },
            todayTransactionsCount = summaries.sumOf { it.transactionCount }
        )
    }

    fun getTransactionsForPerson(personId: Long): Flow<List<LedgerTransaction>> {
        return dao.getTransactionsForPersonFlow(personId)
    }

    suspend fun findPersonByName(name: String): Person? {
        return dao.findPersonByName(name)
    }

    suspend fun getAllPersonsList(): List<Person> {
        return dao.getAllPersons()
    }

    suspend fun insertPerson(person: Person): Long {
        return dao.insertPerson(person)
    }

    suspend fun updatePerson(person: Person) {
        dao.updatePerson(person)
    }

    suspend fun deletePerson(id: Long) {
        dao.deletePerson(id)
    }

    suspend fun insertTransaction(transaction: LedgerTransaction): Long {
        return dao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Long) {
        dao.deleteTransaction(id)
    }

    suspend fun clearTransactionsForPerson(personId: Long) {
        dao.clearTransactionsForPerson(personId)
    }

    suspend fun insertVoiceLog(log: VoiceCommandLog): Long {
        return dao.insertVoiceLog(log)
    }

    suspend fun clearVoiceLogs() {
        dao.clearVoiceLogs()
    }
}
