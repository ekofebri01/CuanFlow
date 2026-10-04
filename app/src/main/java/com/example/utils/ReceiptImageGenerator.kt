package com.example.utils

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object ReceiptImageGenerator {

    fun generateReceiptJpgUri(context: Context, tx: TransactionEntity): Uri? {
        val width = 800
        val height = 1100

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#121214")
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Card Container Background
        val cardRect = RectF(40f, 40f, width - 40f, height - 40f)
        val cardPaint = Paint().apply {
            color = Color.parseColor("#1E1E22")
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, cardPaint)

        // Card Border
        val borderPaint = Paint().apply {
            color = Color.parseColor("#2C2C32")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, borderPaint)

        // Top Accent Stripe
        val isIncome = tx.type == "INCOME"
        val accentColor = if (isIncome) Color.parseColor("#00C853") else Color.parseColor("#FF5252")
        val accentPaint = Paint().apply {
            color = accentColor
            isAntiAlias = true
        }
        val topPill = RectF(width / 2f - 120f, 65f, width / 2f + 120f, 105f)
        canvas.drawRoundRect(topPill, 20f, 20f, accentPaint)

        // Top Badge Text
        val badgeTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("BUKTI TRANSAKSI", width / 2f, 93f, badgeTextPaint)

        // Category Circle Background
        val iconCenterY = 190f
        val circlePaint = Paint().apply {
            color = if (isIncome) Color.parseColor("#1B5E20") else Color.parseColor("#B71C1C")
            isAntiAlias = true
        }
        canvas.drawCircle(width / 2f, iconCenterY, 50f, circlePaint)

        // Category Text
        val categoryPaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val displayCategory = tx.category.ifBlank { tx.title }
        canvas.drawText(displayCategory, width / 2f, 285f, categoryPaint)

        // Amount Text
        val amountPaint = Paint().apply {
            color = if (isIncome) Color.parseColor("#38BDF8") else Color.parseColor("#FF5252")
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val formattedAmount = "Rp ${NumberFormat.getNumberInstance(Locale.US).format(tx.amount.toLong())}"
        canvas.drawText(formattedAmount, width / 2f, 360f, amountPaint)

        // Divider Line
        val linePaint = Paint().apply {
            color = Color.parseColor("#33333A")
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawLine(80f, 410f, width - 80f, 410f, linePaint)

        // Details Section
        val labelPaint = Paint().apply {
            color = Color.parseColor("#8E8E93")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.WHITE
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        val sdfFull = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.ENGLISH)
        val dateStr = sdfFull.format(Date(tx.timestamp))

        var currentY = 470f
        val rowSpacing = 75f

        // Row 1: Tipe
        canvas.drawText("Tipe Transaksi", 80f, currentY, labelPaint)
        canvas.drawText(if (isIncome) "Pemasukan (+)" else "Pengeluaran (-)", width - 80f, currentY, valuePaint)

        currentY += rowSpacing
        canvas.drawLine(80f, currentY - 35f, width - 80f, currentY - 35f, linePaint)

        // Row 2: Tanggal
        canvas.drawText("Tanggal", 80f, currentY, labelPaint)
        canvas.drawText(dateStr, width - 80f, currentY, valuePaint)

        currentY += rowSpacing
        canvas.drawLine(80f, currentY - 35f, width - 80f, currentY - 35f, linePaint)

        // Row 3: Dompet / Akun
        canvas.drawText("Dompet / Akun", 80f, currentY, labelPaint)
        canvas.drawText(tx.account.ifBlank { "Mbako" }, width - 80f, currentY, valuePaint)

        currentY += rowSpacing
        canvas.drawLine(80f, currentY - 35f, width - 80f, currentY - 35f, linePaint)

        // Row 4: Catatan
        canvas.drawText("Catatan", 80f, currentY, labelPaint)
        val noteText = if (tx.note.isNotBlank()) tx.note else "-"
        canvas.drawText(noteText, width - 80f, currentY, valuePaint)

        currentY += rowSpacing
        canvas.drawLine(80f, currentY - 35f, width - 80f, currentY - 35f, linePaint)

        // Row 5: ID Transaksi
        canvas.drawText("Ref ID", 80f, currentY, labelPaint)
        canvas.drawText("#TX-${tx.id.toString().padStart(5, '0')}", width - 80f, currentY, valuePaint)

        // Footer Section
        val footerPaint = Paint().apply {
            color = Color.parseColor("#636366")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val sdfPrinted = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH)
        canvas.drawText("Disimpan pada: ${sdfPrinted.format(Date())}", width / 2f, height - 95f, footerPaint)
        canvas.drawText("• CuanFlow - Smart Financial Tracker •", width / 2f, height - 65f, footerPaint)

        // Save Bitmap to Cache Directory as JPG
        return try {
            val imagesFolder = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(imagesFolder, "receipt_${tx.id}_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            outputStream.flush()
            outputStream.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
