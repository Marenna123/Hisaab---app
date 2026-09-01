package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSlate100
import com.example.ui.theme.PolishBlueContainer
import com.example.ui.theme.PolishBlueLight
import com.example.ui.theme.PolishBluePrimary
import com.example.ui.theme.PolishNavyDark
import com.example.ui.theme.VoiceAmber
import com.example.voice.PendingOption
import com.example.voice.SpeechState
import com.example.voice.VoiceIntentResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputDialog(
    isOpen: Boolean,
    speechState: SpeechState,
    spokenText: String,
    rmsLevel: Float,
    errorMessage: String?,
    selectedLanguage: String,
    activeVoiceResult: VoiceIntentResult?,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSimulatePhrase: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onConfirmOption: (PendingOption, VoiceIntentResult.AmbiguousIntent) -> Unit,
    onConfirmNewPerson: (candidateName: String, amount: Double?, type: com.example.data.model.TransactionType?, direction: com.example.data.model.TransactionDirection?, rawSpeech: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = modifier.testTag("voice_input_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PolishBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = PolishNavyDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Hisaab Voice Assistant",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PolishNavyDark
                        )
                        Text(
                            text = "మాట్లాడండి • Speak to record ledger",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_voice_dialog_button")) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Language Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = "Language",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(18.dp)
                )

                FilterChip(
                    selected = selectedLanguage == "auto",
                    onClick = { onLanguageChange("auto") },
                    label = { Text("Auto Detect", fontSize = 12.sp) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PolishBluePrimary,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedLanguage == "te-IN",
                    onClick = { onLanguageChange("te-IN") },
                    label = { Text("తెలుగు (Telugu)", fontSize = 12.sp) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PolishBluePrimary,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedLanguage == "en-IN",
                    onClick = { onLanguageChange("en-IN") },
                    label = { Text("English", fontSize = 12.sp) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PolishBluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Animated Mic Button with Ripple & Sound Wave
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulse Animation when listening
                if (speechState == SpeechState.LISTENING) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1f,
                        targetValue = 1.35f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(PolishBlueContainer.copy(alpha = 0.6f))
                    )
                }

                // Main Central Button with halo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            when (speechState) {
                                SpeechState.LISTENING -> PolishBluePrimary
                                SpeechState.PROCESSING -> VoiceAmber
                                SpeechState.ERROR -> MaterialTheme.colorScheme.error
                                SpeechState.IDLE -> PolishBluePrimary
                            }
                        )
                        .clickable {
                            if (speechState == SpeechState.LISTENING) {
                                onStopListening()
                            } else {
                                onStartListening()
                            }
                        }
                        .testTag("voice_dialog_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    when (speechState) {
                        SpeechState.PROCESSING -> {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp
                            )
                        }
                        SpeechState.LISTENING -> {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Listening...",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Tap to Speak",
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }
            }

            // Status Label
            Text(
                text = when (speechState) {
                    SpeechState.LISTENING -> "వింటున్నాను... మాట్లాడండి (Listening... speak now)"
                    SpeechState.PROCESSING -> "విశ్లేషిస్తున్నాను... (Processing speech...)"
                    SpeechState.ERROR -> errorMessage ?: "Error in recognition"
                    SpeechState.IDLE -> "Tap mic or select an example phrase below"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (speechState == SpeechState.ERROR) MaterialTheme.colorScheme.error else PolishNavyDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Spoken text display box
            if (spokenText.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PolishBlueLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBlueContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = PolishBluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = spokenText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PolishNavyDark
                        )
                    }
                }
            }

            // Handle Ambiguous or New Person confirmations inside the dialog
            if (activeVoiceResult is VoiceIntentResult.AmbiguousIntent) {
                AmbiguousConfirmationCard(
                    result = activeVoiceResult,
                    onOptionSelected = { option -> onConfirmOption(option, activeVoiceResult) },
                    onDismiss = onDismiss
                )
            } else if (activeVoiceResult is VoiceIntentResult.PromptNewPerson) {
                NewPersonConfirmationCard(
                    result = activeVoiceResult,
                    onConfirm = onConfirmNewPerson,
                    onDismiss = onDismiss
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Example Phrases (Telugu & English)
            Text(
                text = "TRY SPEAKING OR TAP TO TEST:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val samplePhrasesTelugu = listOf(
                "సుల్తాన్ దగ్గర 2000 తీసుకున్నాను" to "Borrowed ₹2000",
                "రమేష్కి 500 ఇచ్చాను" to "Lent ₹500",
                "సుల్తాన్ 1000 తిరిగి ఇచ్చాడు" to "Repaid ₹1000",
                "సుల్తాన్ ఎంత బాకీ ఉన్నాడు?" to "Balance Check"
            )

            val samplePhrasesEnglish = listOf(
                "I took 2000 from Sulthan" to "Borrowed",
                "I gave 500 to Ramesh" to "Lent",
                "Sulthan paid back 1000" to "Repaid",
                "Show Ramesh balance" to "Balance Check"
            )

            // Scrollable chip rows for quick testing
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    samplePhrasesTelugu.forEach { (phrase, label) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PolishBlueLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBlueContainer),
                            modifier = Modifier
                                .clickable { onSimulatePhrase(phrase) }
                                .testTag("phrase_chip_${phrase.take(6)}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = phrase,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PolishNavyDark
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    samplePhrasesEnglish.forEach { (phrase, label) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSlate100),
                            modifier = Modifier
                                .clickable { onSimulatePhrase(phrase) }
                                .testTag("phrase_chip_${phrase.take(6)}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = phrase,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

