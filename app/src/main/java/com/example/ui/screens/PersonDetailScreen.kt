package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LedgerTransaction
import com.example.data.model.Person
import com.example.data.model.PersonLedgerSummary
import com.example.data.model.TransactionDirection
import com.example.data.model.TransactionType
import com.example.ui.theme.BorderSlate100
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.CreditGreenLight
import com.example.ui.theme.DebitRed
import com.example.ui.theme.DebitRedLight
import com.example.ui.theme.PolishBlueContainer
import com.example.ui.theme.PolishBluePrimary
import com.example.ui.theme.PolishNavyDark
import com.example.ui.theme.SettledGray
import com.example.ui.theme.SettledGrayLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    summary: PersonLedgerSummary?,
    transactions: List<LedgerTransaction>,
    onBack: () -> Unit,
    onSpeakBalance: (Person) -> Unit,
    onOpenVoice: () -> Unit,
    onAddManualTransaction: (personId: Long, amount: Double, type: TransactionType, direction: TransactionDirection, note: String) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onSettleAccount: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (summary == null) return
    val person = summary.person
    val context = LocalContext.current

    var showManualDialog by remember { mutableStateOf(false) }
    var manualDialogType by remember { mutableStateOf(TransactionType.LENT) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = person.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = PolishNavyDark
                            )
                            if (person.teluguName.isNotBlank() && !person.teluguName.equals(person.name, ignoreCase = true)) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${person.teluguName})",
                                    fontSize = 14.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                        if (person.phoneNumber.isNotBlank()) {
                            Text(
                                text = person.phoneNumber,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("person_detail_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PolishNavyDark
                        )
                    }
                },
                actions = {
                    // Speak balance
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PolishBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { onSpeakBalance(person) },
                            modifier = Modifier.testTag("person_detail_speak_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Speak balance",
                                tint = PolishNavyDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Share Statement summary
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PolishBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                val net = summary.netBalance
                                val statusText = if (net > 0) {
                                    "Due balance to pay: ₹${formatAmount(net)}"
                                } else if (net < 0) {
                                    "Advance credit: ₹${formatAmount(kotlin.math.abs(net))}"
                                } else {
                                    "Account is settled with ₹0 balance."
                                }
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Hisaab Ledger Statement for ${person.name}:\nTotal Taken: ₹${formatAmount(summary.totalLent)}\nTotal Repaid: ₹${formatAmount(summary.totalRepaid)}\nNet Balance: $statusText\n- Sent via Hisaab App"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Hisaab Statement"))
                            },
                            modifier = Modifier.testTag("share_statement_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = PolishNavyDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // Sticky Action Bar at bottom
            Surface(
                shadowElevation = 8.dp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gave (Lent) Button
                    Button(
                        onClick = {
                            manualDialogType = TransactionType.LENT
                            showManualDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DebitRed),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gave_lent_button")
                    ) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gave ₹ (ఇచ్చాను)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Received (Repay) Button
                    Button(
                        onClick = {
                            manualDialogType = TransactionType.REPAYMENT
                            showManualDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CreditGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("received_repay_button")
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Received ₹ (వచ్చింది)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Mic Quick Button
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PolishBlueContainer)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(PolishBluePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = onOpenVoice,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("detail_quick_mic_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Speak for ${person.name}",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("person_detail_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Net Balance Summary Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            summary.netBalance > 0.01 -> Color.White
                            summary.netBalance < -0.01 -> Color.White
                            else -> Color.White
                        }
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSlate100),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CURRENT RUNNING BALANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = when {
                                    summary.netBalance > 0.01 -> DebitRed
                                    summary.netBalance < -0.01 -> CreditGreen
                                    else -> SettledGray
                                }
                            )

                            if (kotlin.math.abs(summary.netBalance) > 0.01) {
                                OutlinedButton(
                                    onClick = { onSettleAccount(person.id) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("settle_account_button")
                                ) {
                                    Text("Settle Up (₹0)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "₹${formatAmount(kotlin.math.abs(summary.netBalance))}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                summary.netBalance > 0.01 -> DebitRed
                                summary.netBalance < -0.01 -> CreditGreen
                                else -> SettledGray
                            }
                        )

                        Text(
                            text = when {
                                summary.netBalance > 0.01 -> "${person.name} owes you (మీకు రావాల్సిన బాకీ)"
                                summary.netBalance < -0.01 -> "You owe ${person.name} (మీరు ఇవ్వాల్సిన మొత్తం)"
                                else -> "All accounts cleared (ఎటువంటి బాకీ లేదు)"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats breakdown row: Total Taken vs Total Repaid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = DebitRedLight
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Total Given (తీసుకున్నది)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DebitRed)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₹${formatAmount(summary.totalLent)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DebitRed
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = CreditGreenLight
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(text = "Total Repaid (తిరిగి ఇచ్చినది)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CreditGreen)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "₹${formatAmount(summary.totalRepaid)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CreditGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Transactions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ledger Statement (${transactions.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PolishNavyDark
                    )
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSlate100),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No transactions logged yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = PolishNavyDark
                            )
                            Text(
                                text = "Speak or tap Give/Received below to add the first entry",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            } else {
                // Calculate running balance after each transaction
                var running = 0.0
                val transactionsWithBalance = transactions.map { tx ->
                    when (tx.type) {
                        TransactionType.LENT -> running += tx.amount
                        TransactionType.BORROWED -> running -= tx.amount
                        TransactionType.REPAYMENT -> {
                            if (tx.direction == TransactionDirection.RECEIVED) {
                                running -= tx.amount
                            } else {
                                running += tx.amount
                            }
                        }
                    }
                    tx to running
                }

                items(transactionsWithBalance.reversed()) { (tx, balanceAfter) ->
                    TransactionItemRow(
                        transaction = tx,
                        runningBalanceAfter = balanceAfter,
                        onDelete = { onDeleteTransaction(tx.id) }
                    )
                }
            }
        }
    }

    ManualTransactionDialog(
        isOpen = showManualDialog,
        person = person,
        initialType = manualDialogType,
        onDismiss = { showManualDialog = false },
        onAddTransaction = onAddManualTransaction
    )
}

@Composable
private fun TransactionItemRow(
    transaction: LedgerTransaction,
    runningBalanceAfter: Double,
    onDelete: () -> Unit
) {
    val isDebit = transaction.type == TransactionType.LENT
    val isCredit = transaction.type == TransactionType.REPAYMENT || transaction.direction == TransactionDirection.RECEIVED

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSlate100),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Icon + Type + Date + Note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDebit) DebitRedLight else CreditGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDebit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isDebit) DebitRed else CreditGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (transaction.type) {
                                TransactionType.LENT -> "Gave Credit (ఇచ్చారు)"
                                TransactionType.REPAYMENT -> "Payment Received (తిరిగి వచ్చింది)"
                                TransactionType.BORROWED -> "Borrowed (తీసుకున్నారు)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PolishNavyDark
                        )
                    }

                    if (transaction.note.isNotBlank()) {
                        Text(
                            text = transaction.note,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Text(
                        text = formatDateTime(transaction.timestamp),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Right: Amount + Running Balance + Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isDebit) "- ₹${formatAmount(transaction.amount)}" else "+ ₹${formatAmount(transaction.amount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isDebit) DebitRed else CreditGreen
                    )
                    Text(
                        text = "Bal: ₹${formatAmount(kotlin.math.abs(runningBalanceAfter))}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(start = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete entry",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        amount.toLong().toString()
    } else {
        String.format(Locale.getDefault(), "%,.2f", amount)
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

