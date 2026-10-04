package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectedBankEntity
import com.example.data.model.TransactionEntity
import com.example.ui.CategoryExpense
import com.example.ui.FinanceSummary
import com.example.utils.CurrencyUtils
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    summary: FinanceSummary,
    recentTransactions: List<TransactionEntity>,
    categoryExpenses: List<CategoryExpense>,
    connectedBanks: List<ConnectedBankEntity> = emptyList(),
    onAddTransactionClick: () -> Unit,
    onScanReceiptClick: () -> Unit,
    onNavigateToDebts: () -> Unit,
    onNavigateToPlans: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToBanks: () -> Unit
) {
    var isBalanceVisible by remember { mutableStateOf(true) }
    var selectedTopSpendingTab by remember { mutableStateOf("Month") } // "Week" or "Month"

    // Default Wallets matching screenshot if database has none
    val displayWallets = if (connectedBanks.isNotEmpty()) {
        connectedBanks
    } else {
        listOf(
            ConnectedBankEntity(bankCode = "MBAKO", bankName = "Mbako", accountNumber = "Main", balance = 1542000.0),
            ConnectedBankEntity(bankCode = "SEABANK", bankName = "SeaBank", accountNumber = "Digital", balance = 0.0),
            ConnectedBankEntity(bankCode = "BRI", bankName = "Angsuran BRI", accountNumber = "Loan", balance = 0.0)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. TOP HEADER BAR: [Balance Eye] + [Search, Notifications]
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isBalanceVisible) {
                                val formatted = NumberFormat.getNumberInstance(Locale.US).format(summary.totalBalance.toLong().coerceAtLeast(0))
                                "Rp $formatted"
                            } else "Rp ••••••",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = { isBalanceVisible = !isBalanceVisible },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Balance",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { /* Info */ }
                    ) {
                        Text(
                            text = "Total balance",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8E93),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Help",
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Right Top Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateToTransactions) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    IconButton(onClick = { /* Notification center */ }) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // 2. MY WALLETS SECTION
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Wallets",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00C853),
                        modifier = Modifier.clickable { onNavigateToBanks() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        displayWallets.take(3).forEachIndexed { index, wallet ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToBanks() }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Circular Icon with small badge
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (wallet.bankName) {
                                                        "Mbako" -> Color(0xFFE65100)
                                                        "SeaBank" -> Color(0xFF424242)
                                                        else -> Color(0xFF616161)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (wallet.bankName) {
                                                    "Mbako" -> Icons.Default.AccountBalanceWallet
                                                    "SeaBank" -> Icons.Default.ShoppingBag
                                                    else -> Icons.Default.AccountBalance
                                                },
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        if (wallet.bankName != "Mbako") {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF2C2C2E)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AccountBalance,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = wallet.bankName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        if (wallet.bankName != "Mbako") {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.AccountBalance,
                                                contentDescription = null,
                                                tint = Color(0xFF8E8E93),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                val balFormatted = NumberFormat.getNumberInstance(Locale.US).format(wallet.balance.toLong())
                                Text(
                                    text = if (isBalanceVisible) "Rp $balFormatted" else "••••",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }

                            if (index < displayWallets.take(3).size - 1) {
                                HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.6.dp)
                            }
                        }
                    }
                }
            }
        }

        // 3. REPORT THIS MONTH SECTION (With Curved Line Chart & Income/Spent Split)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Report this month",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "See reports",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00C853),
                        modifier = Modifier.clickable { onNavigateToPlans() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Total Spent vs Total Income Columns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Total spent
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Total spent",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF8E8E93)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val spentFormatted = NumberFormat.getNumberInstance(Locale.US).format(summary.totalExpense.toLong())
                                Text(
                                    text = spentFormatted,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF5252)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(
                                    color = Color(0xFFFF5252),
                                    thickness = 2.dp,
                                    modifier = Modifier.fillMaxWidth(0.95f)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Total income
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Total income",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF8E8E93)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val incomeFormatted = NumberFormat.getNumberInstance(Locale.US).format(summary.totalIncome.toLong())
                                Text(
                                    text = incomeFormatted,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(
                                    color = Color(0xFF38BDF8),
                                    thickness = 2.dp,
                                    modifier = Modifier.fillMaxWidth(0.95f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Floating Badge: Previous 3-month average
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF2C2C2E))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Previous 3-month average",
                                        color = Color(0xFF8E8E93),
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "0",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Line Chart Canvas Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val gridColor = Color(0xFF424245)
                                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

                                // 4 Dashed horizontal grid lines (0, 3M, 6M, 9M)
                                val lineLevels = listOf(0.15f, 0.40f, 0.65f, 0.90f)
                                for (level in lineLevels) {
                                    val y = h * level
                                    drawLine(
                                        color = gridColor,
                                        start = Offset(0f, y),
                                        end = Offset(w - 50f, y),
                                        strokeWidth = 1f,
                                        pathEffect = dashEffect
                                    )
                                }

                                // Solid baseline at bottom
                                drawLine(
                                    color = Color(0xFFFF5252),
                                    start = Offset(0f, h * 0.90f),
                                    end = Offset(w - 70f, h * 0.90f),
                                    strokeWidth = 2f
                                )

                                // Trending Curved Line (White/Grey sloping upwards to current date)
                                val curvePath = Path().apply {
                                    moveTo(w * 0.70f, h * 0.90f)
                                    cubicTo(
                                        w * 0.78f, h * 0.90f,
                                        w * 0.80f, h * 0.35f,
                                        w * 0.83f, h * 0.20f
                                    )
                                }
                                drawPath(
                                    path = curvePath,
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = Stroke(width = 2.5f)
                                )

                                // Current milestone dot on chart
                                drawCircle(
                                    color = Color.White,
                                    radius = 5f,
                                    center = Offset(w * 0.80f, h * 0.90f)
                                )
                            }

                            // Y-axis labels on right side (0, 3M, 6M, 9M)
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("9 M", color = Color(0xFF8E8E93), fontSize = 10.sp)
                                Text("6 M", color = Color(0xFF8E8E93), fontSize = 10.sp)
                                Text("3 M", color = Color(0xFF8E8E93), fontSize = 10.sp)
                                Text("0", color = Color(0xFF8E8E93), fontSize = 10.sp)
                            }
                        }

                        // X-axis Dates: 01/10 and 31/10
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 40.dp, top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("01/10", color = Color(0xFF8E8E93), fontSize = 11.sp)
                            Text("31/10", color = Color(0xFF8E8E93), fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Chart Legend: Red dot This month, Grey dot Previous 3-month average ?
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF5252))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("This month", color = Color(0xFFB0B0B5), fontSize = 11.sp)

                            Spacer(modifier = Modifier.width(18.dp))

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF8E8E93))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Previous 3-month average", color = Color(0xFFB0B0B5), fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(11.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Pager Navigation: < Trending report > with 2 pagination dots
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous",
                                tint = Color(0xFF00C853),
                                modifier = Modifier.size(20.dp)
                            )

                            Text(
                                text = "Trending report",
                                color = Color(0xFF00C853),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next",
                                tint = Color(0xFF00C853),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 2 Pagination Dots
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF555558))
                            )
                        }
                    }
                }
            }
        }

        // 4. TOP SPENDING SECTION (Week/Month Pills + Sample Badge + Categories)
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top spending",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "See details",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00C853),
                        modifier = Modifier.clickable { onNavigateToTransactions() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Week | Month Pill Switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF28282B))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(if (selectedTopSpendingTab == "Week") Color(0xFF38383C) else Color.Transparent)
                                    .clickable { selectedTopSpendingTab = "Week" }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Week",
                                    color = if (selectedTopSpendingTab == "Week") Color.White else Color(0xFF8E8E93),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(if (selectedTopSpendingTab == "Month") Color(0xFF38383C) else Color.Transparent)
                                    .clickable { selectedTopSpendingTab = "Month" }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Month",
                                    color = if (selectedTopSpendingTab == "Month") Color.White else Color(0xFF8E8E93),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Green Pill Badge: "Sample"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF153325))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Sample",
                                color = Color(0xFF00C853),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Categories List: Food & Beverage (80%), Rental (15%), Shopping (5%)
                        TopSpendingRowItem(
                            icon = Icons.Default.Restaurant,
                            iconBgColor = Color(0xFF8B2635),
                            title = "Food & Beverage",
                            percentage = "80%"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TopSpendingRowItem(
                            icon = Icons.Default.Home,
                            iconBgColor = Color(0xFF37474F),
                            title = "Rental",
                            percentage = "15%"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TopSpendingRowItem(
                            icon = Icons.Default.ShoppingBasket,
                            iconBgColor = Color(0xFF264653),
                            title = "Shopping",
                            percentage = "5%"
                        )
                    }
                }
            }
        }

        // 5. RECENT TRANSACTIONS SECTION
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00C853),
                        modifier = Modifier.clickable { onNavigateToTransactions() }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        val transactionsToShow = if (recentTransactions.isNotEmpty()) {
                            recentTransactions.take(5)
                        } else {
                            listOf(
                                TransactionEntity(title = "Salary", amount = 160000.0, type = "INCOME", category = "Gaji", timestamp = System.currentTimeMillis()),
                                TransactionEntity(title = "Salary", amount = 300000.0, type = "INCOME", category = "Gaji", timestamp = System.currentTimeMillis() - 86400000L),
                                TransactionEntity(title = "tingwe", amount = 30000.0, type = "INCOME", category = "Belanja", timestamp = System.currentTimeMillis() - 86400000L)
                            )
                        }

                        transactionsToShow.forEachIndexed { index, tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Circular Banknote/Shopping icon with wallet badge
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (tx.title.contains("tingwe", ignoreCase = true)) Color(0xFFFBC02D)
                                                    else Color(0xFF2E7D32)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (tx.title.contains("tingwe", ignoreCase = true)) Icons.Default.ShoppingCart else Icons.Default.Payments,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }

                                        // Small orange wallet badge on bottom-right of icon
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFE65100)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Text(
                                            text = tx.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = CurrencyUtils.formatDate(tx.timestamp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF8E8E93)
                                        )
                                    }
                                }

                                val amountFormatted = NumberFormat.getNumberInstance(Locale.US).format(tx.amount.toLong())
                                Text(
                                    text = amountFormatted,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.type == "INCOME") Color(0xFF38BDF8) else Color(0xFFFF5252)
                                )
                            }

                            if (index < transactionsToShow.size - 1) {
                                HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.6.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopSpendingRowItem(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    percentage: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White.copy(0.9f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE65100)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = percentage,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFFF5252),
            fontWeight = FontWeight.Bold
        )
    }
}
