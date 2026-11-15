package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.Formatters
import com.example.core.InvoiceExportManager
import com.example.core.LocalAppStrings
import com.example.core.LocalCurrencySymbol
import com.example.data.local.KhataEntryEntity
import com.example.data.local.KhataType
import com.example.data.local.PaymentMethod
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.TransactionItemRow
import com.example.ui.theme.BrandMonoFont
import com.example.ui.theme.DueRed
import com.example.ui.theme.PayableAmber
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.CustomerSummary
import com.example.ui.viewmodel.MainUiState
import com.example.ui.viewmodel.UnifiedTxKind
import java.util.Calendar

private enum class TxTypeFilter { ALL, KHATA_GIVEN, KHATA_RECEIVED, INCOME, EXPENSE }
private enum class TxDateFilter { ALL, TODAY, WEEK, MONTH }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    state: MainUiState,
    onBack: (() -> Unit)? = null,
    onOpenCustomerDetail: (Long) -> Unit
) {
    val strings = LocalAppStrings.current
    var query by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf(TxTypeFilter.ALL) }
    var dateFilter by remember { mutableStateOf(TxDateFilter.ALL) }

    val cutoff = remember(dateFilter) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        when (dateFilter) {
            TxDateFilter.ALL -> 0L
            TxDateFilter.TODAY -> cal.timeInMillis
            TxDateFilter.WEEK -> cal.timeInMillis - 6 * 86_400_000L
            TxDateFilter.MONTH -> cal.timeInMillis - 29 * 86_400_000L
        }
    }

    val filtered = remember(state.unifiedTransactions, query, typeFilter, cutoff) {
        state.unifiedTransactions.filter { tx ->
            val matchesQuery = query.isBlank() ||
                tx.title.contains(query, ignoreCase = true) ||
                tx.subtitle.contains(query, ignoreCase = true)
            val matchesType = when (typeFilter) {
                TxTypeFilter.ALL -> true
                TxTypeFilter.KHATA_GIVEN -> tx.kind == UnifiedTxKind.KHATA_GIVEN
                TxTypeFilter.KHATA_RECEIVED -> tx.kind == UnifiedTxKind.KHATA_RECEIVED
                TxTypeFilter.INCOME -> tx.kind == UnifiedTxKind.INCOME
                TxTypeFilter.EXPENSE -> tx.kind == UnifiedTxKind.EXPENSE
            }
            val matchesDate = tx.timestamp >= cutoff
            matchesQuery && matchesType && matchesDate
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.allTransactions) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(strings.searchCustomersHint) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(selected = typeFilter == TxTypeFilter.ALL, onClick = { typeFilter = TxTypeFilter.ALL }, label = { Text(strings.allTypes) })
                    FilterChip(selected = typeFilter == TxTypeFilter.KHATA_GIVEN, onClick = { typeFilter = TxTypeFilter.KHATA_GIVEN }, label = { Text(strings.khataGiven) })
                    FilterChip(selected = typeFilter == TxTypeFilter.KHATA_RECEIVED, onClick = { typeFilter = TxTypeFilter.KHATA_RECEIVED }, label = { Text(strings.khataReceived) })
                    FilterChip(selected = typeFilter == TxTypeFilter.INCOME, onClick = { typeFilter = TxTypeFilter.INCOME }, label = { Text(strings.incomeLabel) })
                    FilterChip(selected = typeFilter == TxTypeFilter.EXPENSE, onClick = { typeFilter = TxTypeFilter.EXPENSE }, label = { Text(strings.expenseLabel) })
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(selected = dateFilter == TxDateFilter.ALL, onClick = { dateFilter = TxDateFilter.ALL }, label = { Text(strings.allTime) })
                    FilterChip(selected = dateFilter == TxDateFilter.TODAY, onClick = { dateFilter = TxDateFilter.TODAY }, label = { Text(strings.today) })
                    FilterChip(selected = dateFilter == TxDateFilter.WEEK, onClick = { dateFilter = TxDateFilter.WEEK }, label = { Text(strings.thisWeek) })
                    FilterChip(selected = dateFilter == TxDateFilter.MONTH, onClick = { dateFilter = TxDateFilter.MONTH }, label = { Text(strings.thisMonth) })
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (filtered.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Rounded.History,
                        title = strings.noTransactionsYet,
                        subtitle = strings.startTip
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtered, key = { it.id }) { tx ->
                            TransactionItemRow(
                                tx = tx,
                                language = state.prefs.language,
                                onClick = { tx.customerId?.let(onOpenCustomerDetail) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DueRemindersScreen(
    state: MainUiState,
    onBack: () -> Unit,
    onOpenCustomerDetail: (Long) -> Unit,
    onRecordPayment: (KhataEntryEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    val currency = LocalCurrencySymbol.current
    val context = LocalContext.current

    val dueList = remember(state.customers) {
        state.customers.filter { it.netBalance > 0.01 }.sortedByDescending { it.netBalance }
    }
    var collectTarget by remember { mutableStateOf<CustomerSummary?>(null) }
    var collectAmount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.dueReminders) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 16.dp)
            ) {
                if (dueList.isEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EmptyStateCard(
                        icon = Icons.Rounded.CheckCircleOutline,
                        title = strings.noDueReminders,
                        subtitle = strings.allClearedMsg
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(dueList, key = { it.customer.id }) { summary ->
                            val cust = summary.customer
                            Surface(
                                onClick = { onOpenCustomerDetail(cust.id) },
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(DueRed.copy(alpha = 0.12f))
                                            ) {
                                                Text(
                                                    Formatters.initials(cust.name),
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = DueRed
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(cust.name, style = MaterialTheme.typography.titleMedium)
                                                if (cust.dueReminderDate != null) {
                                                    Text(
                                                        "${strings.dueDatePrefix} ${Formatters.formatDate(cust.dueReminderDate, state.prefs.language)}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = PayableAmber
                                                    )
                                                } else if (cust.phone.isNotBlank()) {
                                                    Text(cust.phone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                        Text(
                                            text = Formatters.formatMoney(summary.netBalance, currency),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = BrandMonoFont,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = DueRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                val msg = "Assalamu Alaikum / Hello ${cust.name}, friendly payment reminder from ${state.prefs.businessName}: your due balance is ${Formatters.formatMoney(summary.netBalance, currency)}. Thank you!"
                                                InvoiceExportManager.sendWhatsAppReminder(context, cust.phone, msg)
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Rounded.Chat, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(strings.remindWhatsApp)
                                        }
                                        if (cust.phone.isNotBlank()) {
                                            OutlinedButton(
                                                onClick = { InvoiceExportManager.dialPhone(context, cust.phone) },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Rounded.Call, contentDescription = strings.call, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Button(
                                            onClick = {
                                                collectTarget = summary
                                                collectAmount = summary.netBalance.toInt().toString()
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Rounded.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(strings.recordPayment)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (collectTarget != null) {
        val target = collectTarget!!
        AlertDialog(
            onDismissRequest = { collectTarget = null },
            title = { Text("${strings.gotPaymentBtn} — ${target.customer.name}") },
            text = {
                OutlinedTextField(
                    value = collectAmount,
                    onValueChange = { collectAmount = it },
                    label = { Text(strings.amountLabel) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val amt = collectAmount.toDoubleOrNull()
                    if (amt != null && amt > 0.0) {
                        onRecordPayment(
                            KhataEntryEntity(
                                customerId = target.customer.id,
                                type = KhataType.PAYMENT_RECEIVED,
                                amount = amt,
                                description = "Due collection",
                                paymentMethod = PaymentMethod.CASH
                            )
                        )
                        collectTarget = null
                    }
                }) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { collectTarget = null }) { Text(strings.cancel) }
            }
        )
    }
}
