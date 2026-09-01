package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.CreditGreenLight
import com.example.ui.theme.DebitRed
import com.example.ui.theme.DebitRedLight
import com.example.ui.theme.Emerald800
import com.example.ui.theme.VoiceAmber
import com.example.ui.theme.VoiceAmberLight

@Composable
fun HelpGuideDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onSimulatePhrase: (String) -> Unit
) {
    if (!isOpen) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = Emerald800,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Voice Command Guide",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Hisaab listens to natural speech in Telugu or English. Tap any sample phrase below to try it out:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Borrowed (Owner took money / Customer gave)
                GuideSection(
                    title = "1. Borrowed (తీసుకున్నాను)",
                    subtitle = "When you took money / borrowed from customer",
                    color = DebitRed,
                    bgColor = DebitRedLight,
                    icon = Icons.Default.ArrowUpward,
                    phrases = listOf(
                        "సుల్తాన్ దగ్గర 2000 తీసుకున్నాను" to "Telugu",
                        "I took 2000 from Sulthan" to "English",
                        "Sulthan gave me 2000" to "English"
                    ),
                    onPhraseClick = {
                        onSimulatePhrase(it)
                        onDismiss()
                    }
                )

                // 2. Lent (Owner gave money / Credit)
                GuideSection(
                    title = "2. Lent (ఇచ్చాను / ఖాతా)",
                    subtitle = "When you gave money / credit to customer",
                    color = DebitRed,
                    bgColor = DebitRedLight,
                    icon = Icons.Default.ArrowUpward,
                    phrases = listOf(
                        "రమేష్కి 500 ఇచ్చాను" to "Telugu",
                        "I gave 500 to Ramesh" to "English",
                        "Ramesh took 500 from me" to "English"
                    ),
                    onPhraseClick = {
                        onSimulatePhrase(it)
                        onDismiss()
                    }
                )

                // 3. Repayment (Customer paid back)
                GuideSection(
                    title = "3. Repayment (తిరిగి ఇచ్చాడు)",
                    subtitle = "When customer settles debt or pays back",
                    color = CreditGreen,
                    bgColor = CreditGreenLight,
                    icon = Icons.Default.ArrowDownward,
                    phrases = listOf(
                        "సుల్తాన్ 1000 తిరిగి ఇచ్చాడు" to "Telugu",
                        "Sulthan paid back 1000" to "English",
                        "Received 1000 from Sulthan" to "English"
                    ),
                    onPhraseClick = {
                        onSimulatePhrase(it)
                        onDismiss()
                    }
                )

                // 4. Balance Check
                GuideSection(
                    title = "4. Balance Check (బాకీ ఎంత)",
                    subtitle = "Check who owes what, spoken back to you via audio",
                    color = VoiceAmber,
                    bgColor = VoiceAmberLight,
                    icon = Icons.Default.QuestionAnswer,
                    phrases = listOf(
                        "సుల్తాన్ ఎంత బాకీ ఉన్నాడు?" to "Telugu",
                        "Show Ramesh balance" to "English",
                        "How much does Sulthan owe?" to "English"
                    ),
                    onPhraseClick = {
                        onSimulatePhrase(it)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Emerald800),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Got It")
            }
        }
    )
}

@Composable
private fun GuideSection(
    title: String,
    subtitle: String,
    color: Color,
    bgColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    phrases: List<Pair<String, String>>,
    onPhraseClick: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
            }
            Text(text = subtitle, fontSize = 11.sp, color = color.copy(alpha = 0.8f))

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                phrases.forEach { (phrase, lang) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPhraseClick(phrase) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🗣️ \"$phrase\"",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black.copy(alpha = 0.85f)
                            )
                            Text(
                                text = lang,
                                fontSize = 10.sp,
                                color = color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
