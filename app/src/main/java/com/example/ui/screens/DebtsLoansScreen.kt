package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebtLoanEntity
import com.example.ui.components.AddDebtLoanDialog
import com.example.ui.components.PayDebtDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.utils.CurrencyUtils

@Composable
fun DebtsLoansScreen(
    debtsLoans: List<DebtLoanEntity>,
    onAddDebtLoan: (name: String, type: String, amount: Double, dueDate: Long, note: String) -> Unit,
    onPayDebtLoan: (DebtLoanEntity, Double) -> Unit,
    onDeleteDebtLoan: (DebtLoanEntity) -> Unit
) {
    var selectedTab by remember { mutableStateOf("PIUTANG") } // "PIUTANG" or "HUTANG"
    var showAddDialog by remember { mutableStateOf(false) }
    var payingDebt by remember { mutableStateOf<DebtLoanEntity?>(null) }

    val filteredList = debtsLoans.filter { it.type == selectedTab }
    val totalRemaining = filteredList.sumOf { (it.totalAmount - it.paidAmount).coerceAtLeast(0.0) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_debt_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Catatan")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("debts_loans_screen")
        ) {
            // Tab Selector (Piutang vs Hutang)
            TabRow(
                selectedTabIndex = if (selectedTab == "PIUTANG") 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == "PIUTANG",
                    onClick = { selectedTab = "PIUTANG" },
                    text = {
                        Text(
                            text = "Piutang (Ditagih)",
                            fontWeight = if (selectedTab == "PIUTANG") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == "HUTANG",
                    onClick = { selectedTab = "HUTANG" },
                    text = {
                        Text(
                            text = "Hutang Saya (Dibayar)",
                            fontWeight = if (selectedTab == "HUTANG") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedTab == "PIUTANG")
                        IncomeGreen.copy(alpha = 0.12f)
                    else
                        ExpenseRed.copy(alpha = 0.12f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (selectedTab == "PIUTANG") "Total Piutang Belum Diterima" else "Total Hutang Harus Dibayar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyUtils.formatRupiah(totalRemaining),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == "PIUTANG") IncomeGreen else ExpenseRed
                    )
                }
            }

            // List of Debts/Loans
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (selectedTab == "PIUTANG") "Tidak ada catatan piutang aktif" else "Tidak ada catatan hutang aktif",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        DebtLoanCardItem(
                            item = item,
                            onPayClick = { payingDebt = item },
                            onDeleteClick = { onDeleteDebtLoan(item) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddDebtLoanDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, type, amount, due, note ->
                onAddDebtLoan(name, type, amount, due, note)
                showAddDialog = false
            }
        )
    }

    payingDebt?.let { debt ->
        PayDebtDialog(
            debt = debt,
            onDismiss = { payingDebt = null },
            onConfirmPayment = { amount ->
                onPayDebtLoan(debt, amount)
                payingDebt = null
            }
        )
    }
}

@Composable
fun DebtLoanCardItem(
    item: DebtLoanEntity,
    onPayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val remaining = (item.totalAmount - item.paidAmount).coerceAtLeast(0.0)
    val isPaid = item.isSettled || remaining <= 0.0
    val progress = (item.paidAmount / item.totalAmount).toFloat().coerceIn(0f, 1f)
    val isOverdue = System.currentTimeMillis() > item.dueDate && !isPaid

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.personName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isPaid) IncomeGreen.copy(alpha = 0.2f)
                    else if (isOverdue) ExpenseRed.copy(alpha = 0.2f)
                    else Color(0xFFF59E0B).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isPaid) "LUNAS" else if (isOverdue) "JATUH TEMPO" else "BELUM LUNAS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) IncomeGreen else if (isOverdue) ExpenseRed else Color(0xFFD97706),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Sisa Tagihan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(remaining),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (remaining > 0) MaterialTheme.colorScheme.onSurface else IncomeGreen
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Awal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyUtils.formatRupiah(item.totalAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress repayment bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (item.type == "PIUTANG") IncomeGreen else MaterialTheme.colorScheme.primary,
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Jatuh Tempo: ${CurrencyUtils.formatDate(item.dueDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverdue) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Terbayar: ${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            if (item.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Catatan: ${item.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!isPaid) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onPayClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (item.type == "PIUTANG") IncomeGreen else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (item.type == "PIUTANG") "Terima Cicilan" else "Bayar Cicilan")
                    }
                }
            }
        }
    }
}
