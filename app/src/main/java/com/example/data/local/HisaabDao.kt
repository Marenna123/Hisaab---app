package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LedgerTransaction
import com.example.data.model.Person
import com.example.data.model.VoiceCommandLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HisaabDao {

    @Query("SELECT * FROM persons ORDER BY name ASC")
    fun getAllPersonsFlow(): Flow<List<Person>>

    @Query("SELECT * FROM persons")
    suspend fun getAllPersons(): List<Person>

    @Query("SELECT * FROM persons WHERE id = :id LIMIT 1")
    suspend fun getPersonById(id: Long): Person?

    @Query("SELECT * FROM persons WHERE LOWER(name) = LOWER(:name) OR LOWER(teluguName) = LOWER(:name) LIMIT 1")
    suspend fun findPersonByName(name: String): Person?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: Person): Long

    @Update
    suspend fun updatePerson(person: Person)

    @Query("DELETE FROM persons WHERE id = :id")
    suspend fun deletePerson(id: Long)

    @Query("SELECT * FROM ledger_transactions WHERE personId = :personId ORDER BY timestamp ASC")
    fun getTransactionsForPersonFlow(personId: Long): Flow<List<LedgerTransaction>>

    @Query("SELECT * FROM ledger_transactions WHERE personId = :personId ORDER BY timestamp ASC")
    suspend fun getTransactionsForPerson(personId: Long): List<LedgerTransaction>

    @Query("SELECT * FROM ledger_transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<LedgerTransaction>>

    @Query("SELECT * FROM ledger_transactions ORDER BY timestamp DESC")
    suspend fun getAllTransactions(): List<LedgerTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: LedgerTransaction): Long

    @Query("DELETE FROM ledger_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("DELETE FROM ledger_transactions WHERE personId = :personId")
    suspend fun clearTransactionsForPerson(personId: Long)

    @Query("SELECT * FROM voice_command_logs ORDER BY timestamp DESC")
    fun getAllVoiceLogsFlow(): Flow<List<VoiceCommandLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceLog(log: VoiceCommandLog): Long

    @Query("DELETE FROM voice_command_logs")
    suspend fun clearVoiceLogs()
}
