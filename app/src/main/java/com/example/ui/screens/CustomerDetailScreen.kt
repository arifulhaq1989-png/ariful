package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.Formatters
import com.example.core.InvoiceExportManager
import com.example.core.LocalAppStrings
import com.example.core.LocalCurrencySymbol
import com.example.data.local.CustomerEntity
import com.example.data.local.KhataEntryEntity
import com.example.data.local.KhataType
import com.example.data.local.PaymentMethod
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.BrandMonoFont
import com.example.ui.theme.DueRed
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.MainUiState
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long,
    state: MainUiState,
    onBack: () -> Unit,
    onSaveCustomer: (CustomerEntity) -> Unit,
    onDeleteCustomer: (CustomerEntity) -> Unit,
    onUpdateDueReminder: (Long, Long?) -> Unit,
    onSaveKhataEntry: (KhataEntryEntity) -> Unit,
    onDeleteKhataEntry: (KhataEntryEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    val currency = LocalCurrencySymbol.current
    val context = LocalContext.current

    val summary = state.customers.firstOrNull { it.customer.id == customerId }
    if (summary == null) {
        onBack()
        return
    }
    val customer = summary.customer
    val entries = remember(state.khataEntries, customerId) {
        state.khataEntries.filter { it.customerId == customerId }.sortedByDescending { it.timestamp }
    }

    var showEditCustomer by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    var entrySheetType by remember { mutableStateOf<String?>(null) }
    var editingEntry by remember { mutableStateOf<KhataEntryEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(customer.name, style = MaterialTheme.typography.titleLarge)
                        if (customer.phone.isNotBlank()) {
                            Text(
                                customer.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("customer_detail_back")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (customer.phone.isNotBlank()) {
                        IconButton(onClick = { InvoiceExportManager.dialPhone(context, customer.phone) }) {
                            Icon(Icons.Rounded.Call, contentDescription = strings.call)
                        }
                    }
                    IconButton(onClick = { showEditCustomer = true }) {
                        Icon(Icons.Rounded.Edit, contentDescription = strings.editCustomer)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Rounded.Delete, contentDescription = strings.deleteCustomer, tint = DueRed)
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            editingEntry = null
                            entrySheetType = KhataType.CREDIT_GIVEN
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DueRed, contentColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp)
                            .testTag("khata_gave_credit_button")
                    ) {
                        Icon(Icons.Rounded.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.gaveCreditBtn)
                    }
                    Button(
                        onClick = {
                            editingEntry = null
                            entrySheetType = KhataType.PAYMENT_RECEIVED
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ProfitGreen, contentColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp)
                            .testTag("khata_got_payment_button")
                    ) {
                        Icon(Icons.Rounded.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.gotPaymentBtn)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
            ) {
                // Summary Card
                item {
                    val netColor = when {
                        summary.netBalance > 0.01 -> DueRed
                        summary.netBalance < -0.01 -> ProfitGreen
                        else -> EmeraldPrimary
                    }
                    val netLabel = when {
                        summary.netBalance > 0.01 -> strings.youWillGet
                        summary.netBalance < -0.01 -> strings.youWillGive
                        else -> strings.settled
                    }
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(22.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(strings.netBalance, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = Formatters.formatMoney(abs(summary.netBalance), currency),
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontFamily = BrandMonoFont,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = netColor
                                    )
                                }
                                Surface(shape = CircleShape, color = netColor.copy(alpha = 0.12f)) {
                                    Text(
                                        text = netLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = netColor,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(strings.totalGiven, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            Formatters.formatMoney(summary.totalGiven, currency),
                                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = BrandMonoFont, fontWeight = FontWeight.Bold),
                                            color = DueRed
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(strings.totalReceived, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            Formatters.formatMoney(summary.totalReceived, currency),
                                            style = MaterialTheme.typography.titleSmall.copy(fontFamily = BrandMonoFont, fontWeight = FontWeight.Bold),
                                            color = ProfitGreen
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.horizontalScroll(rememberScrollState())
                            ) {
                                OutlinedButton(
                                    onClick = { showDueDatePicker = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        if (customer.dueReminderDate != null) {
                                            "${strings.dueDatePrefix} ${Formatters.formatDate(customer.dueReminderDate, state.prefs.language)}"
                                        } else {
                                            strings.setDueReminderDate
                                        }
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        val msg = "Hello ${customer.name}, friendly reminder from ${state.prefs.businessName}: your current due balance is ${Formatters.formatMoney(abs(summary.netBalance), currency)}."
                                        InvoiceExportManager.sendWhatsAppReminder(context, customer.phone, msg)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.Chat, contentDescription = null, tint = ProfitGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.remindWhatsApp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val pdf = InvoiceExportManager.generateStatementPdfFile(
                                                context = context,
                                                customer = customer,
                                                entries = entries,
                                                prefs = state.prefs,
                                                language = state.prefs.language
                                            )
                                            InvoiceExportManager.shareFile(context, pdf, "application/pdf")
                                        } catch (e: Exception) {
                                            Toast.makeText(context, e.localizedMessage ?: "Export failed", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.exportStatementPdf)
                                }
                            }
                        }
                    }
                }

                if (entries.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Rounded.Book,
                            title = customer.name,
                            subtitle = strings.ledgerEmpty
                        )
                    }
                } else {
                    items(entries, key = { it.id }) { entry ->
                        val isGiven = entry.type == KhataType.CREDIT_GIVEN
                        val color = if (isGiven) DueRed else ProfitGreen
                        Surface(
                            onClick = {
                                editingEntry = entry
                                entrySheetType = entry.type
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
                                        .background(color.copy(alpha = 0.12f))
                                ) {
                                    Icon(
                                        imageVector = if (isGiven) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.description.ifBlank {
                                            if (isGiven) strings.gaveCreditBtn else strings.gotPaymentBtn
                                        },
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "${Formatters.formatDateTime(entry.timestamp, state.prefs.language)} • ${entry.paymentMethod}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (isGiven) "+" else "-"}${Formatters.formatMoney(entry.amount, currency)}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = BrandMonoFont,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = color
                                    )
                                    Text(
                                        text = if (isGiven) strings.khataGiven else strings.khataReceived,
                                        style = MaterialTheme.typography.labelSmall,
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

    if (entrySheetType != null) {
        KhataEntrySheet(
            customerId = customerId,
            type = entrySheetType!!,
            existing = editingEntry,
            onDismiss = { entrySheetType = null; editingEntry = null },
            onSave = {
                onSaveKhataEntry(it)
                entrySheetType = null
                editingEntry = null
            },
            onDelete = {
                onDeleteKhataEntry(it)
                entrySheetType = null
                editingEntry = null
            }
        )
    }

    if (showEditCustomer) {
        CustomerFormSheet(
            existing = customer,
            onDismiss = { showEditCustomer = false },
            onSave = {
                onSaveCustomer(it)
                showEditCustomer = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(strings.deleteCustomer) },
            text = { Text(strings.deleteCustomerConfirm) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteCustomer(customer)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DueRed)
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(strings.cancel) }
            }
        )
    }

    if (showDueDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = customer.dueReminderDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDueDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    onUpdateDueReminder(customerId, datePickerState.selectedDateMillis)
                    showDueDatePicker = false
                }) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                Row {
                    if (customer.dueReminderDate != null) {
                        TextButton(onClick = {
                            onUpdateDueReminder(customerId, null)
                            showDueDatePicker = false
                        }) {
                            Text(strings.clearDueDate)
                        }
                    }
                    TextButton(onClick = { showDueDatePicker = false }) { Text(strings.cancel) }
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KhataEntrySheet(
    customerId: Long,
    type: String,
    existing: KhataEntryEntity?,
    onDismiss: () -> Unit,
    onSave: (KhataEntryEntity) -> Unit,
    onDelete: (KhataEntryEntity) -> Unit
) {
    val strings = LocalAppStrings.current
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
            Text(
                text = if (type == KhataType.CREDIT_GIVEN) strings.gaveCreditBtn else strings.gotPaymentBtn,
                style = MaterialTheme.typography.headlineSmall,
                color = if (type == KhataType.CREDIT_GIVEN) DueRed else ProfitGreen
            )
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
                    .testTag("khata_amount_input")
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(strings.descriptionLabel) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("khata_desc_input")
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
                                (existing ?: KhataEntryEntity(
                                    customerId = customerId,
                                    type = type,
                                    amount = amt
                                )).copy(
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
                        .testTag("save_khata_entry_button")
                ) {
                    Text(strings.save)
                }
            }
        }
    }
}
