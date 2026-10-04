package com.example.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.DebtLoanEntity
import com.example.data.model.TransactionEntity
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyUtils {
    private val indonesianLocale = Locale("id", "ID")
    private val rupiahFormat = NumberFormat.getCurrencyInstance(indonesianLocale).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun formatRupiah(amount: Double): String {
        return rupiahFormat.format(amount).replace("Rp", "Rp ")
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatMonthYear(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMMM yyyy", indonesianLocale)
        return sdf.format(Date(timestamp))
    }

    fun generateTransactionsCsv(
        transactions: List<TransactionEntity>,
        debts: List<DebtLoanEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("ID,Tanggal,Tipe,Kategori,Akun,Nominal,Catatan\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        for (t in transactions) {
            val dateStr = sdf.format(Date(t.timestamp))
            val safeNote = t.note.replace(",", " ")
            val safeTitle = t.title.replace(",", " ")
            sb.append("${t.id},\"$dateStr\",\"${t.type}\",\"${t.category}\",\"${t.account}\",${t.amount},\"$safeTitle - $safeNote\"\n")
        }

        sb.append("\n--- DAFTAR HUTANG & PIUTANG ---\n")
        sb.append("ID,Nama,Tipe,Total,Terbayar,Sisa,Jatuh Tempo,Status\n")
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (d in debts) {
            val dueStr = dateSdf.format(Date(d.dueDate))
            val remaining = (d.totalAmount - d.paidAmount).coerceAtLeast(0.0)
            val status = if (d.isSettled || remaining <= 0.0) "LUNAS" else "BELUM LUNAS"
            sb.append("${d.id},\"${d.personName}\",\"${d.type}\",${d.totalAmount},${d.paidAmount},$remaining,\"$dueStr\",\"$status\"\n")
        }

        return sb.toString()
    }

    fun shareCsvData(context: Context, csvContent: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csvContent)
            putExtra(Intent.EXTRA_SUBJECT, "Laporan Keuangan Money Lover - ${formatDate(System.currentTimeMillis())}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Ekspor Laporan Keuangan CSV")
        context.startActivity(shareIntent)
    }
}
