package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.FinancialDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        TransactionEntity::class,
        DebtLoanEntity::class,
        RecurringBillEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        ConnectedBankEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financialDao(): FinancialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "money_lover_db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial realistic starter data
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getDatabase(context).financialDao()
                            seedInitialData(dao)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(dao: FinancialDao) {
            val now = System.currentTimeMillis()
            val oneDay = 86400000L

            // Sample initial transactions matching Money Lover screenshot
            dao.insertTransaction(
                TransactionEntity(
                    title = "Salary",
                    amount = 160000.0,
                    type = "INCOME",
                    category = "Gaji",
                    account = "Mbako",
                    timestamp = now,
                    note = ""
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    title = "Salary",
                    amount = 300000.0,
                    type = "INCOME",
                    category = "Gaji",
                    account = "Mbako",
                    timestamp = now - oneDay,
                    note = ""
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    title = "tingwe",
                    amount = 30000.0,
                    type = "INCOME",
                    category = "Belanja",
                    account = "Mbako",
                    timestamp = now - oneDay,
                    note = "Gahan"
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    title = "tingwe",
                    amount = 24000.0,
                    type = "INCOME",
                    category = "Belanja",
                    account = "Mbako",
                    timestamp = now - oneDay,
                    note = "Jonet Suratno"
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    title = "tingwe",
                    amount = 154000.0,
                    type = "INCOME",
                    category = "Belanja",
                    account = "Mbako",
                    timestamp = now - oneDay,
                    note = "Mbako"
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    title = "Salary Transfer",
                    amount = 874000.0,
                    type = "INCOME",
                    category = "Gaji",
                    account = "Mbako",
                    timestamp = now - (oneDay * 2),
                    note = "Payroll transfer"
                )
            )

            // Transactions for LAST MONTH (September 2026)
            val calSep30 = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
                set(Calendar.DAY_OF_MONTH, 30)
            }
            dao.insertTransaction(
                TransactionEntity(
                    title = "Adjument",
                    amount = 9527000.0,
                    type = "EXPENSE",
                    category = "Adjument",
                    account = "Mbako",
                    timestamp = calSep30.timeInMillis,
                    note = ""
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    title = "tingwe",
                    amount = 34000.0,
                    type = "INCOME",
                    category = "tingwe",
                    account = "Mbako",
                    timestamp = calSep30.timeInMillis,
                    note = ""
                )
            )
            dao.insertTransaction(
                TransactionEntity(
                    title = "tingwe",
                    amount = 12000.0,
                    type = "INCOME",
                    category = "tingwe",
                    account = "Mbako",
                    timestamp = calSep30.timeInMillis,
                    note = "Qris gahan"
                )
            )
            val calSep29 = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
                set(Calendar.DAY_OF_MONTH, 29)
            }
            dao.insertTransaction(
                TransactionEntity(
                    title = "tingwe",
                    amount = 1907000.0,
                    type = "INCOME",
                    category = "tingwe",
                    account = "Mbako",
                    timestamp = calSep29.timeInMillis,
                    note = "tingwe"
                )
            )
            val calLastMonth = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
                set(Calendar.DAY_OF_MONTH, 25)
            }
            dao.insertTransaction(
                TransactionEntity(
                    title = "Salary",
                    amount = 2100000.0,
                    type = "INCOME",
                    category = "Salary",
                    account = "Mbako",
                    timestamp = calLastMonth.timeInMillis,
                    note = "Gaji Bulanan"
                )
            )
            val calLastMonthExpense = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
                set(Calendar.DAY_OF_MONTH, 18)
            }
            dao.insertTransaction(
                TransactionEntity(
                    title = "Belanja Supermarket",
                    amount = 450000.0,
                    type = "EXPENSE",
                    category = "Belanja",
                    account = "Mbako",
                    timestamp = calLastMonthExpense.timeInMillis,
                    note = "Kebutuhan dapur"
                )
            )
            val calLastMonthBill = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
                set(Calendar.DAY_OF_MONTH, 10)
            }
            dao.insertTransaction(
                TransactionEntity(
                    title = "Tagihan WiFi IndiHome",
                    amount = 350000.0,
                    type = "EXPENSE",
                    category = "Tagihan & Utilitas",
                    account = "Mbako",
                    timestamp = calLastMonthBill.timeInMillis,
                    note = "Paket internet bulanan"
                )
            )

            // Transactions for 2 MONTHS AGO (08/2026)
            val calTwoMonthsAgo = Calendar.getInstance().apply {
                add(Calendar.MONTH, -2)
                set(Calendar.DAY_OF_MONTH, 24)
            }
            dao.insertTransaction(
                TransactionEntity(
                    title = "Salary",
                    amount = 1950000.0,
                    type = "INCOME",
                    category = "Gaji",
                    account = "Mbako",
                    timestamp = calTwoMonthsAgo.timeInMillis,
                    note = "Gaji Agustus"
                )
            )
            val calTwoMonthsAgoExp = Calendar.getInstance().apply {
                add(Calendar.MONTH, -2)
                set(Calendar.DAY_OF_MONTH, 14)
            }
            dao.insertTransaction(
                TransactionEntity(
                    title = "Servis Motor Berkala",
                    amount = 230000.0,
                    type = "EXPENSE",
                    category = "Transportasi",
                    account = "Mbako",
                    timestamp = calTwoMonthsAgoExp.timeInMillis,
                    note = "Ganti oli dan rem"
                )
            )

            // Seed historical monthly transactions for the past 1 full year (up to 12 months ago)
            val pastMonthsData = listOf(
                Triple(-3, "Salary Juli", 1900000.0),
                Triple(-4, "Salary Juni", 1900000.0),
                Triple(-5, "THR & Salary Mei", 2800000.0),
                Triple(-6, "Salary April", 1850000.0),
                Triple(-7, "Salary Maret", 1850000.0),
                Triple(-8, "Salary Februari", 1800000.0),
                Triple(-9, "Salary & Bonus Januari", 2400000.0),
                Triple(-10, "Salary Desember", 1750000.0),
                Triple(-11, "Salary November", 1750000.0)
            )
            val pastMonthsExpenses = listOf(
                Triple(-3, "Liburan & Kuliner", 450000.0),
                Triple(-4, "Tiket Transportasi", 280000.0),
                Triple(-5, "Kebutuhan Hari Raya", 620000.0),
                Triple(-6, "Belanja Bulanan", 390000.0),
                Triple(-7, "Alat Elektronik", 310000.0),
                Triple(-8, "Pemeriksaan Kesehatan", 220000.0),
                Triple(-9, "Asuransi & Langganan", 350000.0),
                Triple(-10, "Kado Akhir Tahun", 290000.0),
                Triple(-11, "Pakaian & Jaket", 210000.0)
            )

            for (item in pastMonthsData) {
                val calM = Calendar.getInstance().apply {
                    add(Calendar.MONTH, item.first)
                    set(Calendar.DAY_OF_MONTH, 26)
                }
                dao.insertTransaction(
                    TransactionEntity(
                        title = item.second,
                        amount = item.third,
                        type = "INCOME",
                        category = "Gaji",
                        account = "Mbako",
                        timestamp = calM.timeInMillis,
                        note = "Payroll bulanan"
                    )
                )
            }

            for (item in pastMonthsExpenses) {
                val calM = Calendar.getInstance().apply {
                    add(Calendar.MONTH, item.first)
                    set(Calendar.DAY_OF_MONTH, 12)
                }
                dao.insertTransaction(
                    TransactionEntity(
                        title = item.second,
                        amount = item.third,
                        type = "EXPENSE",
                        category = "Belanja",
                        account = "Mbako",
                        timestamp = calM.timeInMillis,
                        note = "Pengeluaran rutin"
                    )
                )
            }

            // Transactions for FUTURE (Next Month)
            val calFuture = Calendar.getInstance().apply {
                add(Calendar.MONTH, 1)
                set(Calendar.DAY_OF_MONTH, 15)
            }
            dao.insertTransaction(
                TransactionEntity(
                    title = "Tagihan Asuransi & BPJS",
                    amount = 300000.0,
                    type = "EXPENSE",
                    category = "Tagihan & Utilitas",
                    account = "Mbako",
                    timestamp = calFuture.timeInMillis,
                    note = "Rencana pembayaran mendatang"
                )
            )
            val calFutureIncome = Calendar.getInstance().apply {
                add(Calendar.MONTH, 1)
                set(Calendar.DAY_OF_MONTH, 28)
            }
            dao.insertTransaction(
                TransactionEntity(
                    title = "Estimasi Gaji Masuk",
                    amount = 2500000.0,
                    type = "INCOME",
                    category = "Gaji",
                    account = "Mbako",
                    timestamp = calFutureIncome.timeInMillis,
                    note = "Jadwal payroll kantor"
                )
            )

            // Sample Budgets
            dao.insertBudget(
                BudgetEntity(
                    category = "Makanan & Minuman",
                    limitAmount = 2000000.0,
                    monthYear = "10-2026"
                )
            )
            dao.insertBudget(
                BudgetEntity(
                    category = "Belanja",
                    limitAmount = 1500000.0,
                    monthYear = "10-2026"
                )
            )
            dao.insertBudget(
                BudgetEntity(
                    category = "Transportasi",
                    limitAmount = 600000.0,
                    monthYear = "10-2026"
                )
            )

            // Sample Recurring Bills
            dao.insertRecurringBill(
                RecurringBillEntity(
                    name = "WiFi IndiHome & TV",
                    amount = 385000.0,
                    category = "Tagihan & Utilitas",
                    frequency = "BULANAN",
                    nextDueDate = now + (oneDay * 6),
                    isPaid = false
                )
            )
            dao.insertRecurringBill(
                RecurringBillEntity(
                    name = "Langganan Netflix Premium",
                    amount = 186000.0,
                    category = "Hiburan",
                    frequency = "BULANAN",
                    nextDueDate = now + (oneDay * 12),
                    isPaid = false
                )
            )

            // Sample Debts & Loans
            dao.insertDebtLoan(
                DebtLoanEntity(
                    personName = "Rudi (Teman Kantor)",
                    type = "PIUTANG",
                    totalAmount = 500000.0,
                    paidAmount = 150000.0,
                    dueDate = now + (oneDay * 10),
                    note = "Talangan tiket konser"
                )
            )
            dao.insertDebtLoan(
                DebtLoanEntity(
                    personName = "Om Danu",
                    type = "HUTANG",
                    totalAmount = 1000000.0,
                    paidAmount = 500000.0,
                    dueDate = now + (oneDay * 18),
                    note = "Pinjaman servis motor"
                )
            )

            // Sample Savings Goals
            dao.insertSavingsGoal(
                SavingsGoalEntity(
                    title = "Dana Liburan Akhir Tahun",
                    targetAmount = 5000000.0,
                    currentAmount = 2750000.0,
                    targetDate = now + (oneDay * 60),
                    colorHex = 0xFF00875A
                )
            )
            dao.insertSavingsGoal(
                SavingsGoalEntity(
                    title = "Beli Laptop Baru",
                    targetAmount = 15000000.0,
                    currentAmount = 8200000.0,
                    targetDate = now + (oneDay * 120),
                    colorHex = 0xFF0284C7
                )
            )

            // Wallets matching the user's Money Lover screenshot
            dao.insertConnectedBank(
                ConnectedBankEntity(
                    bankCode = "MBAKO",
                    bankName = "Mbako",
                    accountNumber = "Main Wallet",
                    balance = 1542000.0,
                    isConnected = true,
                    lastSynced = now
                )
            )
            dao.insertConnectedBank(
                ConnectedBankEntity(
                    bankCode = "SEABANK",
                    bankName = "SeaBank",
                    accountNumber = "Digital Bank",
                    balance = 0.0,
                    isConnected = true,
                    lastSynced = now
                )
            )
            dao.insertConnectedBank(
                ConnectedBankEntity(
                    bankCode = "BRI",
                    bankName = "Angsuran BRI",
                    accountNumber = "Angsuran Bank",
                    balance = 0.0,
                    isConnected = true,
                    lastSynced = now
                )
            )
        }
    }
}
