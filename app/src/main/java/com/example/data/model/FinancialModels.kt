package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String,
    val account: String = "Dompet Tunai",
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val receiptUri: String? = null
)

@Entity(tableName = "debts_loans")
data class DebtLoanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personName: String,
    val type: String, // "HUTANG" (Saya berhutang) or "PIUTANG" (Orang hutang ke saya)
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val dueDate: Long,
    val note: String = "",
    val isSettled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recurring_bills")
data class RecurringBillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val category: String,
    val frequency: String = "BULANAN", // "BULANAN", "MINGGUAN", "TAHUNAN"
    val nextDueDate: Long,
    val isPaid: Boolean = false
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val limitAmount: Double,
    val monthYear: String // e.g. "10-2026"
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: Long,
    val colorHex: Long = 0xFF00875A
)

@Entity(tableName = "connected_banks")
data class ConnectedBankEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankCode: String,
    val bankName: String,
    val accountNumber: String,
    val balance: Double,
    val isConnected: Boolean = true,
    val lastSynced: Long = System.currentTimeMillis()
)
