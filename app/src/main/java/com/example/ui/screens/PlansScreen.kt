package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.BudgetEntity
import com.example.data.model.RecurringBillEntity
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.AddBudgetDialog
import com.example.ui.components.AddRecurringBillDialog
import com.example.ui.components.AddSavingsGoalDialog
import com.example.ui.components.DepositWithdrawSavingsDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.utils.CurrencyUtils

@Composable
fun PlansScreen(
    budgets: List<BudgetEntity>,
    savingsGoals: List<SavingsGoalEntity>,
    recurringBills: List<RecurringBillEntity>,
    transactions: List<TransactionEntity>,
    onAddBudget: (category: String, limit: Double) -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit,
    onAddSavingsGoal: (title: String, targetAmount: Double, targetDate: Long) -> Unit,
    onDepositWithdrawSavings: (SavingsGoalEntity, Boolean, Double) -> Unit,
    onDeleteSavingsGoal: (SavingsGoalEntity) -> Unit,
    onAddRecurringBill: (name: String, amount: Double, category: String, frequency: String, dueDate: Long) -> Unit,
    onPayRecurringBill: (RecurringBillEntity) -> Unit,
    onDeleteRecurringBill: (RecurringBillEntity) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Anggaran, 1: Tabungan Target, 2: Tagihan Rutin
    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var showAddSavingsDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var managingSavingsGoal by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> showAddBudgetDialog = true
                        1 -> showAddSavingsDialog = true
                        2 -> showAddBillDialog = true
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("plans_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("plans_screen")
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Anggaran", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Tabungan", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Tagihan Rutin", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            when (selectedTab) {
                0 -> {
                    // Budgets Tab
                    BudgetsTabContent(
                        budgets = budgets,
                        transactions = transactions,
                        onDeleteBudget = onDeleteBudget
                    )
                }
                1 -> {
                    // Savings Goals Tab
                    SavingsTabContent(
                        savingsGoals = savingsGoals,
                        onManageSavings = { managingSavingsGoal = it },
                        onDeleteSavingsGoal = onDeleteSavingsGoal
                    )
                }
                2 -> {
                    // Recurring Bills Tab
                    RecurringBillsTabContent(
                        bills = recurringBills,
                        onPayBill = onPayRecurringBill,
                        onDeleteBill = onDeleteRecurringBill
                    )
                }
            }
        }
    }

    if (showAddBudgetDialog) {
        AddBudgetDialog(
            onDismiss = { showAddBudgetDialog = false },
            onSave = { cat, limit ->
                onAddBudget(cat, limit)
                showAddBudgetDialog = false
            }
        )
    }

    if (showAddSavingsDialog) {
        AddSavingsGoalDialog(
            onDismiss = { showAddSavingsDialog = false },
            onSave = { title, target, date ->
                onAddSavingsGoal(title, target, date)
                showAddSavingsDialog = false
            }
        )
    }

    if (showAddBillDialog) {
        AddRecurringBillDialog(
            onDismiss = { showAddBillDialog = false },
            onSave = { name, amount, cat, freq, due ->
                onAddRecurringBill(name, amount, cat, freq, due)
                showAddBillDialog = false
            }
        )
    }

    managingSavingsGoal?.let { goal ->
        DepositWithdrawSavingsDialog(
            goal = goal,
            onDismiss = { managingSavingsGoal = null },
            onAction = { isDeposit, amount ->
                onDepositWithdrawSavings(goal, isDeposit, amount)
                managingSavingsGoal = null
            }
        )
    }
}

@Composable
fun BudgetsTabContent(
    budgets: List<BudgetEntity>,
    transactions: List<TransactionEntity>,
    onDeleteBudget: (BudgetEntity) -> Unit
) {
    if (budgets.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Belum ada anggaran. Tekan tombol '+' untuk menetapkan batas anggaran belanja bulanan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(budgets, key = { it.id }) { b ->
                val spent = transactions
                    .filter { it.type == "EXPENSE" && it.category == b.category }
                    .sumOf { it.amount }
                val progress = (spent / b.limitAmount).toFloat().coerceIn(0f, 1f)
                val isOverbudget = spent > b.limitAmount

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
                                text = b.category,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { onDeleteBudget(b) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Terpakai: ${CurrencyUtils.formatRupiah(spent)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isOverbudget) ExpenseRed else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Batas: ${CurrencyUtils.formatRupiah(b.limitAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isOverbudget) ExpenseRed else if (progress > 0.8f) Color(0xFFF59E0B) else IncomeGreen,
                            trackColor = Color.LightGray.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val remaining = b.limitAmount - spent
                        Text(
                            text = if (isOverbudget) "⚠️ Melebihi Anggaran sebesar ${CurrencyUtils.formatRupiah(-remaining)}!" else "Sisa Anggaran: ${CurrencyUtils.formatRupiah(remaining)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverbudget) ExpenseRed else IncomeGreen
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SavingsTabContent(
    savingsGoals: List<SavingsGoalEntity>,
    onManageSavings: (SavingsGoalEntity) -> Unit,
    onDeleteSavingsGoal: (SavingsGoalEntity) -> Unit
) {
    if (savingsGoals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Belum ada target tabungan. Buat celengan impian Anda sekarang!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(savingsGoals, key = { it.id }) { goal ->
                val progress = (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                val isCompleted = goal.currentAmount >= goal.targetAmount

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
                                text = goal.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCompleted) IncomeGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = if (isCompleted) "TERCAPAI 🎉" else "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompleted) IncomeGreen else MaterialTheme.colorScheme.primary,
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
                                Text("Terkumpul", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(CurrencyUtils.formatRupiah(goal.currentAmount), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = IncomeGreen)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Target", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(CurrencyUtils.formatRupiah(goal.targetAmount), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = IncomeGreen,
                            trackColor = Color.LightGray.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Target Selesai: ${CurrencyUtils.formatDate(goal.targetDate)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onDeleteSavingsGoal(goal) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { onManageSavings(goal) }) {
                                Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Setor / Tarik")
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun RecurringBillsTabContent(
    bills: List<RecurringBillEntity>,
    onPayBill: (RecurringBillEntity) -> Unit,
    onDeleteBill: (RecurringBillEntity) -> Unit
) {
    if (bills.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Belum ada tagihan rutin. Catat pengeluaran berkala bulanan (WiFi, Netflix, Listrik) disini!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(bills, key = { it.id }) { bill ->
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
                            Column {
                                Text(bill.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${bill.frequency} • ${bill.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                text = CurrencyUtils.formatRupiah(bill.amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = ExpenseRed
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Jatuh Tempo: ${CurrencyUtils.formatDate(bill.nextDueDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row {
                                IconButton(onClick = { onDeleteBill(bill) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus")
                                }
                                Button(
                                    onClick = { onPayBill(bill) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Bayar Sekarang")
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
