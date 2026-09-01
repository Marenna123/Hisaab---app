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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.data.model.DashboardSummary
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.CreditGreenLight
import com.example.ui.theme.DebitRed
import com.example.ui.theme.DebitRedLight
import com.example.ui.theme.PolishBluePrimary
import java.util.Locale

@Composable
fun SummaryStatsHeader(
    summary: DashboardSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("summary_stats_header"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = PolishBluePrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Main Top Bar in Hero Card: Total Receivable & Wallet Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total People Owe You",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${formatMoney(summary.totalReceivable)}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Total Balance",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Two Sub-cards: You Will Give & Net Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // You Will Give (Debit)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "You Will Give",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "You Will Give",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${formatMoney(summary.totalPayable)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Net Balance
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Net Balance",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (summary.netBalance >= 0) {
                                "+ ₹${formatMoney(summary.netBalance)}"
                            } else {
                                "- ₹${formatMoney(kotlin.math.abs(summary.netBalance))}"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (summary.netBalance >= 0) Color(0xFFD1FAE5) else Color(0xFFFFD1D1)
                        )
                    }
                }
            }
        }
    }
}

private fun formatMoney(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        amount.toLong().toString()
    } else {
        String.format(Locale.getDefault(), "%,.2f", amount)
    }
}

