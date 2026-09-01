package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DashboardSummary
import com.example.data.model.Person
import com.example.data.model.PersonLedgerSummary
import com.example.ui.components.AmbiguousConfirmationCard
import com.example.ui.components.NewPersonConfirmationCard
import com.example.ui.components.PersonLedgerItem
import com.example.ui.components.SummaryStatsHeader
import com.example.ui.theme.BorderSlate100
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.DebitRed
import com.example.ui.theme.PolishBlueContainer
import com.example.ui.theme.PolishBlueLight
import com.example.ui.theme.PolishBluePrimary
import com.example.ui.theme.PolishNavyDark
import com.example.ui.theme.SettledGray
import com.example.viewmodel.LedgerFilter
import com.example.voice.PendingOption
import com.example.voice.VoiceIntentResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    summaries: List<PersonLedgerSummary>,
    dashboardSummary: DashboardSummary,
    searchQuery: String,
    selectedFilter: LedgerFilter,
    selectedLanguage: String,
    activeVoiceResult: VoiceIntentResult?,
    notificationMessage: String?,
    onSearchChange: (String) -> Unit,
    onFilterChange: (LedgerFilter) -> Unit,
    onLanguageChange: (String) -> Unit,
    onPersonClick: (Long) -> Unit,
    onSpeakBalance: (Person) -> Unit,
    onOpenVoice: () -> Unit,
    onOpenVoiceLogs: () -> Unit,
    onOpenHelp: () -> Unit,
    onAddPersonClick: () -> Unit,
    onConfirmAmbiguousOption: (PendingOption, VoiceIntentResult.AmbiguousIntent) -> Unit,
    onConfirmNewPerson: (candidateName: String, amount: Double?, type: com.example.data.model.TransactionType?, direction: com.example.data.model.TransactionDirection?, rawSpeech: String) -> Unit,
    onDismissVoiceResult: () -> Unit,
    onClearNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notificationMessage) {
        notificationMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            onClearNotification()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Hisaab",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = PolishNavyDark,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "SHOP LEDGER • వాయిస్ లెడ్జర్",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B),
                            letterSpacing = 0.8.sp
                        )
                    }
                },
                actions = {
                    // Help Guide Button with #D1E4FF circle background
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PolishBlueContainer)
                            .clickable(onClick = onOpenHelp)
                            .testTag("help_guide_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Voice Guide",
                            tint = PolishNavyDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Voice Command History with #D1E4FF circle background
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PolishBlueContainer)
                            .clickable(onClick = onOpenVoiceLogs)
                            .testTag("voice_logs_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Voice History",
                            tint = PolishNavyDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Prominent Voice Bottom Container styled with #EEF1FF background, #D1E4FF border & halo mic button
            Surface(
                shadowElevation = 8.dp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(PolishBlueLight)
                            .border(1.dp, PolishBlueContainer, RoundedCornerShape(24.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left: Quick customer add or prompt description
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(onClick = onOpenVoice)
                            ) {
                                Text(
                                    text = "VOICE INPUT ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishBluePrimary,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "\"Ramesh gave ₹500\"...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Add customer small button
                            Button(
                                onClick = onAddPersonClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = PolishNavyDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_customer_button")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Person", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Main Big Mic Button with #D1E4FF halo ring
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(PolishBlueContainer)
                                    .padding(4.dp)
                                    .clip(CircleShape)
                                    .background(PolishBluePrimary)
                                    .clickable(onClick = onOpenVoice)
                                    .testTag("main_tap_to_speak_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Speak",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // In-line Ambiguous / New Person confirmation if present
            if (activeVoiceResult is VoiceIntentResult.AmbiguousIntent) {
                item {
                    AmbiguousConfirmationCard(
                        result = activeVoiceResult,
                        onOptionSelected = { option -> onConfirmAmbiguousOption(option, activeVoiceResult) },
                        onDismiss = onDismissVoiceResult
                    )
                }
            } else if (activeVoiceResult is VoiceIntentResult.PromptNewPerson) {
                item {
                    NewPersonConfirmationCard(
                        result = activeVoiceResult,
                        onConfirm = onConfirmNewPerson,
                        onDismiss = onDismissVoiceResult
                    )
                }
            }

            // Summary Stats Card Header (Hero 28dp card)
            item {
                SummaryStatsHeader(summary = dashboardSummary)
            }

            // Search Bar & Filter Chips Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header label
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Accounts",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishNavyDark
                        )
                        Text(
                            text = "${summaries.size} Accounts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PolishBluePrimary
                        )
                    }

                    // Search input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search customer (e.g. Ramesh, సుల్తాన్)", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = Color(0xFF64748B)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = BorderSlate100,
                            focusedBorderColor = PolishBluePrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_search_input")
                    )

                    // Filter Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == LedgerFilter.ALL,
                            onClick = { onFilterChange(LedgerFilter.ALL) },
                            label = { Text("All (${summaries.size})", fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PolishBluePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_chip_all")
                        )

                        FilterChip(
                            selected = selectedFilter == LedgerFilter.RECEIVABLE,
                            onClick = { onFilterChange(LedgerFilter.RECEIVABLE) },
                            label = { Text("Owes You (వసూలు)", fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DebitRed,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_chip_receivable")
                        )

                        FilterChip(
                            selected = selectedFilter == LedgerFilter.PAYABLE,
                            onClick = { onFilterChange(LedgerFilter.PAYABLE) },
                            label = { Text("You Owe (ఇవ్వాలి)", fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CreditGreen,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_chip_payable")
                        )

                        FilterChip(
                            selected = selectedFilter == LedgerFilter.SETTLED,
                            onClick = { onFilterChange(LedgerFilter.SETTLED) },
                            label = { Text("Settled (పూర్తయింది)", fontSize = 12.sp) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SettledGray,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_chip_settled")
                        )
                    }
                }
            }

            // Customer Ledgers List
            if (summaries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSlate100)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(PolishBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = PolishBluePrimary,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No accounts found for \"$searchQuery\"" else "No accounts found in this filter",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PolishNavyDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Speak a transaction like 'I gave 500 to Ramesh' or tap below",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenVoice,
                                colors = ButtonDefaults.buttonColors(containerColor = PolishBluePrimary),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Speak Now (మాట్లాడండి)")
                            }
                        }
                    }
                }
            } else {
                items(
                    items = summaries,
                    key = { it.person.id }
                ) { summary ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                        PersonLedgerItem(
                            summary = summary,
                            onClick = { onPersonClick(summary.person.id) },
                            onSpeakBalance = { onSpeakBalance(summary.person) },
                            onQuickVoiceForPerson = onOpenVoice
                        )
                    }
                }
            }
        }
    }
}

