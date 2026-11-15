package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PeopleOutline
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.Formatters
import com.example.core.InvoiceExportManager
import com.example.core.LocalAppStrings
import com.example.core.LocalCurrencySymbol
import com.example.data.local.CustomerEntity
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.BrandMonoFont
import com.example.ui.theme.DueRed
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PayableAmber
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.CustomerSummary
import com.example.ui.viewmodel.MainUiState
import kotlin.math.abs

private enum class CustomerFilter { ALL, DUE, PAYABLE, SETTLED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    state: MainUiState,
    onOpenCustomerDetail: (Long) -> Unit,
    onSaveCustomer: (CustomerEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    val currency = LocalCurrencySymbol.current
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(CustomerFilter.ALL) }
    var showAddSheet by remember { mutableStateOf(false) }

    val filteredCustomers = remember(state.customers, searchQuery, filter) {
        state.customers.filter { summary ->
            val matchesSearch = searchQuery.isBlank() ||
                summary.customer.name.contains(searchQuery, ignoreCase = true) ||
                summary.customer.phone.contains(searchQuery, ignoreCase = true) ||
                summary.customer.businessName.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (filter) {
                CustomerFilter.ALL -> true
                CustomerFilter.DUE -> summary.netBalance > 0.01
                CustomerFilter.PAYABLE -> summary.netBalance < -0.01
                CustomerFilter.SETTLED -> abs(summary.netBalance) <= 0.01
            }
            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = strings.addCustomer)
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
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = strings.customersTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Summary Strip
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = DueRed.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(strings.youWillGet, style = MaterialTheme.typography.labelSmall, color = DueRed)
                            Text(
                                text = Formatters.formatMoney(state.totalDueReceivable, currency),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = BrandMonoFont,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DueRed
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ProfitGreen.copy(alpha = 0.1f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(strings.youWillGive, style = MaterialTheme.typography.labelSmall, color = ProfitGreen)
                            Text(
                                text = Formatters.formatMoney(state.totalPayable, currency),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = BrandMonoFont,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ProfitGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(strings.searchCustomersHint) },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(
                        selected = filter == CustomerFilter.ALL,
                        onClick = { filter = CustomerFilter.ALL },
                        label = { Text(strings.filterAll) }
                    )
                    FilterChip(
                        selected = filter == CustomerFilter.DUE,
                        onClick = { filter = CustomerFilter.DUE },
                        label = { Text(strings.filterDue) }
                    )
                    FilterChip(
                        selected = filter == CustomerFilter.PAYABLE,
                        onClick = { filter = CustomerFilter.PAYABLE },
                        label = { Text(strings.filterPayable) }
                    )
                    FilterChip(
                        selected = filter == CustomerFilter.SETTLED,
                        onClick = { filter = CustomerFilter.SETTLED },
                        label = { Text(strings.filterSettled) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredCustomers.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Rounded.PeopleOutline,
                        title = strings.noCustomersTitle,
                        subtitle = strings.noCustomersBody,
                        actionButton = {
                            Button(onClick = { showAddSheet = true }) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.addCustomer)
                            }
                        }
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 92.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredCustomers, key = { it.customer.id }) { summary ->
                            CustomerListCard(
                                summary = summary,
                                currency = currency,
                                onClick = { onOpenCustomerDetail(summary.customer.id) },
                                onCall = { InvoiceExportManager.dialPhone(context, summary.customer.phone) },
                                onWhatsApp = {
                                    val msg = if (summary.netBalance > 0.01) {
                                        "Hello ${summary.customer.name}, friendly reminder from ${state.prefs.businessName}: your current due balance is ${Formatters.formatMoney(summary.netBalance, currency)}."
                                    } else {
                                        "Hello ${summary.customer.name}, thank you for doing business with ${state.prefs.businessName}."
                                    }
                                    InvoiceExportManager.sendWhatsAppReminder(context, summary.customer.phone, msg)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        CustomerFormSheet(
            existing = null,
            onDismiss = { showAddSheet = false },
            onSave = { customer ->
                onSaveCustomer(customer)
                showAddSheet = false
            }
        )
    }
}

@Composable
private fun CustomerListCard(
    summary: CustomerSummary,
    currency: String,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    val strings = LocalAppStrings.current
    val cust = summary.customer
    val (statusLabel, statusColor) = when {
        summary.netBalance > 0.01 -> strings.youWillGet to DueRed
        summary.netBalance < -0.01 -> strings.youWillGive to ProfitGreen
        else -> strings.settled to EmeraldPrimary
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .testTag("customer_card_${cust.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = Formatters.initials(cust.name),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cust.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = cust.phone.ifBlank { cust.address.ifBlank { "${summary.entryCount} entries" } },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (cust.phone.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onCall,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Call,
                                contentDescription = strings.call,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onWhatsApp,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Chat,
                                contentDescription = strings.remindWhatsApp,
                                tint = ProfitGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.formatMoney(abs(summary.netBalance), currency),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = BrandMonoFont,
                        fontWeight = FontWeight.Bold
                    ),
                    color = statusColor
                )
                Surface(
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerFormSheet(
    existing: CustomerEntity?,
    onDismiss: () -> Unit,
    onSave: (CustomerEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var businessName by remember { mutableStateOf(existing?.businessName ?: "") }
    var address by remember { mutableStateOf(existing?.address ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var nameError by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                text = if (existing == null) strings.addCustomer else strings.editCustomer,
                style = MaterialTheme.typography.headlineSmall
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text(strings.customerName) },
                isError = nameError,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_name_input")
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(strings.phoneNumber) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("customer_phone_input")
            )
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text(strings.businessName) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(strings.addressLabel) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(strings.notesLabel) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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
                        if (name.isBlank()) {
                            nameError = true
                        } else {
                            onSave(
                                (existing ?: CustomerEntity(name = name.trim())).copy(
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    businessName = businessName.trim(),
                                    address = address.trim(),
                                    notes = notes.trim()
                                )
                            )
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("save_customer_button")
                ) {
                    Text(strings.save)
                }
            }
        }
    }
}
