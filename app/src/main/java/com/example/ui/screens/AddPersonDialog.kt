package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald800

@Composable
fun AddPersonDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onAddPerson: (name: String, phone: String, teluguName: String) -> Unit
) {
    if (!isOpen) return

    var name by remember { mutableStateOf("") }
    var teluguName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Customer (కొత్త ఖాతా)",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name (e.g. Ramesh)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_person_name_input")
                )

                OutlinedTextField(
                    value = teluguName,
                    onValueChange = { teluguName = it },
                    label = { Text("Telugu Name (optional, e.g. రమేష్)") },
                    leadingIcon = { Icon(Icons.Default.Translate, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_person_telugu_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (optional)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_person_phone_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAddPerson(name, phone, teluguName)
                        onDismiss()
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_add_person_button")
            ) {
                Text("Save Customer")
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
