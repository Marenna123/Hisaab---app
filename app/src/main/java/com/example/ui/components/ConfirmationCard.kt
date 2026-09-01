package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionDirection
import com.example.data.model.TransactionType
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.DebitRed
import com.example.ui.theme.Emerald800
import com.example.ui.theme.VoiceAmber
import com.example.ui.theme.VoiceAmberLight
import com.example.voice.PendingOption
import com.example.voice.VoiceIntentResult

@Composable
fun AmbiguousConfirmationCard(
    result: VoiceIntentResult.AmbiguousIntent,
    onOptionSelected: (PendingOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("ambiguous_confirmation_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = VoiceAmberLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(VoiceAmber),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Confirm Direction",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirmation Needed",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF78350F)
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF78350F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = Color.White.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "\"${result.rawSpeech}\"",
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Question
            Text(
                text = result.question,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF451A03)
            )
            Text(
                text = result.teluguQuestion,
                fontSize = 14.sp,
                color = Color(0xFF78350F)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Two distinct clear choice buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Option A: Gave to person (Lent)
                Button(
                    onClick = { onOptionSelected(result.optionA) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("confirm_option_a_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DebitRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Gave (Lent)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = result.optionA.teluguLabel,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                // Option B: Repaid / Received
                Button(
                    onClick = { onOptionSelected(result.optionB) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("confirm_option_b_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CreditGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Repaid",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = result.optionB.teluguLabel,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewPersonConfirmationCard(
    result: VoiceIntentResult.PromptNewPerson,
    onConfirm: (candidateName: String, amount: Double?, type: TransactionType?, direction: TransactionDirection?, rawSpeech: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("new_person_confirmation_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "New Person",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "New Customer Detected",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF075985)
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF075985)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = Color.White.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "\"${result.rawSpeech}\"",
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = Color.Black.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = result.question,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0C4A6E)
            )
            Text(
                text = result.teluguQuestion,
                fontSize = 14.sp,
                color = Color(0xFF0369A1)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = Color(0xFF075985))
                }

                Button(
                    onClick = {
                        onConfirm(
                            result.candidateName,
                            result.pendingAmount,
                            result.pendingType,
                            result.pendingDirection,
                            result.rawSpeech
                        )
                    },
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("confirm_add_person_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add & Log Entry", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
