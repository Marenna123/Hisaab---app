package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.VoiceInputDialog
import com.example.ui.screens.AddPersonDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HelpGuideDialog
import com.example.ui.screens.PersonDetailScreen
import com.example.ui.screens.VoiceLogScreen
import com.example.viewmodel.HisaabViewModel

enum class Screen {
    DASHBOARD,
    PERSON_DETAIL,
    VOICE_LOGS
}

@Composable
fun HisaabApp(
    viewModel: HisaabViewModel = viewModel()
) {
    val context = LocalContext.current

    val filteredSummaries by viewModel.filteredPersonsSummary.collectAsStateWithLifecycle()
    val allPersons by viewModel.allPersons.collectAsStateWithLifecycle()
    val personsWithSummary by viewModel.personsWithSummary.collectAsStateWithLifecycle()
    val dashboardSummary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val isVoiceDialogOpen by viewModel.isVoiceDialogOpen.collectAsStateWithLifecycle()
    val activeVoiceResult by viewModel.activeVoiceResult.collectAsStateWithLifecycle()
    val notificationMessage by viewModel.notificationMessage.collectAsStateWithLifecycle()
    val selectedPersonId by viewModel.selectedPersonId.collectAsStateWithLifecycle()
    val transactionsForSelectedPerson by viewModel.transactionsForSelectedPerson.collectAsStateWithLifecycle()
    val voiceLogs by viewModel.voiceLogs.collectAsStateWithLifecycle()

    val speechState by viewModel.speechManager.speechState.collectAsStateWithLifecycle()
    val spokenText by viewModel.speechManager.spokenText.collectAsStateWithLifecycle()
    val rmsLevel by viewModel.speechManager.rmsLevel.collectAsStateWithLifecycle()
    val speechError by viewModel.speechManager.errorMessage.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.openVoiceDialog()
        } else {
            Toast.makeText(context, "Microphone permission is needed for voice entry. You can also tap sample phrases.", Toast.LENGTH_LONG).show()
            viewModel.openVoiceDialog()
        }
    }

    val onTriggerVoice = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.openVoiceDialog()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            Screen.DASHBOARD -> {
                DashboardScreen(
                    summaries = filteredSummaries,
                    dashboardSummary = dashboardSummary,
                    searchQuery = searchQuery,
                    selectedFilter = selectedFilter,
                    selectedLanguage = selectedLanguage,
                    activeVoiceResult = activeVoiceResult,
                    notificationMessage = notificationMessage,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onFilterChange = { viewModel.setFilter(it) },
                    onLanguageChange = { viewModel.setLanguage(it) },
                    onPersonClick = { personId ->
                        viewModel.selectPersonForDetail(personId)
                        currentScreen = Screen.PERSON_DETAIL
                    },
                    onSpeakBalance = { person ->
                        viewModel.speakPersonBalance(person, selectedLanguage == "te-IN")
                    },
                    onOpenVoice = onTriggerVoice,
                    onOpenVoiceLogs = { currentScreen = Screen.VOICE_LOGS },
                    onOpenHelp = { showHelpDialog = true },
                    onAddPersonClick = { showAddPersonDialog = true },
                    onConfirmAmbiguousOption = { option, result ->
                        viewModel.confirmAmbiguousOption(option, result)
                    },
                    onConfirmNewPerson = { name, amount, type, dir, speech ->
                        viewModel.confirmCreateNewPersonAndProceed(name, amount, type, dir, speech)
                    },
                    onDismissVoiceResult = { viewModel.dismissVoiceResult() },
                    onClearNotification = { viewModel.clearNotification() }
                )
            }

            Screen.PERSON_DETAIL -> {
                val selectedSummary = personsWithSummary.find { it.person.id == selectedPersonId }
                PersonDetailScreen(
                    summary = selectedSummary,
                    transactions = transactionsForSelectedPerson,
                    onBack = {
                        viewModel.clearSelectedPerson()
                        currentScreen = Screen.DASHBOARD
                    },
                    onSpeakBalance = { person ->
                        viewModel.speakPersonBalance(person, selectedLanguage == "te-IN")
                    },
                    onOpenVoice = onTriggerVoice,
                    onAddManualTransaction = { id, amount, type, dir, note ->
                        viewModel.addManualTransaction(id, amount, type, dir, note)
                    },
                    onDeleteTransaction = { id -> viewModel.deleteTransaction(id) },
                    onSettleAccount = { id -> viewModel.settlePersonAccount(id) }
                )
            }

            Screen.VOICE_LOGS -> {
                VoiceLogScreen(
                    logs = voiceLogs,
                    onBack = { currentScreen = Screen.DASHBOARD },
                    onSpeakLog = { text -> viewModel.speechManager.speak(text, false) }
                )
            }
        }

        // Voice Input Modal Bottom Sheet
        VoiceInputDialog(
            isOpen = isVoiceDialogOpen,
            speechState = speechState,
            spokenText = spokenText,
            rmsLevel = rmsLevel,
            errorMessage = speechError,
            selectedLanguage = selectedLanguage,
            activeVoiceResult = activeVoiceResult,
            onStartListening = { viewModel.speechManager.startListening() },
            onStopListening = { viewModel.speechManager.stopListening() },
            onSimulatePhrase = { phrase -> viewModel.speechManager.simulateSpeechInput(phrase) },
            onLanguageChange = { viewModel.setLanguage(it) },
            onConfirmOption = { option, result ->
                viewModel.confirmAmbiguousOption(option, result)
            },
            onConfirmNewPerson = { name, amount, type, dir, speech ->
                viewModel.confirmCreateNewPersonAndProceed(name, amount, type, dir, speech)
            },
            onDismiss = { viewModel.closeVoiceDialog() }
        )

        // Manual Add Person Dialog
        AddPersonDialog(
            isOpen = showAddPersonDialog,
            onDismiss = { showAddPersonDialog = false },
            onAddPerson = { name, phone, teluguName ->
                viewModel.addNewPersonManual(name, phone, teluguName)
            }
        )

        // Help Guide Dialog
        HelpGuideDialog(
            isOpen = showHelpDialog,
            onDismiss = { showHelpDialog = false },
            onSimulatePhrase = { phrase ->
                viewModel.handleSpokenPhrase(phrase)
            }
        )
    }
}
