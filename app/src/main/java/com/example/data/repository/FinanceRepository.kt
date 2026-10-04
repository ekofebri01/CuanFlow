package com.example.data.repository

import com.example.data.dao.FinancialDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val dao: FinancialDao) {
    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    suspend fun insertTransaction(item: TransactionEntity): Long = dao.insertTransaction(item)
    suspend fun updateTransaction(item: TransactionEntity) = dao.updateTransaction(item)
    suspend fun deleteTransaction(item: TransactionEntity) = dao.deleteTransaction(item)
    suspend fun deleteTransactionById(id: Long) = dao.deleteTransactionById(id)

    // Debts & Loans
    val allDebtsLoans: Flow<List<DebtLoanEntity>> = dao.getAllDebtsLoans()
    suspend fun insertDebtLoan(item: DebtLoanEntity): Long = dao.insertDebtLoan(item)
    suspend fun updateDebtLoan(item: DebtLoanEntity) = dao.updateDebtLoan(item)
    suspend fun deleteDebtLoan(item: DebtLoanEntity) = dao.deleteDebtLoan(item)

    // Recurring Bills
    val allRecurringBills: Flow<List<RecurringBillEntity>> = dao.getAllRecurringBills()
    suspend fun insertRecurringBill(item: RecurringBillEntity): Long = dao.insertRecurringBill(item)
    suspend fun updateRecurringBill(item: RecurringBillEntity) = dao.updateRecurringBill(item)
    suspend fun deleteRecurringBill(item: RecurringBillEntity) = dao.deleteRecurringBill(item)

    // Budgets
    val allBudgets: Flow<List<BudgetEntity>> = dao.getAllBudgets()
    suspend fun insertBudget(item: BudgetEntity): Long = dao.insertBudget(item)
    suspend fun updateBudget(item: BudgetEntity) = dao.updateBudget(item)
    suspend fun deleteBudget(item: BudgetEntity) = dao.deleteBudget(item)

    // Savings Goals
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = dao.getAllSavingsGoals()
    suspend fun insertSavingsGoal(item: SavingsGoalEntity): Long = dao.insertSavingsGoal(item)
    suspend fun updateSavingsGoal(item: SavingsGoalEntity) = dao.updateSavingsGoal(item)
    suspend fun deleteSavingsGoal(item: SavingsGoalEntity) = dao.deleteSavingsGoal(item)

    // Connected Banks
    val allConnectedBanks: Flow<List<ConnectedBankEntity>> = dao.getAllConnectedBanks()
    suspend fun insertConnectedBank(item: ConnectedBankEntity): Long = dao.insertConnectedBank(item)
    suspend fun updateConnectedBank(item: ConnectedBankEntity) = dao.updateConnectedBank(item)
    suspend fun deleteConnectedBank(item: ConnectedBankEntity) = dao.deleteConnectedBank(item)
}
