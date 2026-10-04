package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialDao {
    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(item: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(item: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(item: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    // Debts & Loans
    @Query("SELECT * FROM debts_loans ORDER BY dueDate ASC")
    fun getAllDebtsLoans(): Flow<List<DebtLoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtLoan(item: DebtLoanEntity): Long

    @Update
    suspend fun updateDebtLoan(item: DebtLoanEntity)

    @Delete
    suspend fun deleteDebtLoan(item: DebtLoanEntity)

    // Recurring Bills
    @Query("SELECT * FROM recurring_bills ORDER BY nextDueDate ASC")
    fun getAllRecurringBills(): Flow<List<RecurringBillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringBill(item: RecurringBillEntity): Long

    @Update
    suspend fun updateRecurringBill(item: RecurringBillEntity)

    @Delete
    suspend fun deleteRecurringBill(item: RecurringBillEntity)

    // Budgets
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(item: BudgetEntity): Long

    @Update
    suspend fun updateBudget(item: BudgetEntity)

    @Delete
    suspend fun deleteBudget(item: BudgetEntity)

    // Savings Goals
    @Query("SELECT * FROM savings_goals ORDER BY targetDate ASC")
    fun getAllSavingsGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(item: SavingsGoalEntity): Long

    @Update
    suspend fun updateSavingsGoal(item: SavingsGoalEntity)

    @Delete
    suspend fun deleteSavingsGoal(item: SavingsGoalEntity)

    // Connected Banks
    @Query("SELECT * FROM connected_banks")
    fun getAllConnectedBanks(): Flow<List<ConnectedBankEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnectedBank(item: ConnectedBankEntity): Long

    @Update
    suspend fun updateConnectedBank(item: ConnectedBankEntity)

    @Delete
    suspend fun deleteConnectedBank(item: ConnectedBankEntity)
}
