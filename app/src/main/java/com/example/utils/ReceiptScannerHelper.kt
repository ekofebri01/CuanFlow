package com.example.utils

import android.net.Uri
import kotlin.random.Random

data class ScannedReceiptData(
    val merchant: String,
    val amount: Double,
    val category: String,
    val items: List<String>,
    val date: Long = System.currentTimeMillis(),
    val rawText: String
)

object ReceiptScannerHelper {
    private val sampleReceipts = listOf(
        ScannedReceiptData(
            merchant = "INDOMARET POINT",
            amount = 64500.0,
            category = "Belanja",
            items = listOf("Roti Tawar Gandum", "Susu UHT Full Cream 1L", "Air Mineral 600ml"),
            rawText = """
                INDOMARET POINT SUDIRMAN
                JL. JEND. SUDIRMAN KAV 21
                ================================
                ROTI GANDUM           22.500
                SUSU UHT 1L           28.000
                AIR MINERAL 600ML     14.000
                --------------------------------
                TOTAL                Rp 64.500
                TUNAI                Rp 100.000
                KEMBALI              Rp 35.500
                Terima kasih atas kunjungan Anda
            """.trimIndent()
        ),
        ScannedReceiptData(
            merchant = "STARBUCKS COFFEE",
            amount = 78000.0,
            category = "Makanan & Minuman",
            items = listOf("Caffe Latte Grande", "Croissant Butter"),
            rawText = """
                STARBUCKS GRAND INDONESIA
                Order #0492
                ================================
                1 CAFFE LATTE (G)      53.000
                1 CROISSANT BUTTER     25.000
                --------------------------------
                SUBTOTAL               78.000
                TAX 10%                INCLUDED
                TOTAL                  Rp 78.000
                PAID VIA GOPAY
            """.trimIndent()
        ),
        ScannedReceiptData(
            merchant = "SPBU PERTAMINA 31.129",
            amount = 150000.0,
            category = "Transportasi",
            items = listOf("Pertamax Turbo 11.5L"),
            rawText = """
                SPBU PERTAMINA 31.129.02
                KUNINGAN, JAKARTA SELATAN
                ================================
                PRODUK : PERTAMAX TURBO
                VOLUME : 11.54 LITER
                HARGA/L : Rp 13.000
                --------------------------------
                TOTAL BAYAR : Rp 150.000
                METODE: BCA QRIS
            """.trimIndent()
        ),
        ScannedReceiptData(
            merchant = "APOTEK K-24 FARMA",
            amount = 92000.0,
            category = "Kesehatan",
            items = listOf("Vitamin C 1000mg", "Minyak Kayu Putih", "Plester Luka"),
            rawText = """
                APOTEK K-24 TEBET
                NO STRUK: AP-88291
                ================================
                VITAMIN C 1000MG       45.000
                MINYAK KAYU PUTIH      32.000
                PLESTER STERIL (10)    15.000
                --------------------------------
                TOTAL TAGIHAN : Rp 92.000
                LUNAS
            """.trimIndent()
        ),
        ScannedReceiptData(
            merchant = "RESTORAN PADANG SEDAP",
            amount = 58000.0,
            category = "Makanan & Minuman",
            items = listOf("Nasi Rendang Sapi", "Es Teh Manis", "Perkedel Kentang"),
            rawText = """
                RM PADANG SEDAP MANIS
                ================================
                NASI RENDANG SPESIAL   38.000
                PERKEDEL KENTANG       12.000
                ES TEH MANIS            8.000
                --------------------------------
                TOTAL                  Rp 58.000
                CASH PAYMENT
            """.trimIndent()
        )
    )

    fun analyzeReceipt(uri: Uri?): ScannedReceiptData {
        // Simulates realistic computer vision & OCR text parsing from image
        val index = Random.nextInt(sampleReceipts.size)
        return sampleReceipts[index].copy(date = System.currentTimeMillis())
    }
}
