package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategoryExpense(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class FinanceSummary(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalDebt: Double = 0.0, // yang harus dibayar
    val totalLoan: Double = 0.0  // yang harus ditagih (piutang)
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository

    val transactions: StateFlow<List<TransactionEntity>>
    val debtsLoans: StateFlow<List<DebtLoanEntity>>
    val recurringBills: StateFlow<List<RecurringBillEntity>>
    val budgets: StateFlow<List<BudgetEntity>>
    val savingsGoals: StateFlow<List<SavingsGoalEntity>>
    val connectedBanks: StateFlow<List<ConnectedBankEntity>>

    // Selected Transaction Filter
    private val _transactionFilter = MutableStateFlow("SEMUA") // "SEMUA", "EXPENSE", "INCOME"
    val transactionFilter = _transactionFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Premium Subscription Status for Bank Sync
    private val _isPremiumUnlocked = MutableStateFlow(true) // Set true by default so user can test open banking!
    val isPremiumUnlocked = _isPremiumUnlocked.asStateFlow()

    // Bank Sync in progress state
    private val _isSyncingBank = MutableStateFlow(false)
    val isSyncingBank = _isSyncingBank.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).financialDao()
        repository = FinanceRepository(dao)

        transactions = repository.allTransactions.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        debtsLoans = repository.allDebtsLoans.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        recurringBills = repository.allRecurringBills.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        budgets = repository.allBudgets.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        savingsGoals = repository.allSavingsGoals.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
        connectedBanks = repository.allConnectedBanks.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )
    }

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        transactions, _transactionFilter, _searchQuery
    ) { list, filter, query ->
        list.filter { item ->
            val matchesFilter = when (filter) {
                "EXPENSE" -> item.type == "EXPENSE"
                "INCOME" -> item.type == "INCOME"
                else -> true
            }
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true) ||
                    item.note.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary Totals
    val summary: StateFlow<FinanceSummary> = combine(
        transactions, debtsLoans
    ) { txList, debtList ->
        var income = 0.0
        var expense = 0.0
        for (tx in txList) {
            if (tx.type == "INCOME") income += tx.amount
            else if (tx.type == "EXPENSE") expense += tx.amount
        }

        var debtToPay = 0.0
        var loanToCollect = 0.0
        for (d in debtList) {
            val remaining = (d.totalAmount - d.paidAmount).coerceAtLeast(0.0)
            if (d.type == "HUTANG") {
                debtToPay += remaining
            } else {
                loanToCollect += remaining
            }
        }

        FinanceSummary(
            totalBalance = income - expense,
            totalIncome = income,
            totalExpense = expense,
            totalDebt = debtToPay,
            totalLoan = loanToCollect
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinanceSummary())

    // Category Breakdown for Charts
    val categoryBreakdown: StateFlow<List<CategoryExpense>> = transactions.map { list ->
        val expenseList = list.filter { it.type == "EXPENSE" }
        val totalExp = expenseList.sumOf { it.amount }
        if (totalExp <= 0.0) {
            emptyList()
        } else {
            expenseList.groupBy { it.category }
                .map { (cat, items) ->
                    val sum = items.sumOf { it.amount }
                    CategoryExpense(
                        category = cat,
                        amount = sum,
                        percentage = ((sum / totalExp) * 100).toFloat()
                    )
                }
                .sortedByDescending { it.amount }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTransactionFilter(filter: String) {
        _transactionFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun togglePremium() {
        _isPremiumUnlocked.value = !_isPremiumUnlocked.value
    }

    // Transaction Actions
    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        account: String,
        date: Long,
        note: String,
        receiptUri: String? = null
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    account = account,
                    timestamp = date,
                    note = note,
                    receiptUri = receiptUri
                )
            )
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    fun updateTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(tx)
        }
    }

    // Debts & Loans Actions
    fun addDebtLoan(
        personName: String,
        type: String,
        totalAmount: Double,
        paidAmount: Double,
        dueDate: Long,
        note: String
    ) {
        viewModelScope.launch {
            repository.insertDebtLoan(
                DebtLoanEntity(
                    personName = personName,
                    type = type,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    dueDate = dueDate,
                    note = note,
                    isSettled = paidAmount >= totalAmount
                )
            )
        }
    }

    fun payDebtLoan(debt: DebtLoanEntity, paymentAmount: Double) {
        viewModelScope.launch {
            val newPaid = debt.paidAmount + paymentAmount
            val isSettled = newPaid >= debt.totalAmount
            repository.updateDebtLoan(
                debt.copy(
                    paidAmount = newPaid,
                    isSettled = isSettled
                )
            )
            // Also log transaction for cash flow!
            val txType = if (debt.type == "HUTANG") "EXPENSE" else "INCOME"
            val txTitle = if (debt.type == "HUTANG") "Bayar Hutang: ${debt.personName}" else "Terima Piutang: ${debt.personName}"
            repository.insertTransaction(
                TransactionEntity(
                    title = txTitle,
                    amount = paymentAmount,
                    type = txType,
                    category = "Hutang/Piutang",
                    account = "Dompet Tunai",
                    timestamp = System.currentTimeMillis(),
                    note = "Pelunasan sebesar $paymentAmount"
                )
            )
        }
    }

    fun deleteDebtLoan(debt: DebtLoanEntity) {
        viewModelScope.launch {
            repository.deleteDebtLoan(debt)
        }
    }

    // Recurring Bills
    fun addRecurringBill(
        name: String,
        amount: Double,
        category: String,
        frequency: String,
        dueDate: Long
    ) {
        viewModelScope.launch {
            repository.insertRecurringBill(
                RecurringBillEntity(
                    name = name,
                    amount = amount,
                    category = category,
                    frequency = frequency,
                    nextDueDate = dueDate,
                    isPaid = false
                )
            )
        }
    }

    fun payRecurringBill(bill: RecurringBillEntity) {
        viewModelScope.launch {
            // Record as expense transaction
            repository.insertTransaction(
                TransactionEntity(
                    title = "Tagihan: ${bill.name}",
                    amount = bill.amount,
                    type = "EXPENSE",
                    category = bill.category,
                    account = "BCA",
                    timestamp = System.currentTimeMillis(),
                    note = "Pembayaran tagihan rutin (${bill.frequency})"
                )
            )

            // Shift due date to next cycle
            val cal = Calendar.getInstance().apply { timeInMillis = bill.nextDueDate }
            when (bill.frequency) {
                "MINGGUAN" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                "TAHUNAN" -> cal.add(Calendar.YEAR, 1)
                else -> cal.add(Calendar.MONTH, 1)
            }
            repository.updateRecurringBill(
                bill.copy(
                    nextDueDate = cal.timeInMillis,
                    isPaid = true
                )
            )
        }
    }

    fun deleteRecurringBill(bill: RecurringBillEntity) {
        viewModelScope.launch {
            repository.deleteRecurringBill(bill)
        }
    }

    // Budgets
    fun addBudget(category: String, limit: Double) {
        viewModelScope.launch {
            repository.insertBudget(
                BudgetEntity(
                    category = category,
                    limitAmount = limit,
                    monthYear = "10-2026"
                )
            )
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    // Savings Goals
    fun addSavingsGoal(title: String, targetAmount: Double, targetDate: Long) {
        viewModelScope.launch {
            repository.insertSavingsGoal(
                SavingsGoalEntity(
                    title = title,
                    targetAmount = targetAmount,
                    currentAmount = 0.0,
                    targetDate = targetDate
                )
            )
        }
    }

    fun depositToSavings(goal: SavingsGoalEntity, amount: Double) {
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = goal.currentAmount + amount)
            repository.updateSavingsGoal(updated)
            // Log as savings expense/allocation
            repository.insertTransaction(
                TransactionEntity(
                    title = "Nabung: ${goal.title}",
                    amount = amount,
                    type = "EXPENSE",
                    category = "Tabungan",
                    account = "Dompet Tunai",
                    timestamp = System.currentTimeMillis(),
                    note = "Setoran celengan target"
                )
            )
        }
    }

    fun withdrawFromSavings(goal: SavingsGoalEntity, amount: Double) {
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = (goal.currentAmount - amount).coerceAtLeast(0.0))
            repository.updateSavingsGoal(updated)
            // Log as income/return to wallet
            repository.insertTransaction(
                TransactionEntity(
                    title = "Tarik Tabungan: ${goal.title}",
                    amount = amount,
                    type = "INCOME",
                    category = "Tabungan",
                    account = "Dompet Tunai",
                    timestamp = System.currentTimeMillis(),
                    note = "Penarikan dari target tabungan"
                )
            )
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
        }
    }

    // Connected Banks (Open Banking)
    fun connectNewBank(bankCode: String, bankName: String, accountNumber: String, initialBalance: Double) {
        viewModelScope.launch {
            repository.insertConnectedBank(
                ConnectedBankEntity(
                    bankCode = bankCode,
                    bankName = bankName,
                    accountNumber = accountNumber,
                    balance = initialBalance,
                    isConnected = true,
                    lastSynced = System.currentTimeMillis()
                )
            )
        }
    }

    fun syncAllBanks() {
        viewModelScope.launch {
            _isSyncingBank.value = true
            kotlinx.coroutines.delay(1800) // realistic network sync simulation
            val currentList = connectedBanks.value
            for (bank in currentList) {
                repository.updateConnectedBank(
                    bank.copy(lastSynced = System.currentTimeMillis())
                )
            }
            _isSyncingBank.value = false
        }
    }

    fun disconnectBank(bank: ConnectedBankEntity) {
        viewModelScope.launch {
            repository.deleteConnectedBank(bank)
        }
    }
}
