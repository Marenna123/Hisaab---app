package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.PersonLedgerSummary
import com.example.ui.theme.AvatarBlueBg
import com.example.ui.theme.AvatarBlueText
import com.example.ui.theme.AvatarEmeraldBg
import com.example.ui.theme.AvatarEmeraldText
import com.example.ui.theme.AvatarOrangeBg
import com.example.ui.theme.AvatarOrangeText
import com.example.ui.theme.AvatarPurpleBg
import com.example.ui.theme.AvatarPurpleText
import com.example.ui.theme.AvatarSlateBg
import com.example.ui.theme.AvatarSlateText
import com.example.ui.theme.BorderSlate100
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.DebitRed
import com.example.ui.theme.PolishNavyDark
import com.example.ui.theme.SettledGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PersonLedgerItem(
    summary: PersonLedgerSummary,
    onClick: () -> Unit,
    onSpeakBalance: () -> Unit,
    onQuickVoiceForPerson: () -> Unit,
    modifier: Modifier = Modifier
) {
    val person = summary.person
    val net = summary.netBalance
    val avatarPair = getAvatarColors(person.name)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(1.dp, BorderSlate100, RoundedCornerShape(24.dp))
            .testTag("person_ledger_item_${person.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Avatar + Name + Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Initials Avatar with styled pastel background and dark text
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(avatarPair.first),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = person.name.take(1).uppercase(Locale.ROOT),
                        color = avatarPair.second,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = person.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PolishNavyDark
                        )
                        if (person.teluguName.isNotBlank() && !person.teluguName.equals(person.name, ignoreCase = true)) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${person.teluguName})",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (summary.transactionCount > 0) {
                            "Last: ${formatTimestamp(summary.lastTransactionTime)} • ${summary.transactionCount} entries"
                        } else {
                            "No transactions yet"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Right: Balance badge + Quick voice actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    when {
                        net > 0.01 -> {
                            // Receivable (Customer owes shopkeeper)
                            Text(
                                text = "OWES YOU",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DebitRed,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "₹${formatAmount(net)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = DebitRed
                            )
                        }

                        net < -0.01 -> {
                            // Payable (Shopkeeper owes customer)
                            Text(
                                text = "YOU OWE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CreditGreen,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "₹${formatAmount(kotlin.math.abs(net))}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = CreditGreen
                            )
                        }

                        else -> {
                            Text(
                                text = "SETTLED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SettledGray,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = "₹0",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = SettledGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Voice Readback Button
                IconButton(
                    onClick = onSpeakBalance,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("speak_balance_${person.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Speak balance for ${person.name}",
                        tint = Color(0xFF0061A4),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun getAvatarColors(name: String): Pair<Color, Color> {
    val pairs = listOf(
        Pair(AvatarOrangeBg, AvatarOrangeText),
        Pair(AvatarEmeraldBg, AvatarEmeraldText),
        Pair(AvatarSlateBg, AvatarSlateText),
        Pair(AvatarBlueBg, AvatarBlueText),
        Pair(AvatarPurpleBg, AvatarPurpleText)
    )
    val hash = kotlin.math.abs(name.hashCode())
    return pairs[hash % pairs.size]
}

private fun formatAmount(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        amount.toLong().toString()
    } else {
        String.format(Locale.getDefault(), "%,.2f", amount)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

