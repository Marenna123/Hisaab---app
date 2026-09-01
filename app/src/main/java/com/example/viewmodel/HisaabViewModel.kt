package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.HisaabDatabase
import com.example.data.model.DashboardSummary
import com.example.data.model.LedgerTransaction
import com.example.data.model.Person
import com.example.data.model.PersonLedgerSummary
import com.example.data.model.TransactionDirection
import com.example.data.model.TransactionType
import com.example.data.model.VoiceCommandLog
import com.example.data.repository.HisaabRepository
import com.example.voice.PendingOption
import com.example.voice.SpeechState
import com.example.voice.VoiceIntentParser
import com.example.voice.VoiceIntentResult
import com.example.voice.VoiceSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class LedgerFilter {
    ALL,
    RECEIVABLE, // Customers who owe you (You Will Get)
    PAYABLE,    // You owe them (You Will Give)
    SETTLED     // Zero balance
}

class HisaabViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HisaabRepository
    val speechManager: VoiceSpeechManager = VoiceSpeechManager(application)

    init {
        val database = HisaabDatabase.getDatabase(application, viewModelScope)
        repository = HisaabRepository(database.hisaabDao())

        speechManager.onFinalSpeechResult = { phrase ->
            handleSpokenPhrase(phrase)
        }
    }

    val allPersons: StateFlow<List<Person>> = repository.allPersons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val personsWithSummary: StateFlow<List<PersonLedgerSummary>> = repository.personsWithSummary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardSummary: StateFlow<DashboardSummary> = repository.dashboardSummary
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DashboardSummary(0.0, 0.0, 0.0, 0, 0)
        )

    val voiceLogs: StateFlow<List<VoiceCommandLog>> = repository.voiceLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(LedgerFilter.ALL)
    val selectedFilter: StateFlow<LedgerFilter> = _selectedFilter.asStateFlow()

    // Filtered Persons Flow
    val filteredPersonsSummary: StateFlow<List<PersonLedgerSummary>> = combine(
        repository.personsWithSummary,
        _searchQuery,
        _selectedFilter
    ) { summaries, query, filter ->
        summaries.filter { summary ->
            val matchesQuery = query.isBlank() ||
                    summary.person.name.contains(query, ignoreCase = true) ||
                    summary.person.teluguName.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                LedgerFilter.ALL -> true
                LedgerFilter.RECEIVABLE -> summary.netBalance > 0.01
                LedgerFilter.PAYABLE -> summary.netBalance < -0.01
                LedgerFilter.SETTLED -> kotlin.math.abs(summary.netBalance) <= 0.01
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Dialog States
    private val _isVoiceDialogOpen = MutableStateFlow(false)
    val isVoiceDialogOpen: StateFlow<Boolean> = _isVoiceDialogOpen.asStateFlow()

    private val _activeVoiceResult = MutableStateFlow<VoiceIntentResult?>(null)
    val activeVoiceResult: StateFlow<VoiceIntentResult?> = _activeVoiceResult.asStateFlow()

    private val _selectedPersonId = MutableStateFlow<Long?>(null)
    val selectedPersonId: StateFlow<Long?> = _selectedPersonId.asStateFlow()

    private val _transactionsForSelectedPerson = MutableStateFlow<List<LedgerTransaction>>(emptyList())
    val transactionsForSelectedPerson: StateFlow<List<LedgerTransaction>> = _transactionsForSelectedPerson.asStateFlow()

    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("auto")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    fun setLanguage(langCode: String) {
        _selectedLanguage.value = langCode
        speechManager.selectedLanguageCode = langCode
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: LedgerFilter) {
        _selectedFilter.value = filter
    }

    fun openVoiceDialog() {
        _isVoiceDialogOpen.value = true
        _activeVoiceResult.value = null
        speechManager.startListening()
    }

    fun closeVoiceDialog() {
        _isVoiceDialogOpen.value = false
        speechManager.cancelListening()
    }

    fun selectPersonForDetail(personId: Long) {
        _selectedPersonId.value = personId
        viewModelScope.launch {
            repository.getTransactionsForPerson(personId).collect {
                _transactionsForSelectedPerson.value = it
            }
        }
    }

    fun clearSelectedPerson() {
        _selectedPersonId.value = null
        _transactionsForSelectedPerson.value = emptyList()
    }

    fun handleSpokenPhrase(phrase: String) {
        viewModelScope.launch {
            val contacts = allPersons.value
            val result = VoiceIntentParser.parse(phrase, contacts)
            _activeVoiceResult.value = result

            when (result) {
                is VoiceIntentResult.ExecuteTransaction -> {
                    // Auto-execute clear intent
                    executeTransaction(result)
                }

                is VoiceIntentResult.BalanceCheck -> {
                    // Calculate and speak balance
                    speakPersonBalance(result.person, isTeluguPhrase(phrase))
                }

                is VoiceIntentResult.AmbiguousIntent -> {
                    // Present confirmation prompt to shopkeeper
                    val questionToSpeak = if (isTeluguPhrase(phrase)) result.teluguQuestion else result.question
                    speechManager.speak(questionToSpeak, isTeluguPhrase(phrase))
                }

                is VoiceIntentResult.PromptNewPerson -> {
                    val promptToSpeak = if (isTeluguPhrase(phrase)) result.teluguQuestion else result.question
                    speechManager.speak(promptToSpeak, isTeluguPhrase(phrase))
                }

                is VoiceIntentResult.Unknown -> {
                    val msg = if (isTeluguPhrase(phrase)) result.teluguMessage else result.message
                    speechManager.speak(msg, isTeluguPhrase(phrase))
                }
            }
        }
    }

    private suspend fun executeTransaction(intent: VoiceIntentResult.ExecuteTransaction) {
        val tx = LedgerTransaction(
            personId = intent.person.id,
            amount = intent.amount,
            type = intent.type,
            direction = intent.direction,
            note = "Voice: ${intent.rawSpeech}",
            rawVoiceText = intent.rawSpeech,
            timestamp = System.currentTimeMillis()
        )
        repository.insertTransaction(tx)

        // Record Voice Log
        repository.insertVoiceLog(
            VoiceCommandLog(
                rawText = intent.rawSpeech,
                language = if (isTeluguPhrase(intent.rawSpeech)) "te" else "en",
                parsedIntent = intent.explanation,
                matchedPersonName = intent.person.name,
                amount = intent.amount,
                status = "SUCCESS"
            )
        )

        // Read confirmation aloud
        val isTe = isTeluguPhrase(intent.rawSpeech)
        val speechText = if (isTe) {
            "${intent.person.name} ఖాతాలో ₹${formatAmount(intent.amount)} నమోదయింది"
        } else {
            "Recorded ₹${formatAmount(intent.amount)} for ${intent.person.name}"
        }
        speechManager.speak(speechText, isTe)
        _notificationMessage.value = intent.explanation
    }

    fun confirmAmbiguousOption(option: PendingOption, result: VoiceIntentResult.AmbiguousIntent) {
        viewModelScope.launch {
            val tx = LedgerTransaction(
                personId = result.person.id,
                amount = result.amount,
                type = option.type,
                direction = option.direction,
                note = "Confirmed: ${option.label}",
                rawVoiceText = result.rawSpeech,
                timestamp = System.currentTimeMillis()
            )
            repository.insertTransaction(tx)

            repository.insertVoiceLog(
                VoiceCommandLog(
                    rawText = result.rawSpeech,
                    language = if (isTeluguPhrase(result.rawSpeech)) "te" else "en",
                    parsedIntent = "${option.label} (₹${formatAmount(result.amount)})",
                    matchedPersonName = result.person.name,
                    amount = result.amount,
                    status = "SUCCESS"
                )
            )

            val isTe = isTeluguPhrase(result.rawSpeech)
            val confirmMsg = if (isTe) {
                "${result.person.name} ఖాతాలో ₹${formatAmount(result.amount)} నమోదయింది"
            } else {
                "Logged ₹${formatAmount(result.amount)} for ${result.person.name}"
            }
            speechManager.speak(confirmMsg, isTe)
            _activeVoiceResult.value = null
            _notificationMessage.value = confirmMsg
        }
    }

    fun confirmCreateNewPersonAndProceed(
        candidateName: String,
        amount: Double?,
        type: TransactionType?,
        direction: TransactionDirection?,
        rawSpeech: String
    ) {
        viewModelScope.launch {
            val newPerson = Person(
                name = candidateName.trim(),
                teluguName = candidateName.trim(),
                phoneNumber = ""
            )
            val newPersonId = repository.insertPerson(newPerson)

            if (amount != null && amount > 0 && type != null && direction != null) {
                val tx = LedgerTransaction(
                    personId = newPersonId,
                    amount = amount,
                    type = type,
                    direction = direction,
                    note = "Voice initial entry: $rawSpeech",
                    rawVoiceText = rawSpeech,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertTransaction(tx)

                repository.insertVoiceLog(
                    VoiceCommandLog(
                        rawText = rawSpeech,
                        language = if (isTeluguPhrase(rawSpeech)) "te" else "en",
                        parsedIntent = "Added new person $candidateName with ₹${formatAmount(amount)}",
                        matchedPersonName = candidateName,
                        amount = amount,
                        status = "SUCCESS"
                    )
                )

                val isTe = isTeluguPhrase(rawSpeech)
                val msg = if (isTe) {
                    "కొత్త వ్యక్తి $candidateName చేర్చబడింది మరియు ₹${formatAmount(amount)} నమోదయింది"
                } else {
                    "Added $candidateName and recorded ₹${formatAmount(amount)}"
                }
                speechManager.speak(msg, isTe)
                _notificationMessage.value = msg
            } else {
                _notificationMessage.value = "Added $candidateName"
            }

            _activeVoiceResult.value = null
        }
    }

    fun speakPersonBalance(person: Person, isTelugu: Boolean = false) {
        viewModelScope.launch {
            val summary = personsWithSummary.value.find { it.person.id == person.id }
            val net = summary?.netBalance ?: 0.0

            val speech = if (isTelugu) {
                when {
                    net > 0.01 -> "${person.name} మీకు ₹${formatAmount(net)} బాకీ ఉన్నాడు"
                    net < -0.01 -> "మీరు ${person.name} కి ₹${formatAmount(kotlin.math.abs(net))} ఇవ్వాలి"
                    else -> "${person.name} ఖాతా పూర్తయింది, ఎటువంటి బాకీ లేదు"
                }
            } else {
                when {
                    net > 0.01 -> "${person.name} owes you ₹${formatAmount(net)}"
                    net < -0.01 -> "You owe ${person.name} ₹${formatAmount(kotlin.math.abs(net))}"
                    else -> "${person.name}'s account is fully settled with zero balance"
                }
            }

            speechManager.speak(speech, isTelugu)
            _notificationMessage.value = speech
        }
    }

    fun addManualTransaction(
        personId: Long,
        amount: Double,
        type: TransactionType,
        direction: TransactionDirection,
        note: String
    ) {
        viewModelScope.launch {
            val tx = LedgerTransaction(
                personId = personId,
                amount = amount,
                type = type,
                direction = direction,
                note = note.ifBlank { if (type == TransactionType.LENT) "Gave credit" else "Received payment" },
                rawVoiceText = "Manual Entry",
                timestamp = System.currentTimeMillis()
            )
            repository.insertTransaction(tx)
            _notificationMessage.value = "Transaction saved"
        }
    }

    fun addNewPersonManual(name: String, phoneNumber: String, teluguName: String = "") {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val p = Person(
                    name = name.trim(),
                    teluguName = teluguName.trim().ifBlank { name.trim() },
                    phoneNumber = phoneNumber.trim()
                )
                repository.insertPerson(p)
                _notificationMessage.value = "Added customer $name"
            }
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
            _notificationMessage.value = "Transaction deleted"
        }
    }

    fun settlePersonAccount(personId: Long) {
        viewModelScope.launch {
            val summary = personsWithSummary.value.find { it.person.id == personId } ?: return@launch
            val balance = summary.netBalance
            if (kotlin.math.abs(balance) > 0.01) {
                if (balance > 0) {
                    // Customer pays full balance to shopkeeper
                    repository.insertTransaction(
                        LedgerTransaction(
                            personId = personId,
                            amount = balance,
                            type = TransactionType.REPAYMENT,
                            direction = TransactionDirection.RECEIVED,
                            note = "Full Balance Settlement",
                            rawVoiceText = "Account Settled",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Shopkeeper pays back to customer
                    repository.insertTransaction(
                        LedgerTransaction(
                            personId = personId,
                            amount = kotlin.math.abs(balance),
                            type = TransactionType.REPAYMENT,
                            direction = TransactionDirection.GAVE,
                            note = "Debt Paid in Full",
                            rawVoiceText = "Account Settled",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                _notificationMessage.value = "Account settled to ₹0"
            }
        }
    }

    fun dismissVoiceResult() {
        _activeVoiceResult.value = null
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }

    private fun isTeluguPhrase(text: String): Boolean {
        return text.any { it in '\u0C00'..'\u0C7F' } ||
                text.contains("icchanu", ignoreCase = true) ||
                text.contains("teesukunnanu", ignoreCase = true) ||
                text.contains("baaki", ignoreCase = true) ||
                text.contains("tirigi", ignoreCase = true)
    }

    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            amount.toLong().toString()
        } else {
            String.format(Locale.getDefault(), "%.2f", amount)
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
