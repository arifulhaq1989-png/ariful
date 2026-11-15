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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.Formatters
import com.example.core.LocalAppStrings
import com.example.core.LocalCurrencySymbol
import com.example.data.local.CashbookCategories
import com.example.data.local.CashbookEntryEntity
import com.example.data.local.CashbookType
import com.example.data.local.PaymentMethod
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.BrandMonoFont
import com.example.ui.theme.DueRed
import com.example.ui.theme.PayableAmber
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.MainUiState

private enum class CashbookFilter { ALL, INCOME, EXPENSE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashbookScreen(
    state: MainUiState,
    onBack: (() -> Unit)? = null,
    onSaveEntry: (CashbookEntryEntity) -> Unit,
    onDeleteEntry: (CashbookEntryEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    val currency = LocalCurrencySymbol.current
    val isBn = state.prefs.language == AppLanguage.BN

    var searchQuery by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(CashbookFilter.ALL) }
    var editingEntry by remember { mutableStateOf<CashbookEntryEntity?>(null) }
    var showFormSheet by remember { mutableStateOf(false) }

    val filteredEntries = remember(state.cashbookEntries, searchQuery, filter) {
        state.cashbookEntries.filter { entry ->
            val matchesFilter = when (filter) {
                CashbookFilter.ALL -> true
                CashbookFilter.INCOME -> entry.type == CashbookType.INCOME
                CashbookFilter.EXPENSE -> entry.type == CashbookType.EXPENSE
            }
            val matchesSearch = searchQuery.isBlank() ||
                entry.category.contains(searchQuery, ignoreCase = true) ||
                entry.description.contains(searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text(strings.cashbookTitle) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingEntry = null
                    showFormSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_cashbook_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = strings.addIncomeExpense)
            }
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
                if (onBack == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = strings.cashbookTitle,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Summary Strip
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CashSummaryChip(
                        label = strings.totalIncome,
                        value = Formatters.formatMoney(state.totalCashIncome, currency),
                        color = ProfitGreen,
                        modifier = Modifier.weight(1f)
                    )
                    CashSummaryChip(
                        label = strings.totalExpense,
                        value = Formatters.formatMoney(state.totalCashExpense, currency),
                        color = PayableAmber,
                        modifier = Modifier.weight(1f)
                    )
                    val net = state.totalCashIncome - state.totalCashExpense
                    CashSummaryChip(
                        label = strings.netCashflow,
                        value = Formatters.formatMoney(net, currency),
                        color = if (net >= 0) MaterialTheme.colorScheme.primary else DueRed,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(strings.categoryLabel + " / " + strings.descriptionLabel) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = filter == CashbookFilter.ALL,
                        onClick = { filter = CashbookFilter.ALL },
                        label = { Text(strings.filterAll) }
                    )
                    FilterChip(
                        selected = filter == CashbookFilter.INCOME,
                        onClick = { filter = CashbookFilter.INCOME },
                        label = { Text(strings.incomeLabel) }
                    )
                    FilterChip(
                        selected = filter == CashbookFilter.EXPENSE,
                        onClick = { filter = CashbookFilter.EXPENSE },
                        label = { Text(strings.expenseLabel) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredEntries.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Rounded.AccountBalanceWallet,
                        title = strings.cashbookTitle,
                        subtitle = strings.cashbookEmpty,
                        actionButton = {
                            Button(onClick = {
                                editingEntry = null
                                showFormSheet = true
                            }) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.addIncomeExpense)
                            }
                        }
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 92.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredEntries, key = { it.id }) { entry ->
                            val isIncome = entry.type == CashbookType.INCOME
                            val color = if (isIncome) ProfitGreen else PayableAmber
                            Surface(
                                onClick = {
                                    editingEntry = entry
                                    showFormSheet = true
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(color.copy(alpha = 0.13f))
                                    ) {
                                        Icon(
                                            imageVector = if (isIncome) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                                            contentDescription = null,
                                            tint = color
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = CashbookCategories.localized(entry.category, isBn),
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Text(
                                            text = "${entry.description.ifBlank { entry.paymentMethod }} • ${Formatters.formatDate(entry.timestamp, state.prefs.language)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${if (isIncome) "+" else "-"}${Formatters.formatMoney(entry.amount, currency)}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = BrandMonoFont,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = color
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFormSheet) {
        CashbookEntrySheet(
            existing = editingEntry,
            isBengali = isBn,
            onDismiss = { showFormSheet = false; editingEntry = null },
            onSave = {
                onSaveEntry(it)
                showFormSheet = false
                editingEntry = null
            },
            onDelete = {
                onDeleteEntry(it)
                showFormSheet = false
                editingEntry = null
            }
        )
    }
}

@Composable
private fun CashSummaryChip(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = BrandMonoFont,
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashbookEntrySheet(
    existing: CashbookEntryEntity?,
    isBengali: Boolean,
    onDismiss: () -> Unit,
    onSave: (CashbookEntryEntity) -> Unit,
    onDelete: (CashbookEntryEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    var type by remember { mutableStateOf(existing?.type ?: CashbookType.INCOME) }
    val categories = if (type == CashbookType.INCOME) CashbookCategories.income else CashbookCategories.expense
    var category by remember { mutableStateOf(existing?.category ?: categories.first()) }
    var amountStr by remember { mutableStateOf(existing?.amount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var paymentMethod by remember { mutableStateOf(existing?.paymentMethod ?: PaymentMethod.CASH) }
    var amountError by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(strings.addIncomeExpense, style = MaterialTheme.typography.headlineSmall)

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = type == CashbookType.INCOME,
                    onClick = {
                        type = CashbookType.INCOME
                        if (category !in CashbookCategories.income) {
                            category = CashbookCategories.income.first()
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) {
                    Text(strings.incomeLabel)
                }
                SegmentedButton(
                    selected = type == CashbookType.EXPENSE,
                    onClick = {
                        type = CashbookType.EXPENSE
                        if (category !in CashbookCategories.expense) {
                            category = CashbookCategories.expense.first()
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) {
                    Text(strings.expenseLabel)
                }
            }

            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it; amountError = false },
                label = { Text(strings.amountLabel) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cashbook_amount_input")
            )

            Text(strings.categoryLabel, style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(CashbookCategories.localized(cat, isBengali)) }
                    )
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(strings.descriptionLabel) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text(strings.paymentMethodLabel, style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                PaymentMethod.all.forEach { method ->
                    FilterChip(
                        selected = paymentMethod == method,
                        onClick = { paymentMethod = method },
                        label = { Text(method) }
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (existing != null) {
                    OutlinedButton(
                        onClick = { onDelete(existing) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = DueRed)
                    }
                }
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                ) {
                    Text(strings.cancel)
                }
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull()
                        if (amt == null || amt <= 0.0) {
                            amountError = true
                        } else {
                            onSave(
                                (existing ?: CashbookEntryEntity(
                                    type = type,
                                    category = category,
                                    amount = amt
                                )).copy(
                                    type = type,
                                    category = category,
                                    amount = amt,
                                    description = description.trim(),
                                    paymentMethod = paymentMethod
                                )
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("save_cashbook_button")
                ) {
                    Text(strings.save)
                }
            }
        }
    }
}
