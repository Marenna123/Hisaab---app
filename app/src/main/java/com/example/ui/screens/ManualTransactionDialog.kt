package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Person
import com.example.data.model.TransactionDirection
import com.example.data.model.TransactionType
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.DebitRed
import com.example.ui.theme.Emerald800

@Composable
fun ManualTransactionDialog(
    isOpen: Boolean,
    person: Person?,
    initialType: TransactionType = TransactionType.LENT,
    onDismiss: () -> Unit,
    onAddTransaction: (personId: Long, amount: Double, type: TransactionType, direction: TransactionDirection, note: String) -> Unit
) {
    if (!isOpen || person == null) return

    var amountText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(initialType) }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Entry for ${person.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == TransactionType.LENT,
                        onClick = { selectedType = TransactionType.LENT },
                        label = { Text("Gave (ఇచ్చాను)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DebitRed,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = selectedType == TransactionType.REPAYMENT,
                        onClick = { selectedType = TransactionType.REPAYMENT },
                        label = { Text("Received (వచ్చింది)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CreditGreen,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Amount (₹ మొత్తం)") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("manual_transaction_amount_input")
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note / Item Description (optional)") },
                    placeholder = { Text("e.g. Groceries, Rice bag, Cash") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("manual_transaction_note_input")
                )
            }
        },
        confirmButton = {
            val amount = amountText.toDoubleOrNull()
            Button(
                onClick = {
                    if (amount != null && amount > 0) {
                        val direction = if (selectedType == TransactionType.LENT) {
                            TransactionDirection.GAVE
                        } else {
                            TransactionDirection.RECEIVED
                        }
                        onAddTransaction(person.id, amount, selectedType, direction, noteText)
                        onDismiss()
                    }
                },
                enabled = amount != null && amount > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == TransactionType.LENT) DebitRed else CreditGreen
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_manual_transaction_button")
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
