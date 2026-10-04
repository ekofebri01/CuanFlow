package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.example.utils.ReceiptImageGenerator
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.utils.CurrencyUtils
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class PeriodTabItem(
    val id: String,
    val label: String,
    val year: Int? = null,
    val month: Int? = null
)

data class TransactionDayGroup(
    val dayNumber: String,
    val dayLabel: String,
    val monthYear: String,
    val dayTotal: Double,
    val transactions: List<TransactionEntity>
)

data class TransactionCategoryGroup(
    val categoryName: String,
    val totalAmount: Double,
    val transactions: List<TransactionEntity>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    selectedFilter: String = "SEMUA",
    searchQuery: String = "",
    onFilterChange: (String) -> Unit = {},
    onSearchChange: (String) -> Unit = {},
    onDeleteTransaction: (TransactionEntity) -> Unit = {},
    onUpdateTransaction: (TransactionEntity) -> Unit = {},
    onAddTransactionClick: () -> Unit = {}
) {
    // Dynamic 1-Year Tab Range (Past 12 months, THIS MONTH, FUTURE, and 1 TAHUN)
    val calNow = Calendar.getInstance()
    val curYear = calNow.get(Calendar.YEAR)
    val curMonth = calNow.get(Calendar.MONTH) // 0-indexed

    var selectedTimeRangeMode by remember { mutableStateOf("Month") } // Day, Week, Month, Quarter, Year, All, Custom
    var showTimeRangeMenu by remember { mutableStateOf(false) }
    var customStartDate by remember { mutableLongStateOf(System.currentTimeMillis() - 7 * 86400000L) }
    var customEndDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showCustomDateDialog by remember { mutableStateOf(false) }

    // Dynamic Period Tabs depending on selectedTimeRangeMode
    val periodTabs = remember(selectedTimeRangeMode, customStartDate, customEndDate) {
        val list = mutableListOf<PeriodTabItem>()
        when (selectedTimeRangeMode) {
            "Day" -> {
                val sdfDay = SimpleDateFormat("dd/MM", Locale.ENGLISH)
                for (i in 5 downTo 2) {
                    val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
                    list.add(PeriodTabItem(id = "DAY_${c.get(Calendar.YEAR)}_${c.get(Calendar.DAY_OF_YEAR)}", label = sdfDay.format(c.time), year = c.get(Calendar.YEAR), month = c.get(Calendar.DAY_OF_YEAR)))
                }
                val yestCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                list.add(PeriodTabItem(id = "DAY_YESTERDAY", label = "Yesterday", year = yestCal.get(Calendar.YEAR), month = yestCal.get(Calendar.DAY_OF_YEAR)))
                val todayCal = Calendar.getInstance()
                list.add(PeriodTabItem(id = "DAY_TODAY", label = "Today", year = todayCal.get(Calendar.YEAR), month = todayCal.get(Calendar.DAY_OF_YEAR)))
                val tomCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
                list.add(PeriodTabItem(id = "DAY_TOMORROW", label = "Tomorrow", year = tomCal.get(Calendar.YEAR), month = tomCal.get(Calendar.DAY_OF_YEAR)))
            }
            "Week" -> {
                for (i in 4 downTo 2) {
                    val c = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -i) }
                    list.add(PeriodTabItem(id = "WEEK_${c.get(Calendar.YEAR)}_${c.get(Calendar.WEEK_OF_YEAR)}", label = "W${c.get(Calendar.WEEK_OF_YEAR)}", year = c.get(Calendar.YEAR), month = c.get(Calendar.WEEK_OF_YEAR)))
                }
                val lastWeekCal = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -1) }
                list.add(PeriodTabItem(id = "LAST_WEEK", label = "Last Week", year = lastWeekCal.get(Calendar.YEAR), month = lastWeekCal.get(Calendar.WEEK_OF_YEAR)))
                val thisWeekCal = Calendar.getInstance()
                list.add(PeriodTabItem(id = "THIS_WEEK", label = "This Week", year = thisWeekCal.get(Calendar.YEAR), month = thisWeekCal.get(Calendar.WEEK_OF_YEAR)))
                val nextWeekCal = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, 1) }
                list.add(PeriodTabItem(id = "NEXT_WEEK", label = "Next Week", year = nextWeekCal.get(Calendar.YEAR), month = nextWeekCal.get(Calendar.WEEK_OF_YEAR)))
            }
            "Quarter" -> {
                val curQ = (curMonth / 3) + 1
                for (i in 3 downTo 1) {
                    val qCal = Calendar.getInstance().apply { add(Calendar.MONTH, -i * 3) }
                    val qNum = (qCal.get(Calendar.MONTH) / 3) + 1
                    list.add(PeriodTabItem(id = "Q_${qCal.get(Calendar.YEAR)}_$qNum", label = "Q$qNum ${qCal.get(Calendar.YEAR)}", year = qCal.get(Calendar.YEAR), month = qNum))
                }
                list.add(PeriodTabItem(id = "THIS_QUARTER", label = "Q$curQ $curYear", year = curYear, month = curQ))
                val nextQCal = Calendar.getInstance().apply { add(Calendar.MONTH, 3) }
                val nextQNum = (nextQCal.get(Calendar.MONTH) / 3) + 1
                list.add(PeriodTabItem(id = "NEXT_QUARTER", label = "Q$nextQNum ${nextQCal.get(Calendar.YEAR)}", year = nextQCal.get(Calendar.YEAR), month = nextQNum))
            }
            "Year" -> {
                for (y in (curYear - 2) until curYear) {
                    list.add(PeriodTabItem(id = "YEAR_$y", label = "$y", year = y))
                }
                list.add(PeriodTabItem(id = "THIS_YEAR", label = "$curYear", year = curYear))
                list.add(PeriodTabItem(id = "NEXT_YEAR", label = "${curYear + 1}", year = curYear + 1))
            }
            "All" -> {
                list.add(PeriodTabItem(id = "ALL_TIME", label = "ALL TIME"))
            }
            "Custom" -> {
                val sdf = SimpleDateFormat("dd/MM/yy", Locale.ENGLISH)
                list.add(PeriodTabItem(id = "CUSTOM_RANGE", label = "${sdf.format(Date(customStartDate))} - ${sdf.format(Date(customEndDate))}"))
            }
            else -> { // Default: Month
                val sdfMonthYear = SimpleDateFormat("MM/yyyy", Locale.ENGLISH)
                for (i in 11 downTo 2) {
                    val c = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
                    val y = c.get(Calendar.YEAR)
                    val m = c.get(Calendar.MONTH)
                    list.add(PeriodTabItem(id = "M_${y}_$m", label = sdfMonthYear.format(c.time), year = y, month = m))
                }
                val lastMonthCal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
                list.add(PeriodTabItem(id = "LAST_MONTH", label = "LAST MONTH", year = lastMonthCal.get(Calendar.YEAR), month = lastMonthCal.get(Calendar.MONTH)))
                list.add(PeriodTabItem(id = "THIS_MONTH", label = "THIS MONTH", year = curYear, month = curMonth))
                list.add(PeriodTabItem(id = "FUTURE", label = "FUTURE"))
                list.add(PeriodTabItem(id = "1_YEAR", label = "1 TAHUN"))
            }
        }
        list
    }

    var selectedPeriodTabId by remember { mutableStateOf("THIS_MONTH") }
    var selectedWallet by remember { mutableStateOf("Mbako") }
    var showWalletDialog by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedDetailTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var isViewByCategory by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var showOptionsMenu by remember { mutableStateOf(false) }

    // Dialog states for 7 menu options
    var showSelectTimeRangeDialog by remember { mutableStateOf(false) }
    var showViewByCategoryDialog by remember { mutableStateOf(false) }
    var showAdjustBalanceDialog by remember { mutableStateOf(false) }
    var showTransferMoneyDialog by remember { mutableStateOf(false) }
    var showEditWalletDialog by remember { mutableStateOf(false) }
    var showShareWalletDialog by remember { mutableStateOf(false) }

    val tabListState = rememberLazyListState()

    // Auto-scroll when tabs change
    LaunchedEffect(selectedTimeRangeMode) {
        val defaultId = when (selectedTimeRangeMode) {
            "Day" -> "DAY_TODAY"
            "Week" -> "THIS_WEEK"
            "Quarter" -> "THIS_QUARTER"
            "Year" -> "THIS_YEAR"
            "All" -> "ALL_TIME"
            "Custom" -> "CUSTOM_RANGE"
            else -> "THIS_MONTH"
        }
        selectedPeriodTabId = defaultId
        val idx = periodTabs.indexOfFirst { it.id == defaultId }
        if (idx >= 0) {
            tabListState.scrollToItem((idx - 1).coerceAtLeast(0))
        }
    }

    // Filter transactions by selectedPeriodTab and wallet
    val periodFilteredTransactions = remember(transactions, selectedPeriodTabId, selectedWallet) {
        val selectedTab = periodTabs.find { it.id == selectedPeriodTabId } ?: periodTabs.first()

        transactions.filter { tx ->
            val matchesWallet = selectedWallet == "All Wallets" || tx.account.equals(selectedWallet, ignoreCase = true)
            if (!matchesWallet) return@filter false

            val calTx = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            val txYear = calTx.get(Calendar.YEAR)
            val txMonth = calTx.get(Calendar.MONTH)

            when (selectedTimeRangeMode) {
                "Day" -> {
                    if (selectedTab.year != null && selectedTab.month != null) {
                        txYear == selectedTab.year && calTx.get(Calendar.DAY_OF_YEAR) == selectedTab.month
                    } else {
                        true
                    }
                }
                "Week" -> {
                    if (selectedTab.year != null && selectedTab.month != null) {
                        txYear == selectedTab.year && calTx.get(Calendar.WEEK_OF_YEAR) == selectedTab.month
                    } else {
                        true
                    }
                }
                "Quarter" -> {
                    val txQuarter = (txMonth / 3) + 1
                    if (selectedTab.year != null && selectedTab.month != null) {
                        txYear == selectedTab.year && txQuarter == selectedTab.month
                    } else {
                        true
                    }
                }
                "Year" -> {
                    if (selectedTab.year != null) {
                        txYear == selectedTab.year
                    } else {
                        true
                    }
                }
                "All" -> true
                "Custom" -> tx.timestamp in customStartDate..(customEndDate + 86399999L)
                else -> { // "Month"
                    when (selectedTab.id) {
                        "THIS_MONTH", "LAST_MONTH" -> {
                            txYear == selectedTab.year && txMonth == selectedTab.month
                        }
                        "FUTURE" -> {
                            val endOfThisMonth = Calendar.getInstance().apply {
                                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                                set(Calendar.HOUR_OF_DAY, 23)
                                set(Calendar.MINUTE, 59)
                                set(Calendar.SECOND, 59)
                                set(Calendar.MILLISECOND, 999)
                            }
                            tx.timestamp > endOfThisMonth.timeInMillis
                        }
                        "1_YEAR" -> {
                            val oneYearAgo = Calendar.getInstance().apply {
                                add(Calendar.YEAR, -1)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }
                            tx.timestamp >= oneYearAgo.timeInMillis && tx.timestamp <= System.currentTimeMillis() + 86400000L
                        }
                        else -> {
                            if (selectedTab.year != null && selectedTab.month != null) {
                                txYear == selectedTab.year && txMonth == selectedTab.month
                            } else {
                                true
                            }
                        }
                    }
                }
            }
        }
    }

    // Calculate Inflow, Outflow, and Total based on the filtered period transactions!
    val inflow = periodFilteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val outflow = periodFilteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val netTotal = inflow - outflow

    // Group transactions by day for this selected period
    val groupedTransactions = remember(periodFilteredTransactions, searchQuery) {
        val filtered = if (searchQuery.isBlank()) {
            periodFilteredTransactions
        } else {
            periodFilteredTransactions.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.note.contains(searchQuery, ignoreCase = true)
            }
        }

        val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val sdfDayNum = SimpleDateFormat("dd", Locale.ENGLISH)
        val sdfMonthYear = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
        val sdfDayLabel = SimpleDateFormat("EEEE", Locale.ENGLISH)

        fun getDayLabel(ts: Long): String {
            val calToday = Calendar.getInstance()
            val calTx = Calendar.getInstance().apply { timeInMillis = ts }
            return when {
                calToday.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
                calToday.get(Calendar.DAY_OF_YEAR) == calTx.get(Calendar.DAY_OF_YEAR) -> "Today"

                calToday.get(Calendar.YEAR) == calTx.get(Calendar.YEAR) &&
                calToday.get(Calendar.DAY_OF_YEAR) - calTx.get(Calendar.DAY_OF_YEAR) == 1 -> "Yesterday"

                else -> sdfDayLabel.format(Date(ts))
            }
        }

        filtered.groupBy { sdfKey.format(Date(it.timestamp)) }
            .map { (_, dayItems) ->
                val first = dayItems.first()
                val daySum = dayItems.sumOf { if (it.type == "INCOME") it.amount else -it.amount }
                TransactionDayGroup(
                    dayNumber = sdfDayNum.format(Date(first.timestamp)),
                    dayLabel = getDayLabel(first.timestamp),
                    monthYear = sdfMonthYear.format(Date(first.timestamp)),
                    dayTotal = daySum,
                    transactions = dayItems
                )
            }
            .sortedByDescending { it.transactions.first().timestamp }
    }

    // Group transactions by category for "View by category" mode
    val categoryGroupedTransactions = remember(periodFilteredTransactions, searchQuery) {
        val filtered = if (searchQuery.isBlank()) {
            periodFilteredTransactions
        } else {
            periodFilteredTransactions.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.note.contains(searchQuery, ignoreCase = true)
            }
        }

        filtered.groupBy { it.category.ifBlank { it.title } }
            .map { (catName, items) ->
                val sum = items.sumOf { if (it.type == "INCOME") it.amount else -it.amount }
                TransactionCategoryGroup(
                    categoryName = catName,
                    totalAmount = sum,
                    transactions = items.sortedByDescending { it.timestamp }
                )
            }
            .sortedByDescending { it.transactions.size }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .testTag("transactions_screen")
    ) {
        // 1. TOP HEADER: Balance + Wallet Pill + Icons (Search, More)
        Surface(
            color = Color(0xFF141416),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: Centered Balance & Actions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Balance",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8E8E93),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val balFormatted = NumberFormat.getNumberInstance(Locale.US).format(netTotal.toLong())
                        Text(
                            text = "Rp $balFormatted",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Top Right Actions (Search & More)
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { isSearchActive = !isSearchActive }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Box {
                            IconButton(onClick = { showOptionsMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false },
                                modifier = Modifier
                                    .background(Color(0xFF1E1E20))
                                    .widthIn(min = 210.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Select time range", color = Color.White, fontSize = 15.sp) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showTimeRangeMenu = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (isViewByCategory) "View by transaction" else "View by category",
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                    },
                                    onClick = {
                                        showOptionsMenu = false
                                        isViewByCategory = !isViewByCategory
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Adjust Balance", color = Color.White, fontSize = 15.sp) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showAdjustBalanceDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Transfer money", color = Color.White, fontSize = 15.sp) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showTransferMoneyDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Synchronize", color = Color.White, fontSize = 15.sp) },
                                    onClick = {
                                        showOptionsMenu = false
                                        Toast.makeText(context, "Synchronized with Mbako wallet", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Edit wallet", color = Color.White, fontSize = 15.sp) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showEditWalletDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share wallet", color = Color.White, fontSize = 15.sp) },
                                    onClick = {
                                        showOptionsMenu = false
                                        showShareWalletDialog = true
                                    }
                                )
                            }

                            // Sub-menu for "Select time range" matching screenshot
                            DropdownMenu(
                                expanded = showTimeRangeMenu,
                                onDismissRequest = { showTimeRangeMenu = false },
                                modifier = Modifier
                                    .background(Color(0xFF1E1E20))
                                    .widthIn(min = 180.dp)
                            ) {
                                val modes = listOf("Day", "Week", "Month", "Quarter", "Year", "All", "Custom")
                                modes.forEach { mode ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (selectedTimeRangeMode == mode) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                } else {
                                                    Spacer(modifier = Modifier.size(20.dp))
                                                }
                                                Spacer(modifier = Modifier.width(14.dp))
                                                Text(
                                                    text = mode,
                                                    color = Color.White,
                                                    fontSize = 15.sp,
                                                    fontWeight = if (selectedTimeRangeMode == mode) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        },
                                        onClick = {
                                            showTimeRangeMenu = false
                                            selectedTimeRangeMode = mode
                                            if (mode == "Custom") {
                                                showCustomDateDialog = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Wallet Pill: [Orange Wallet] Mbako [Arrow]
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF262628),
                    modifier = Modifier
                        .clickable { showWalletDialog = true }
                        .testTag("wallet_selector_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE65100)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedWallet,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.UnfoldMore,
                            contentDescription = null,
                            tint = Color(0xFF8E8E93),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Search Bar
                if (isSearchActive) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search transactions...", color = Color(0xFF8E8E93)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00C853),
                            unfocusedBorderColor = Color(0xFF38383A)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // 2. PERIOD TABS: Horizontally scrollable 1-year tabs (Past 12 months, THIS MONTH, FUTURE, 1 TAHUN)
                LazyRow(
                    state = tabListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(periodTabs, key = { it.id }) { tab ->
                        val isSelected = selectedPeriodTabId == tab.id
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedPeriodTabId = tab.id }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tab.label,
                                color = if (isSelected) Color.White else Color(0xFF75757A),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(2.5.dp)
                                        .background(Color.White, RoundedCornerShape(1.dp))
                                )
                            } else {
                                Spacer(modifier = Modifier.height(2.5.dp))
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF242426), thickness = 0.8.dp)

                // 3. INFLOW / OUTFLOW SUMMARY
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    // Inflow Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Inflow", color = Color(0xFF8E8E93), fontSize = 14.sp)
                        val inflowFormatted = NumberFormat.getNumberInstance(Locale.US).format(inflow.toLong())
                        Text(inflowFormatted, color = Color(0xFF38BDF8), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Outflow Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Outflow", color = Color(0xFF8E8E93), fontSize = 14.sp)
                        val outflowFormatted = NumberFormat.getNumberInstance(Locale.US).format(outflow.toLong())
                        Text(outflowFormatted, color = Color(0xFFFF5252), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Right-aligned Underline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        HorizontalDivider(
                            color = Color(0xFF38383A),
                            thickness = 1.dp,
                            modifier = Modifier.width(130.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Net Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        val netFormatted = NumberFormat.getNumberInstance(Locale.US).format(netTotal.toLong())
                        Text(netFormatted, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // "View report for this period" Green Pill Button
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF132F20),
                            modifier = Modifier
                                .clickable { onFilterChange("ALL") }
                                .testTag("view_report_btn")
                        ) {
                            Text(
                                text = "View report for this period",
                                color = Color(0xFF00C853),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section Separator bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFF0A0A0C))
        )

        // 4. TRANSACTION LIST (View by Category OR View by Transaction)
        if (isViewByCategory) {
            if (categoryGroupedTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions in this period",
                        color = Color(0xFF8E8E93),
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    categoryGroupedTransactions.forEach { catGroup ->
                        item {
                            // Category Header Box matching screenshot
                            Surface(
                                color = Color(0xFF18181A),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val isTingwe = catGroup.categoryName.contains("tingwe", ignoreCase = true)
                                            val isSalary = catGroup.categoryName.contains("salary", ignoreCase = true) || catGroup.categoryName.contains("gaji", ignoreCase = true)
                                            val isAdj = catGroup.categoryName.contains("adju", ignoreCase = true)

                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isTingwe) Color(0xFFFBC02D)
                                                        else if (isSalary) Color(0xFF2E7D32)
                                                        else if (isAdj) Color(0xFFFFA000)
                                                        else Color(0xFF00897B)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isTingwe) Icons.Default.ShoppingCart
                                                    else if (isSalary) Icons.Default.Payments
                                                    else if (isAdj) Icons.Default.Assessment
                                                    else Icons.Default.Category,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column {
                                                Text(
                                                    text = catGroup.categoryName,
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                val countText = if (catGroup.transactions.size == 1) "1 transaction" else "${catGroup.transactions.size} transactions"
                                                Text(
                                                    text = countText,
                                                    color = Color(0xFF8E8E93),
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }

                                        // Category Total Amount
                                        val catFormatted = if (catGroup.totalAmount < 0) {
                                            "-${NumberFormat.getNumberInstance(Locale.US).format(-catGroup.totalAmount.toLong())}"
                                        } else {
                                            NumberFormat.getNumberInstance(Locale.US).format(catGroup.totalAmount.toLong())
                                        }
                                        Text(
                                            text = catFormatted,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = Color(0xFF28282B), thickness = 0.6.dp)
                                }
                            }
                        }

                        // Transactions in this Category
                        val sdfCatDate = SimpleDateFormat("dd MMMM yyyy,", Locale.ENGLISH)
                        val sdfCatDay = SimpleDateFormat("EEEE", Locale.ENGLISH)

                        items(catGroup.transactions, key = { it.id }) { tx ->
                            Surface(
                                color = Color(0xFF18181A),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedDetailTransaction = tx }
                                        .padding(start = 72.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${sdfCatDate.format(Date(tx.timestamp))}\n${sdfCatDay.format(Date(tx.timestamp))}",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp
                                        )
                                        if (tx.note.isNotBlank() && tx.note != tx.title) {
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = tx.note,
                                                color = Color(0xFF8E8E93),
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    val amountFormatted = NumberFormat.getNumberInstance(Locale.US).format(tx.amount.toLong())
                                    Text(
                                        text = amountFormatted,
                                        color = if (tx.type == "INCOME") Color(0xFF38BDF8) else Color(0xFFFF5252),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Thick separator between category sections
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(Color(0xFF0A0A0C))
                            )
                        }
                    }
                }
            }
        } else {
            // Standard Daily Grouped Transactions List
            if (groupedTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions in this period",
                        color = Color(0xFF8E8E93),
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    groupedTransactions.forEach { dayGroup ->
                        item {
                            // Date Header Box
                            Surface(
                                color = Color(0xFF18181A),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Big 2-digit day number
                                            Text(
                                                text = dayGroup.dayNumber,
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Column {
                                                Text(
                                                    text = dayGroup.dayLabel,
                                                    color = Color.White,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = dayGroup.monthYear,
                                                    color = Color(0xFF8E8E93),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }

                                        // Day total
                                        val dayFormatted = NumberFormat.getNumberInstance(Locale.US).format(dayGroup.dayTotal.toLong())
                                        Text(
                                            text = dayFormatted,
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = Color(0xFF28282B), thickness = 0.6.dp)
                                }
                            }
                        }

                        // Items for this day
                        items(dayGroup.transactions, key = { it.id }) { tx ->
                            Surface(
                                color = Color(0xFF18181A),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedDetailTransaction = tx }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Circular category icon
                                        val isTingwe = tx.title.contains("tingwe", ignoreCase = true)
                                        val isSalary = tx.title.contains("salary", ignoreCase = true) || tx.category.contains("gaji", ignoreCase = true)

                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isTingwe) Color(0xFFFBC02D)
                                                    else if (isSalary) Color(0xFF2E7D32)
                                                    else if (tx.type == "EXPENSE") Color(0xFFC62828)
                                                    else Color(0xFF00897B)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isTingwe) Icons.Default.ShoppingCart
                                                else if (isSalary) Icons.Default.Payments
                                                else if (tx.type == "EXPENSE") Icons.Default.ArrowOutward
                                                else Icons.Default.ArrowDownward,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Text(
                                                text = tx.title,
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (tx.note.isNotBlank() && tx.note != tx.title) {
                                                Text(
                                                    text = tx.note,
                                                    color = Color(0xFF8E8E93),
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                    }

                                    val amountFormatted = NumberFormat.getNumberInstance(Locale.US).format(tx.amount.toLong())
                                    Text(
                                        text = amountFormatted,
                                        color = if (tx.type == "INCOME") Color(0xFF38BDF8) else Color(0xFFFF5252),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Dark separator between date groups
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(Color(0xFF0A0A0C))
                            )
                        }
                    }
                }
            }
        }
    }

    // Wallet Picker Dialog
    if (showWalletDialog) {
        AlertDialog(
            onDismissRequest = { showWalletDialog = false },
            title = { Text("Select Wallet", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("Mbako", "SeaBank", "Angsuran BRI", "All Wallets").forEach { wallet ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedWallet = wallet
                                    showWalletDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(wallet, fontSize = 16.sp, fontWeight = if (selectedWallet == wallet) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Transaction Detail Screen & In-place Editors matching Money Lover screenshot
    selectedDetailTransaction?.let { initialTx ->
        var currentTx by remember(initialTx.id) { mutableStateOf(initialTx) }

        // Dialog states for editing fields
        var showCategoryPicker by remember { mutableStateOf(false) }
        var showAmountEditor by remember { mutableStateOf(false) }
        var showDateEditor by remember { mutableStateOf(false) }
        var showWalletPicker by remember { mutableStateOf(false) }
        var showNoteEditor by remember { mutableStateOf(false) }
        var showDeleteConfirmDialog by remember { mutableStateOf(false) }
        var showQuickEditDialog by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = { selectedDetailTransaction = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            BackHandler { selectedDetailTransaction = null }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF141416)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // TOP BAR: Close (X) on left, and Share, Edit, Delete on right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedDetailTransaction = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 1. Share button (Export & Share as JPG Receipt Card)
                            IconButton(onClick = {
                                try {
                                    val imageUri = ReceiptImageGenerator.generateReceiptJpgUri(context, currentTx)
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        if (imageUri != null) {
                                            type = "image/jpeg"
                                            putExtra(Intent.EXTRA_STREAM, imageUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        } else {
                                            type = "text/plain"
                                        }
                                        val sdfShare = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.ENGLISH)
                                        val shareText = "Bukti Transaksi:\nKategori: ${currentTx.category}\nNominal: Rp ${NumberFormat.getNumberInstance(Locale.US).format(currentTx.amount.toLong())}\nTanggal: ${sdfShare.format(Date(currentTx.timestamp))}\nDompet: ${currentTx.account}${if (currentTx.note.isNotBlank()) "\nCatatan: ${currentTx.note}" else ""}"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Bagikan Bukti Transaksi (JPG)"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal membagikan gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // 2. Edit button
                            IconButton(onClick = { showQuickEditDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // 3. Delete button
                            IconButton(onClick = { showDeleteConfirmDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BODY CONTENT: Category, Nominal, Tanggal, Dompet (all touchable & editable!)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        // 1. CATEGORY ROW (Touchable to edit)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showCategoryPicker = true }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isSalary = currentTx.category.contains("salary", ignoreCase = true) || currentTx.category.contains("gaji", ignoreCase = true)
                            val isTingwe = currentTx.category.contains("tingwe", ignoreCase = true) || currentTx.category.contains("belanja", ignoreCase = true)
                            val isAdj = currentTx.category.contains("adju", ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSalary) Color(0xFF2E7D32)
                                        else if (isTingwe) Color(0xFFFBC02D)
                                        else if (isAdj) Color(0xFFFFA000)
                                        else if (currentTx.type == "EXPENSE") Color(0xFFC62828)
                                        else Color(0xFF00897B)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSalary) Icons.Default.Payments
                                    else if (isTingwe) Icons.Default.ShoppingCart
                                    else if (isAdj) Icons.Default.Assessment
                                    else if (currentTx.type == "EXPENSE") Icons.Default.ArrowOutward
                                    else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(18.dp))

                            Text(
                                text = currentTx.category.ifBlank { currentTx.title },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. NOMINAL ROW (Touchable to edit)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showAmountEditor = true }
                                .padding(vertical = 8.dp)
                        ) {
                            val amountStr = NumberFormat.getNumberInstance(Locale.US).format(currentTx.amount.toLong())
                            Text(
                                text = "Rp $amountStr",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentTx.type == "INCOME") Color(0xFF38BDF8) else Color(0xFFFF5252)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 3. TANGGAL / DATE ROW (Touchable to edit)
                        val sdfDetailDate = SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.ENGLISH)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showDateEditor = true }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Date",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(
                                text = sdfDetailDate.format(Date(currentTx.timestamp)),
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4. DOMPET / WALLET ROW (Touchable to edit)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showWalletPicker = true }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE65100)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(
                                text = currentTx.account.ifBlank { "Mbako" },
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 5. NOTE / CATATAN ROW (Touchable to edit)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showNoteEditor = true }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notes,
                                contentDescription = "Note",
                                tint = Color(0xFF8E8E93),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(
                                text = if (currentTx.note.isNotBlank()) currentTx.note else "Tambah catatan...",
                                color = if (currentTx.note.isNotBlank()) Color.White else Color(0xFF8E8E93),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // Dialog 1: Pick Category
            if (showCategoryPicker) {
                val availableCategories = listOf(
                    "Salary", "Gaji", "Belanja", "tingwe", "Makanan & Minuman",
                    "Transportasi", "Tagihan & Utilitas", "Adjument", "Hiburan", "Kesehatan", "Investasi", "Lainnya"
                )
                AlertDialog(
                    onDismissRequest = { showCategoryPicker = false },
                    title = { Text("Pilih Kategori", fontWeight = FontWeight.Bold) },
                    text = {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            availableCategories.forEach { cat ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val updated = currentTx.copy(category = cat, title = cat)
                                            currentTx = updated
                                            onUpdateTransaction(updated)
                                            showCategoryPicker = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = currentTx.category == cat,
                                        onClick = {
                                            val updated = currentTx.copy(category = cat, title = cat)
                                            currentTx = updated
                                            onUpdateTransaction(updated)
                                            showCategoryPicker = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(cat, fontSize = 16.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {}
                )
            }

            // Dialog 2: Edit Amount
            if (showAmountEditor) {
                var amountInput by remember { mutableStateOf(currentTx.amount.toLong().toString()) }
                val focusRequester = remember { FocusRequester() }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                AlertDialog(
                    onDismissRequest = { showAmountEditor = false },
                    title = { Text("Ubah Nominal", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = amountInput,
                                onValueChange = { if (it.all { ch -> ch.isDigit() }) amountInput = it },
                                label = { Text("Nominal (Rp)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val newAmt = amountInput.toDoubleOrNull() ?: currentTx.amount
                                val updated = currentTx.copy(amount = newAmt)
                                currentTx = updated
                                onUpdateTransaction(updated)
                                showAmountEditor = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAmountEditor = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Dialog 3: Edit Date
            if (showDateEditor) {
                val sdfInput = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                var dateInput by remember { mutableStateOf(sdfInput.format(Date(currentTx.timestamp))) }
                AlertDialog(
                    onDismissRequest = { showDateEditor = false },
                    title = { Text("Ubah Tanggal", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = dateInput,
                                onValueChange = { dateInput = it },
                                label = { Text("Format: dd/MM/yyyy") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                try {
                                    val parsed = sdfInput.parse(dateInput)
                                    if (parsed != null) {
                                        val updated = currentTx.copy(timestamp = parsed.time)
                                        currentTx = updated
                                        onUpdateTransaction(updated)
                                    }
                                } catch (_: Exception) {}
                                showDateEditor = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDateEditor = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Dialog 4: Pick Wallet
            if (showWalletPicker) {
                val availableWallets = listOf("Mbako", "SeaBank", "Angsuran BRI", "BCA", "Dompet Tunai")
                AlertDialog(
                    onDismissRequest = { showWalletPicker = false },
                    title = { Text("Pilih Dompet / Akun", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            availableWallets.forEach { w ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val updated = currentTx.copy(account = w)
                                            currentTx = updated
                                            onUpdateTransaction(updated)
                                            showWalletPicker = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = currentTx.account == w,
                                        onClick = {
                                            val updated = currentTx.copy(account = w)
                                            currentTx = updated
                                            onUpdateTransaction(updated)
                                            showWalletPicker = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(w, fontSize = 16.sp)
                                }
                            }
                        }
                    },
                    confirmButton = {}
                )
            }

            // Dialog 5: Edit Note
            if (showNoteEditor) {
                var noteInput by remember { mutableStateOf(currentTx.note) }
                AlertDialog(
                    onDismissRequest = { showNoteEditor = false },
                    title = { Text("Catatan Transaksi", fontWeight = FontWeight.Bold) },
                    text = {
                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = { noteInput = it },
                            label = { Text("Catatan / Keterangan") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val updated = currentTx.copy(note = noteInput)
                                currentTx = updated
                                onUpdateTransaction(updated)
                                showNoteEditor = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showNoteEditor = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Dialog 6: Delete Confirmation (Pas hapus tampilin jendela konfirmasi apakah beneran mau hapus transaksi)
            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("Hapus Transaksi?", fontWeight = FontWeight.Bold) },
                    text = { Text("Apakah Anda yakin ingin menghapus transaksi ini? Tindakan ini tidak dapat dibatalkan.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                onDeleteTransaction(currentTx)
                                showDeleteConfirmDialog = false
                                selectedDetailTransaction = null
                                Toast.makeText(context, "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Hapus")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }

            // Dialog 7: Quick Edit All
            if (showQuickEditDialog) {
                var quickTitle by remember { mutableStateOf(currentTx.title) }
                var quickAmount by remember { mutableStateOf(currentTx.amount.toLong().toString()) }
                var quickNote by remember { mutableStateOf(currentTx.note) }
                AlertDialog(
                    onDismissRequest = { showQuickEditDialog = false },
                    title = { Text("Edit Transaksi", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = quickTitle,
                                onValueChange = { quickTitle = it },
                                label = { Text("Judul / Kategori") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = quickAmount,
                                onValueChange = { if (it.all { ch -> ch.isDigit() }) quickAmount = it },
                                label = { Text("Nominal (Rp)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = quickNote,
                                onValueChange = { quickNote = it },
                                label = { Text("Catatan") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val amt = quickAmount.toDoubleOrNull() ?: currentTx.amount
                                val updated = currentTx.copy(title = quickTitle, category = quickTitle, amount = amt, note = quickNote)
                                currentTx = updated
                                onUpdateTransaction(updated)
                                showQuickEditDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                        ) {
                            Text("Simpan")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showQuickEditDialog = false }) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }

    // 1. Select time range Dialog
    if (showSelectTimeRangeDialog) {
        AlertDialog(
            onDismissRequest = { showSelectTimeRangeDialog = false },
            title = { Text("Select time range", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(
                        "This Month" to "THIS_MONTH",
                        "Last Month" to "LAST_MONTH",
                        "1 Year (1 Tahun)" to "1_YEAR",
                        "Future" to "FUTURE"
                    ).forEach { (label, id) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPeriodTabId = id
                                    showSelectTimeRangeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedPeriodTabId == id,
                                onClick = {
                                    selectedPeriodTabId = id
                                    showSelectTimeRangeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // 2. View by category Dialog
    if (showViewByCategoryDialog) {
        val categoryBreakdown = remember(periodFilteredTransactions) {
            periodFilteredTransactions
                .groupBy { it.category }
                .map { (cat, list) -> cat to list.sumOf { it.amount } }
                .sortedByDescending { it.second }
        }
        AlertDialog(
            onDismissRequest = { showViewByCategoryDialog = false },
            title = { Text("View by category", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (categoryBreakdown.isEmpty()) {
                        Text("No category data for this period", color = Color.Gray)
                    } else {
                        val totalSum = categoryBreakdown.sumOf { it.second }
                        categoryBreakdown.forEach { (cat, amount) ->
                            val pct = if (totalSum > 0) ((amount / totalSum) * 100).toInt() else 0
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(cat, fontWeight = FontWeight.Medium)
                                    Text(CurrencyUtils.formatRupiah(amount), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LinearProgressIndicator(
                                        progress = { (amount / totalSum).toFloat().coerceIn(0f, 1f) },
                                        modifier = Modifier.weight(1f).padding(top = 4.dp),
                                        color = Color(0xFF00C853)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("$pct%", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showViewByCategoryDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // 3. Adjust Balance Dialog
    if (showAdjustBalanceDialog) {
        var newBalanceInput by remember { mutableStateOf(netTotal.toLong().toString()) }
        AlertDialog(
            onDismissRequest = { showAdjustBalanceDialog = false },
            title = { Text("Adjust Balance", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Current balance: Rp ${NumberFormat.getNumberInstance(Locale.US).format(netTotal.toLong())}", color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newBalanceInput,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) newBalanceInput = it },
                        label = { Text("Actual new balance (Rp)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newBal = newBalanceInput.toDoubleOrNull() ?: netTotal
                        val diff = newBal - netTotal
                        if (diff != 0.0) {
                            onAddTransactionClick()
                        }
                        showAdjustBalanceDialog = false
                        Toast.makeText(context, "Balance adjusted to Rp ${NumberFormat.getNumberInstance(Locale.US).format(newBal.toLong())}", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Text("Save Adjustment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustBalanceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 4. Transfer money Dialog
    if (showTransferMoneyDialog) {
        var targetWallet by remember { mutableStateOf("SeaBank") }
        var transferAmount by remember { mutableStateOf("") }
        var transferNote by remember { mutableStateOf("Transfer antar rekening") }
        AlertDialog(
            onDismissRequest = { showTransferMoneyDialog = false },
            title = { Text("Transfer money", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("From: $selectedWallet", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("To Wallet / Account:", fontSize = 13.sp, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                        listOf("SeaBank", "Angsuran BRI", "BCA").forEach { w ->
                            FilterChip(
                                selected = targetWallet == w,
                                onClick = { targetWallet = w },
                                label = { Text(w, fontSize = 12.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = transferAmount,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) transferAmount = it },
                        label = { Text("Nominal Transfer (Rp)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = transferNote,
                        onValueChange = { transferNote = it },
                        label = { Text("Note / Catatan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = transferAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            showTransferMoneyDialog = false
                            Toast.makeText(context, "Transfer Rp ${NumberFormat.getNumberInstance(Locale.US).format(amt.toLong())} ke $targetWallet berhasil", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Text("Transfer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferMoneyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 5. Edit wallet Dialog
    if (showEditWalletDialog) {
        var walletNameInput by remember { mutableStateOf(selectedWallet) }
        AlertDialog(
            onDismissRequest = { showEditWalletDialog = false },
            title = { Text("Edit wallet", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = walletNameInput,
                        onValueChange = { walletNameInput = it },
                        label = { Text("Wallet Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Currency: IDR (Indonesian Rupiah)", color = Color.Gray, fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (walletNameInput.isNotBlank()) {
                            selectedWallet = walletNameInput
                        }
                        showEditWalletDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditWalletDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 6. Share wallet Dialog
    if (showShareWalletDialog) {
        AlertDialog(
            onDismissRequest = { showShareWalletDialog = false },
            title = { Text("Share wallet '$selectedWallet'", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Bagikan ringkasan transaksi atau undang anggota untuk mengelola dompet bersama.")
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            val bal = NumberFormat.getNumberInstance(Locale.US).format(netTotal.toLong())
                            val shareText = "Laporan Dompet $selectedWallet:\nSaldo: Rp $bal\nInflow: Rp ${NumberFormat.getNumberInstance(Locale.US).format(inflow.toLong())}\nOutflow: Rp ${NumberFormat.getNumberInstance(Locale.US).format(outflow.toLong())}\nDicatat via Money Lover"
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Wallet Summary"))
                            showShareWalletDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Financial Summary")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showShareWalletDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // 7. Custom Date Range Dialog
    if (showCustomDateDialog) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        var startInput by remember { mutableStateOf(sdf.format(Date(customStartDate))) }
        var endInput by remember { mutableStateOf(sdf.format(Date(customEndDate))) }
        AlertDialog(
            onDismissRequest = { showCustomDateDialog = false },
            title = { Text("Select Custom Date Range", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = startInput,
                        onValueChange = { startInput = it },
                        label = { Text("Start Date (dd/MM/yyyy)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = endInput,
                        onValueChange = { endInput = it },
                        label = { Text("End Date (dd/MM/yyyy)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            sdf.parse(startInput)?.let { customStartDate = it.time }
                            sdf.parse(endInput)?.let { customEndDate = it.time }
                        } catch (_: Exception) {}
                        showCustomDateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Text("Apply Range")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
