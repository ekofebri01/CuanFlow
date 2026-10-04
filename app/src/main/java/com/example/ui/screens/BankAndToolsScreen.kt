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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectedBankEntity
import com.example.data.model.DebtLoanEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.ConnectBankDialog
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IncomeGreen
import com.example.utils.CurrencyUtils

@Composable
fun BankAndToolsScreen(
    connectedBanks: List<ConnectedBankEntity>,
    isSyncingBank: Boolean,
    isPremiumUnlocked: Boolean,
    transactions: List<TransactionEntity>,
    debts: List<DebtLoanEntity>,
    onTogglePremium: () -> Unit,
    onSyncBanks: () -> Unit,
    onConnectBank: (code: String, name: String, accNumber: String, balance: Double) -> Unit,
    onDisconnectBank: (ConnectedBankEntity) -> Unit,
    onOpenReceiptScanner: () -> Unit
) {
    val context = LocalContext.current
    var showConnectDialog by remember { mutableStateOf(false) }
    var showExportConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bank_tools_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Premium Open Banking Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FITUR PREMIUM",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = Color(0xFFB45309)
                                )
                                Text(
                                    text = "Hubungkan Rekening Bank",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        AssistChip(
                            onClick = onTogglePremium,
                            label = {
                                Text(if (isPremiumUnlocked) "AKTIF" else "UPGRADE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isPremiumUnlocked) IncomeGreen else Color(0xFFF59E0B),
                                labelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Sinkronisasi saldo dan mutasi rekening Bank BCA, Mandiri, BRI, BNI, serta e-wallet GoPay/OVO secara otomatis dengan keamanan Open Banking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { showConnectDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("connect_bank_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hubungkan Bank", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onSyncBanks,
                            enabled = !isSyncingBank && connectedBanks.isNotEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sync_banks_btn")
                        ) {
                            if (isSyncingBank) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sinkron...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sinkronkan", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section: Rekening Terhubung
        item {
            Text(
                text = "Rekening & E-Wallet Terhubung (${connectedBanks.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (connectedBanks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Belum ada rekening terhubung. Tekan 'Hubungkan Bank' untuk menambahkan akun.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(connectedBanks, key = { it.id }) { bank ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = EmeraldPrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(bank.bankName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("No. Rek: ${bank.accountNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Saldo: ${CurrencyUtils.formatRupiah(bank.balance)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = IncomeGreen)
                            }
                        }

                        IconButton(onClick = { onDisconnectBank(bank) }) {
                            Icon(Icons.Default.LinkOff, contentDescription = "Putuskan", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Section: Alat Keuangan (Scan & Export)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Alat & Ekspor Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Scan Receipt Tool Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = Color(0xFF0284C7))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Scan Kwitansi / Struk", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Deteksi otomatis total nominal & rincian nota belanja", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Button(
                        onClick = onOpenReceiptScanner,
                        modifier = Modifier.testTag("scan_receipt_tool_btn")
                    ) {
                        Text("Scan")
                    }
                }
            }
        }

        // Export CSV / Excel Tool Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF10B981))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Ekspor ke CSV / Excel", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Unduh seluruh data transaksi & hutang piutang", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Button(
                        onClick = { showExportConfirmDialog = true },
                        modifier = Modifier.testTag("export_csv_btn")
                    ) {
                        Text("Ekspor")
                    }
                }
            }
        }
    }

    if (showConnectDialog) {
        ConnectBankDialog(
            onDismiss = { showConnectDialog = false },
            onSave = { code, name, acc, balance ->
                onConnectBank(code, name, acc, balance)
                showConnectDialog = false
            }
        )
    }

    if (showExportConfirmDialog) {
        val csv = CurrencyUtils.generateTransactionsCsv(transactions, debts)
        AlertDialog(
            onDismissRequest = { showExportConfirmDialog = false },
            title = { Text("Ekspor Laporan Keuangan", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Total data yang siap diekspor:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• ${transactions.size} Catatan Transaksi", fontWeight = FontWeight.Medium)
                    Text("• ${debts.size} Catatan Hutang/Piutang", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "File CSV dapat dibuka langsung menggunakan Microsoft Excel, Google Sheets, atau aplikasi spreadsheet lainnya.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        CurrencyUtils.shareCsvData(context, csv)
                        showExportConfirmDialog = false
                    }
                ) {
                    Text("Bagikan / Buka File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
