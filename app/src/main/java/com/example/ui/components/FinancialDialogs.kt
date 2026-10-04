package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DebtLoanEntity
import com.example.data.model.SavingsGoalEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.utils.CurrencyUtils
import com.example.utils.ReceiptScannerHelper
import com.example.utils.ScannedReceiptData

import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor

val DefaultCategoriesExpense = listOf(
    "Makanan & Minuman", "Belanja", "Transportasi", "Tagihan & Utilitas",
    "Hiburan", "Kesehatan", "Pendidikan", "Keluarga", "Lainnya"
)

val DefaultCategoriesIncome = listOf(
    "Gaji", "Bonus", "Investasi", "Penjualan", "Hadiah", "Lainnya"
)

val DefaultAccounts = listOf(
    "Mbako", "Dompet Tunai", "BCA", "Mandiri", "BRI", "GoPay", "OVO", "DANA"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    initialType: String = "EXPENSE",
    initialTitle: String = "",
    initialAmount: Double = 0.0,
    initialCategory: String = "",
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, type: String, category: String, account: String, note: String) -> Unit,
    onOpenReceiptScanner: () -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var selectedAccount by remember { mutableStateOf("Mbako") }
    var amountText by remember { mutableStateOf(if (initialAmount > 0) initialAmount.toLong().toString() else "0") }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var note by remember { mutableStateOf(initialTitle) }
    var dateText by remember { mutableStateOf("Today") }
    var withPerson by remember { mutableStateOf("") }
    var selectedEvent by remember { mutableStateOf("") }
    var reminderText by remember { mutableStateOf("No remind") }
    var excludeFromReport by remember { mutableStateOf(false) }
    var attachedReceiptUri by remember { mutableStateOf<Uri?>(null) }

    // Sub-dialog pickers
    var showAccountPicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showWithDialog by remember { mutableStateOf(false) }
    var showEventDialog by remember { mutableStateOf(false) }

    // Modern photo picker for receipt attachment
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedReceiptUri = uri
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0F0F10)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                // Top App Bar: [X] Add transaction
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_transaction_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Add transaction",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Scrollable Form Cards
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // MAIN CARD 1
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E20))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            // 1. Wallet Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAccountPicker = true }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(
                                    text = selectedAccount,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.8.dp)

                            // 2. Amount Row (IDR Badge + Green Number + Green Underline)
                            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // IDR Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF38383A))
                                            .padding(horizontal = 7.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "IDR",
                                            color = Color(0xFFA5A5AB),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(18.dp))

                                    // Editable Green Amount Input
                                    BasicTextField(
                                        value = if (amountText == "0") "" else amountText,
                                        onValueChange = { input ->
                                            val filtered = input.filter { it.isDigit() }
                                            amountText = if (filtered.isEmpty()) "0" else filtered
                                        },
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            color = Color(0xFF00C853),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        cursorBrush = SolidColor(Color(0xFF00C853)),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("transaction_amount_input"),
                                        decorationBox = { innerTextField ->
                                            if (amountText == "0" || amountText.isEmpty()) {
                                                Text(
                                                    text = "0",
                                                    color = Color(0xFF00C853),
                                                    fontSize = 32.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            innerTextField()
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Green Underline under the amount row
                                HorizontalDivider(
                                    color = Color(0xFF00C853),
                                    thickness = 2.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.8.dp)

                            // 3. Category Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCategoryPicker = true }
                                    .padding(vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (selectedCategory.isEmpty()) Color(0xFFD1D1D6) else IncomeGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedCategory.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(18.dp))
                                Text(
                                    text = if (selectedCategory.isEmpty()) "Select category" else selectedCategory,
                                    color = if (selectedCategory.isEmpty()) Color(0xFF8E8E93) else Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = if (selectedCategory.isEmpty()) FontWeight.Normal else FontWeight.Medium
                                )
                            }

                            HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.8.dp)

                            // 4. Note Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subject,
                                    contentDescription = "Note",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                                BasicTextField(
                                    value = note,
                                    onValueChange = { note = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = Color.White,
                                        fontSize = 16.sp
                                    ),
                                    cursorBrush = SolidColor(Color.White),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("transaction_title_input"),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (note.isEmpty()) {
                                            Text(
                                                text = "Write note",
                                                color = Color(0xFF8E8E93),
                                                fontSize = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }

                            HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.8.dp)

                            // 5. Date Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true }
                                    .padding(vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Date",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(
                                    text = dateText,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // CARD 2: "With"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E20))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showWithDialog = true }
                                .padding(horizontal = 16.dp, vertical = 18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "With",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(
                                text = if (withPerson.isEmpty()) "With" else "With $withPerson",
                                color = if (withPerson.isEmpty()) Color(0xFF8E8E93) else Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // CARD 3: "Select event" & "No remind"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E20))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showEventDialog = true }
                                    .padding(vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Work,
                                    contentDescription = "Event",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(
                                    text = if (selectedEvent.isEmpty()) "Select event" else selectedEvent,
                                    color = if (selectedEvent.isEmpty()) Color(0xFF8E8E93) else Color.White,
                                    fontSize = 16.sp
                                )
                            }

                            HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.8.dp)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        reminderText = if (reminderText == "No remind") "Remind in 1 hour" else "No remind"
                                    }
                                    .padding(vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Remind",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(
                                    text = reminderText,
                                    color = Color(0xFF8E8E93),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    // TWO ATTACHMENT BUTTONS (Gallery & Camera)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Gallery Button
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E20))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Attach Photo",
                                    tint = if (attachedReceiptUri != null) Color(0xFF00C853) else Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Camera / Scan Kwitansi Button
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clickable { onOpenReceiptScanner() },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E20))
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Scan Receipt",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    // CARD 4: Exclude from report
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E20))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Exclude",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(
                                    text = "Exclude from report",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }

                            Switch(
                                checked = excludeFromReport,
                                onCheckedChange = { excludeFromReport = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF00C853),
                                    uncheckedThumbColor = Color(0xFF8E8E93),
                                    uncheckedTrackColor = Color(0xFF2C2C2E)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // BOTTOM STICKY SAVE BUTTON
                Surface(
                    color = Color(0xFF0F0F10),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val isValidAmount = (amountText.toDoubleOrNull() ?: 0.0) > 0.0
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Button(
                            onClick = {
                                val amount = amountText.toDoubleOrNull() ?: 0.0
                                val finalCategory = if (selectedCategory.isNotBlank()) selectedCategory else "Makanan & Minuman"
                                val finalTitle = if (note.isNotBlank()) note else finalCategory
                                if (amount > 0) {
                                    onSave(finalTitle, amount, type, finalCategory, selectedAccount, note)
                                }
                            },
                            enabled = isValidAmount,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("save_transaction_btn"),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isValidAmount) Color(0xFF00C853) else Color(0xFF28282B),
                                disabledContainerColor = Color(0xFF28282B),
                                contentColor = Color.White,
                                disabledContentColor = Color(0xFF6E6E73)
                            )
                        ) {
                            Text(
                                text = "Save",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // 1. Account / Wallet Picker Dialog
    if (showAccountPicker) {
        AlertDialog(
            onDismissRequest = { showAccountPicker = false },
            title = { Text("Select wallet / account", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    DefaultAccounts.forEach { acc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAccount = acc
                                    showAccountPicker = false
                                }
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = acc, style = MaterialTheme.typography.bodyLarge, fontWeight = if (selectedAccount == acc) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 2. Category Picker Dialog (Expense & Income)
    if (showCategoryPicker) {
        var pickerTab by remember { mutableStateOf("EXPENSE") }
        val categoryList = if (pickerTab == "EXPENSE") DefaultCategoriesExpense else DefaultCategoriesIncome

        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    Button(
                        onClick = { pickerTab = "EXPENSE" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pickerTab == "EXPENSE") ExpenseRed else Color.Transparent,
                            contentColor = if (pickerTab == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(6.dp),
                        elevation = null
                    ) {
                        Text("Expense", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { pickerTab = "INCOME" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pickerTab == "INCOME") IncomeGreen else Color.Transparent,
                            contentColor = if (pickerTab == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(6.dp),
                        elevation = null
                    ) {
                        Text("Income", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    categoryList.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategory = cat
                                    type = pickerTab
                                    showCategoryPicker = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (pickerTab == "EXPENSE") ExpenseRed.copy(0.15f) else IncomeGreen.copy(0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (pickerTab == "EXPENSE") Icons.Default.ShoppingCart else Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = if (pickerTab == "EXPENSE") ExpenseRed else IncomeGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 3. Date Selection Dialog
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Date", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("Today", "Yesterday", "2 days ago", "Custom Date").forEach { d ->
                        Text(
                            text = d,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    dateText = d
                                    showDatePicker = false
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 4. "With" Person Input Dialog
    if (showWithDialog) {
        var personInput by remember { mutableStateOf(withPerson) }
        AlertDialog(
            onDismissRequest = { showWithDialog = false },
            title = { Text("With", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = personInput,
                    onValueChange = { personInput = it },
                    placeholder = { Text("Friend, Colleague, or Family name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    withPerson = personInput
                    showWithDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWithDialog = false }) { Text("Cancel") }
            }
        )
    }

    // 5. "Select Event" Dialog
    if (showEventDialog) {
        AlertDialog(
            onDismissRequest = { showEventDialog = false },
            title = { Text("Select Event", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("Holiday / Trip", "Wedding", "Birthday", "Office Project", "None").forEach { ev ->
                        Text(
                            text = ev,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedEvent = if (ev == "None") "" else ev
                                    showEventDialog = false
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun ScanReceiptDialog(
    onDismiss: () -> Unit,
    onApplyReceiptData: (ScannedReceiptData) -> Unit
) {
    var scannedData by remember { mutableStateOf<ScannedReceiptData?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    // Photo picker launcher (Zero-permission modern Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            isAnalyzing = true
            // Simulate OCR analysis
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                scannedData = ReceiptScannerHelper.analyzeReceipt(uri)
                isAnalyzing = false
            }, 1200)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DocumentScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Kwitansi / Struk Belanja", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (scannedData == null && !isAnalyzing) {
                    Text(
                        text = "Ambil foto atau pilih struk belanja dari galeri. Sistem OCR cerdas akan otomatis mendeteksi nama toko, total belanja, dan item struk.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pick_receipt_btn")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih Foto Struk / Kwitansi")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            isAnalyzing = true
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                scannedData = ReceiptScannerHelper.analyzeReceipt(null)
                                isAnalyzing = false
                            }, 1000)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scan_sample_receipt_btn")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Coba Scan Struk Otomatis (Demo)")
                    }
                } else if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                    Text(
                        text = "Menganalisis teks kwitansi & nominal...",
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    scannedData?.let { data ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "HASIL SCAN STRUK",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = data.merchant,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = CurrencyUtils.formatRupiah(data.amount),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ExpenseRed
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Kategori Terdeteksi: ${data.category}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )

                                if (data.items.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Item Belanja:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    data.items.forEach { item ->
                                        Text(
                                            text = "• $item",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Tekan 'Gunakan Data Ini' untuk langsung membuat catatan transaksi pengeluaran secara instan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (scannedData != null) {
                Button(
                    onClick = {
                        scannedData?.let { onApplyReceiptData(it) }
                    },
                    modifier = Modifier.testTag("apply_scanned_receipt_btn")
                ) {
                    Text("Gunakan Data Ini")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun AddDebtLoanDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, amount: Double, dueDate: Long, note: String) -> Unit
) {
    var type by remember { mutableStateOf("HUTANG") } // "HUTANG" or "PIUTANG"
    var personName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var daysToDue by remember { mutableStateOf("14") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (type == "HUTANG") "Catat Hutang Saya" else "Catat Piutang (Tagihan)",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    Button(
                        onClick = { type = "HUTANG" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "HUTANG") ExpenseRed else Color.Transparent,
                            contentColor = if (type == "HUTANG") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = null
                    ) {
                        Text("Hutang Saya", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { type = "PIUTANG" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "PIUTANG") IncomeGreen else Color.Transparent,
                            contentColor = if (type == "PIUTANG") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = null
                    ) {
                        Text("Piutang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text(if (type == "HUTANG") "Pemberi Pinjaman (Nama Orang/Bank)" else "Peminjam (Nama Orang)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amountText = it },
                    label = { Text("Total Nominal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = daysToDue,
                    onValueChange = { if (it.all { c -> c.isDigit() }) daysToDue = it },
                    label = { Text("Jatuh Tempo (Dalam Berapa Hari)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Keperluan / Catatan") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val days = daysToDue.toLongOrNull() ?: 14L
                    val dueTimestamp = System.currentTimeMillis() + (days * 86400000L)
                    if (personName.isNotBlank() && amount > 0) {
                        onSave(personName, type, amount, dueTimestamp, note)
                    }
                },
                enabled = personName.isNotBlank() && amountText.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun PayDebtDialog(
    debt: DebtLoanEntity,
    onDismiss: () -> Unit,
    onConfirmPayment: (amount: Double) -> Unit
) {
    val remaining = (debt.totalAmount - debt.paidAmount).coerceAtLeast(0.0)
    var amountText by remember { mutableStateOf(remaining.toLong().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (debt.type == "HUTANG") "Bayar Hutang ke ${debt.personName}" else "Catat Pelunasan dari ${debt.personName}",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Sisa yang harus diselesaikan: ${CurrencyUtils.formatRupiah(remaining)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amountText = it },
                    label = { Text("Nominal Pembayaran (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirmPayment(amount)
                    }
                }
            ) {
                Text("Konfirmasi Bayar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onSave: (category: String, limit: Double) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(DefaultCategoriesExpense.first()) }
    var limitText by remember { mutableStateOf("1500000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Anggaran Kategori", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Pilih Kategori Anggaran", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DefaultCategoriesExpense) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) limitText = it },
                    label = { Text("Batas Anggaran Bulanan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull() ?: 0.0
                    if (limit > 0) onSave(selectedCategory, limit)
                }
            ) {
                Text("Simpan Anggaran")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, targetAmount: Double, targetDate: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("5000000") }
    var monthsText by remember { mutableStateOf("6") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Celengan / Target Tabungan", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Target Tabungan") },
                    placeholder = { Text("cth: Liburan Bali, iPhone 17, Dana Darurat") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) targetText = it },
                    label = { Text("Target Nominal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = monthsText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) monthsText = it },
                    label = { Text("Target Selesai Dalam (Bulan)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull() ?: 0.0
                    val months = monthsText.toLongOrNull() ?: 6L
                    val targetTimestamp = System.currentTimeMillis() + (months * 30 * 86400000L)
                    if (title.isNotBlank() && target > 0) {
                        onSave(title, target, targetTimestamp)
                    }
                }
            ) {
                Text("Mulai Menabung")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun DepositWithdrawSavingsDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onAction: (isDeposit: Boolean, amount: Double) -> Unit
) {
    var isDeposit by remember { mutableStateOf(true) }
    var amountText by remember { mutableStateOf("100000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(goal.title, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Terkumpul saat ini: ${CurrencyUtils.formatRupiah(goal.currentAmount)} dari ${CurrencyUtils.formatRupiah(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { isDeposit = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDeposit) IncomeGreen else Color.LightGray.copy(alpha = 0.4f),
                            contentColor = if (isDeposit) Color.White else Color.Black
                        )
                    ) {
                        Text("+ Nabung")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { isDeposit = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isDeposit) ExpenseRed else Color.LightGray.copy(alpha = 0.4f),
                            contentColor = if (!isDeposit) Color.White else Color.Black
                        )
                    ) {
                        Text("- Tarik")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amountText = it },
                    label = { Text("Nominal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) onAction(isDeposit, amount)
                }
            ) {
                Text("Konfirmasi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun AddRecurringBillDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double, category: String, frequency: String, dueDate: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tagihan & Utilitas") }
    var selectedFrequency by remember { mutableStateOf("BULANAN") }
    val frequencies = listOf("BULANAN", "MINGGUAN", "TAHUNAN")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Buat Tagihan Rutin Berkala", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Tagihan / Langganan") },
                    placeholder = { Text("cth: Listrik PLN, WiFi IndiHome, Netflix, BPJS") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) amountText = it },
                    label = { Text("Nominal Tagihan (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Frekuensi Tagihan", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    frequencies.forEach { freq ->
                        FilterChip(
                            selected = selectedFrequency == freq,
                            onClick = { selectedFrequency = freq },
                            label = { Text(freq) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val due = System.currentTimeMillis() + (7 * 86400000L) // 7 days from now
                    if (name.isNotBlank() && amount > 0) {
                        onSave(name, amount, selectedCategory, selectedFrequency, due)
                    }
                }
            ) {
                Text("Simpan Tagihan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun ConnectBankDialog(
    onDismiss: () -> Unit,
    onSave: (code: String, name: String, accNumber: String, balance: Double) -> Unit
) {
    val banks = listOf(
        Pair("BCA", "Bank Central Asia"),
        Pair("MANDIRI", "Bank Mandiri"),
        Pair("BRI", "Bank Rakyat Indonesia"),
        Pair("BNI", "Bank Negara Indonesia"),
        Pair("JAGO", "Bank Jago"),
        Pair("GOPAY", "GoPay"),
        Pair("OVO", "OVO"),
        Pair("DANA", "DANA")
    )
    var selectedBank by remember { mutableStateOf(banks.first()) }
    var accNumber by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("2500000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hubungkan Rekening Bank", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Fitur Open Banking Premium memungkinkan sinkronisasi otomatis mutasi dan saldo rekening secara aman (enkripsi 256-bit).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Pilih Bank / Dompet Digital", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(banks) { b ->
                        FilterChip(
                            selected = selectedBank == b,
                            onClick = { selectedBank = b },
                            label = { Text(b.first) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = accNumber,
                    onValueChange = { accNumber = it },
                    label = { Text("Nomor Rekening / No. HP E-Wallet") },
                    placeholder = { Text("cth: 882019281") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { if (it.all { c -> c.isDigit() }) balanceText = it },
                    label = { Text("Saldo Saat Ini (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val balance = balanceText.toDoubleOrNull() ?: 0.0
                    val finalAcc = accNumber.ifBlank { "88319201" }
                    onSave(selectedBank.first, selectedBank.second, finalAcc, balance)
                }
            ) {
                Text("Otentikasi & Hubungkan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
