package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.LedgerTransaction
import com.example.data.model.Person
import com.example.data.model.TransactionDirection
import com.example.data.model.TransactionType
import com.example.data.model.VoiceCommandLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Person::class, LedgerTransaction::class, VoiceCommandLog::class],
    version = 1,
    exportSchema = false
)
abstract class HisaabDatabase : RoomDatabase() {

    abstract fun hisaabDao(): HisaabDao

    companion object {
        @Volatile
        private var INSTANCE: HisaabDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): HisaabDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HisaabDatabase::class.java,
                    "hisaab_ledger_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.hisaabDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: HisaabDao) {
                // Pre-populate with realistic shopkeeper ledger entries
                val sulthanId = dao.insertPerson(
                    Person(name = "Sulthan", teluguName = "సుల్తాన్", phoneNumber = "+91 98480 12345")
                )
                val rameshId = dao.insertPerson(
                    Person(name = "Ramesh", teluguName = "రమేష్", phoneNumber = "+91 94401 67890")
                )
                val lakshmiId = dao.insertPerson(
                    Person(name = "Lakshmi", teluguName = "లక్ష్మి", phoneNumber = "+91 91234 56789")
                )
                val venkatId = dao.insertPerson(
                    Person(name = "Venkat", teluguName = "వెంకట్", phoneNumber = "+91 99887 76655")
                )

                val now = System.currentTimeMillis()
                val oneDay = 86400000L

                // Sulthan borrowed 2000 (Customer took money / goods on credit)
                dao.insertTransaction(
                    LedgerTransaction(
                        personId = sulthanId,
                        amount = 2000.0,
                        type = TransactionType.LENT,
                        direction = TransactionDirection.GAVE,
                        note = "Groceries & Cash credit",
                        rawVoiceText = "I took 2000 from Sulthan / సుల్తాన్ 2000 తీసుకున్నాడు",
                        timestamp = now - (oneDay * 3)
                    )
                )

                // Sulthan repaid 1000
                dao.insertTransaction(
                    LedgerTransaction(
                        personId = sulthanId,
                        amount = 1000.0,
                        type = TransactionType.REPAYMENT,
                        direction = TransactionDirection.RECEIVED,
                        note = "Cash partial payment",
                        rawVoiceText = "Sulthan paid back 1000 / సుల్తాన్ 1000 తిరిగి ఇచ్చాడు",
                        timestamp = now - oneDay
                    )
                )

                // Ramesh lent 500 (Owner gave 500 to Ramesh)
                dao.insertTransaction(
                    LedgerTransaction(
                        personId = rameshId,
                        amount = 500.0,
                        type = TransactionType.LENT,
                        direction = TransactionDirection.GAVE,
                        note = "Store purchase on credit",
                        rawVoiceText = "I gave 500 to Ramesh / రమేష్కి 500 ఇచ్చాను",
                        timestamp = now - (oneDay * 2)
                    )
                )

                // Lakshmi borrowed 1200
                dao.insertTransaction(
                    LedgerTransaction(
                        personId = lakshmiId,
                        amount = 1200.0,
                        type = TransactionType.LENT,
                        direction = TransactionDirection.GAVE,
                        note = "Rice bag & provisions",
                        rawVoiceText = "Lakshmi took 1200 / లక్ష్మి 1200 తీసుకుంది",
                        timestamp = now - (oneDay * 4)
                    )
                )

                // Initial Voice Command Log
                dao.insertVoiceLog(
                    VoiceCommandLog(
                        rawText = "సుల్తాన్ 1000 తిరిగి ఇచ్చాడు",
                        language = "te",
                        parsedIntent = "Repayment of ₹1000 by Sulthan",
                        matchedPersonName = "Sulthan",
                        amount = 1000.0,
                        status = "SUCCESS",
                        timestamp = now - oneDay
                    )
                )
            }
        }
    }
}
