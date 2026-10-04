package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.FinanceViewModel
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.ScanReceiptDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.ScannedReceiptData

enum class AppScreen {
    HOME,
    TRANSACTIONS,
    BUDGETS,
    ACCOUNT
}

class MainActivity : ComponentActivity() {
    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: FinanceViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    // Dialog States
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showScanReceiptDialog by remember { mutableStateOf(false) }

    // Pre-filled receipt data for transaction form
    var prefillTitle by remember { mutableStateOf("") }
    var prefillAmount by remember { mutableDoubleStateOf(0.0) }
    var prefillCategory by remember { mutableStateOf("") }

    // Collect StateFlows
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val debtsLoans by viewModel.debtsLoans.collectAsStateWithLifecycle()
    val recurringBills by viewModel.recurringBills.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val connectedBanks by viewModel.connectedBanks.collectAsStateWithLifecycle()

    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val categoryExpenses by viewModel.categoryBreakdown.collectAsStateWithLifecycle()
    val transactionFilter by viewModel.transactionFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isPremiumUnlocked by viewModel.isPremiumUnlocked.collectAsStateWithLifecycle()
    val isSyncingBank by viewModel.isSyncingBank.collectAsStateWithLifecycle()

    // BackHandler to return to Home from other tabs
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        currentScreen = AppScreen.HOME
    }

    Scaffold(
        containerColor = Color(0xFF000000),
        bottomBar = {
            Surface(
                color = Color(0xFF141416),
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Home
                    MoneyLoverNavItem(
                        icon = Icons.Default.Home,
                        label = "Home",
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { currentScreen = AppScreen.HOME }
                    )

                    // 2. Transactions
                    MoneyLoverNavItem(
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "Transactions",
                        selected = currentScreen == AppScreen.TRANSACTIONS,
                        onClick = { currentScreen = AppScreen.TRANSACTIONS }
                    )

                    // 3. Center Green Floating Plus Button (+)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00C853))
                            .clickable {
                                prefillTitle = ""
                                prefillAmount = 0.0
                                prefillCategory = ""
                                showAddTxDialog = true
                            }
                            .testTag("center_add_tx_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Transaction",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // 4. Budgets
                    MoneyLoverNavItem(
                        icon = Icons.Default.ReceiptLong,
                        label = "Budgets",
                        selected = currentScreen == AppScreen.BUDGETS,
                        onClick = { currentScreen = AppScreen.BUDGETS }
                    )

                    // 5. Account
                    MoneyLoverNavItem(
                        icon = Icons.Default.PersonOutline,
                        label = "Account",
                        selected = currentScreen == AppScreen.ACCOUNT,
                        onClick = { currentScreen = AppScreen.ACCOUNT }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF000000))
        ) {
            when (currentScreen) {
                AppScreen.HOME -> {
                    DashboardScreen(
                        summary = summary,
                        recentTransactions = transactions,
                        categoryExpenses = categoryExpenses,
                        connectedBanks = connectedBanks,
                        onAddTransactionClick = {
                            prefillTitle = ""
                            prefillAmount = 0.0
                            prefillCategory = ""
                            showAddTxDialog = true
                        },
                        onScanReceiptClick = { showScanReceiptDialog = true },
                        onNavigateToDebts = { currentScreen = AppScreen.BUDGETS },
                        onNavigateToPlans = { currentScreen = AppScreen.BUDGETS },
                        onNavigateToTransactions = { currentScreen = AppScreen.TRANSACTIONS },
                        onNavigateToBanks = { currentScreen = AppScreen.ACCOUNT }
                    )
                }
                AppScreen.TRANSACTIONS -> {
                    TransactionsScreen(
                        transactions = filteredTransactions,
                        selectedFilter = transactionFilter,
                        searchQuery = searchQuery,
                        onFilterChange = { viewModel.setTransactionFilter(it) },
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onDeleteTransaction = { viewModel.deleteTransaction(it) },
                        onUpdateTransaction = { viewModel.updateTransaction(it) },
                        onAddTransactionClick = {
                            prefillTitle = ""
                            prefillAmount = 0.0
                            prefillCategory = ""
                            showAddTxDialog = true
                        }
                    )
                }
                AppScreen.BUDGETS -> {
                    PlansScreen(
                        budgets = budgets,
                        savingsGoals = savingsGoals,
                        recurringBills = recurringBills,
                        transactions = transactions,
                        onAddBudget = { cat, limit -> viewModel.addBudget(cat, limit) },
                        onDeleteBudget = { viewModel.deleteBudget(it) },
                        onAddSavingsGoal = { title, target, date -> viewModel.addSavingsGoal(title, target, date) },
                        onDepositWithdrawSavings = { goal, isDeposit, amount ->
                            if (isDeposit) viewModel.depositToSavings(goal, amount)
                            else viewModel.withdrawFromSavings(goal, amount)
                        },
                        onDeleteSavingsGoal = { viewModel.deleteSavingsGoal(it) },
                        onAddRecurringBill = { name, amount, cat, freq, due ->
                            viewModel.addRecurringBill(name, amount, cat, freq, due)
                        },
                        onPayRecurringBill = { viewModel.payRecurringBill(it) },
                        onDeleteRecurringBill = { viewModel.deleteRecurringBill(it) }
                    )
                }
                AppScreen.ACCOUNT -> {
                    BankAndToolsScreen(
                        connectedBanks = connectedBanks,
                        isSyncingBank = isSyncingBank,
                        isPremiumUnlocked = isPremiumUnlocked,
                        transactions = transactions,
                        debts = debtsLoans,
                        onTogglePremium = { viewModel.togglePremium() },
                        onSyncBanks = { viewModel.syncAllBanks() },
                        onConnectBank = { code, name, acc, balance ->
                            viewModel.connectNewBank(code, name, acc, balance)
                        },
                        onDisconnectBank = { viewModel.disconnectBank(it) },
                        onOpenReceiptScanner = { showScanReceiptDialog = true }
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (showAddTxDialog) {
        AddTransactionDialog(
            initialType = "EXPENSE",
            initialTitle = prefillTitle,
            initialAmount = prefillAmount,
            initialCategory = prefillCategory,
            onDismiss = { showAddTxDialog = false },
            onSave = { title, amount, type, category, account, note ->
                viewModel.addTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    account = account,
                    date = System.currentTimeMillis(),
                    note = note
                )
                showAddTxDialog = false
            },
            onOpenReceiptScanner = {
                showAddTxDialog = false
                showScanReceiptDialog = true
            }
        )
    }

    if (showScanReceiptDialog) {
        ScanReceiptDialog(
            onDismiss = { showScanReceiptDialog = false },
            onApplyReceiptData = { data: ScannedReceiptData ->
                showScanReceiptDialog = false
                prefillTitle = data.merchant
                prefillAmount = data.amount
                prefillCategory = data.category
                showAddTxDialog = true
            }
        )
    }
}

@Composable
fun MoneyLoverNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else Color(0xFF7A7A80),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (selected) Color.White else Color(0xFF7A7A80),
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
