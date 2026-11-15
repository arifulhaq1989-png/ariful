package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.Formatters
import com.example.core.InvoiceExportManager
import com.example.core.LocalAppStrings
import com.example.core.LocalCurrencySymbol
import com.example.data.local.InvoiceEntity
import com.example.data.local.InvoiceItemEntity
import com.example.data.local.InvoiceWithItems
import com.example.ui.components.EmptyStateCard
import com.example.ui.theme.BrandMonoFont
import com.example.ui.theme.DueRed
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PayableAmber
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.MainUiState
import java.io.File

private data class DraftInvoiceLine(
    val id: Int,
    val name: String = "",
    val qty: String = "1",
    val price: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicesScreen(
    state: MainUiState,
    openBuilderInitially: Boolean,
    onConsumeOpenBuilder: () -> Unit,
    generateNextInvoiceNumber: suspend () -> String,
    onSaveInvoice: (InvoiceEntity, List<InvoiceItemEntity>, Boolean, (Long) -> Unit) -> Unit,
    onDeleteInvoice: (InvoiceEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    val currency = LocalCurrencySymbol.current
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var showBuilder by remember { mutableStateOf(openBuilderInitially) }
    var previewInvoice by remember { mutableStateOf<InvoiceWithItems?>(null) }

    LaunchedEffect(openBuilderInitially) {
        if (openBuilderInitially) {
            showBuilder = true
            onConsumeOpenBuilder()
        }
    }

    var pendingExportFile by remember { mutableStateOf<File?>(null) }
    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        val src = pendingExportFile
        if (uri != null && src != null) {
            InvoiceExportManager.copyFileToUri(context, src, uri)
            Toast.makeText(context, strings.exportedToDevice, Toast.LENGTH_SHORT).show()
        }
    }
    val savePngLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        val src = pendingExportFile
        if (uri != null && src != null) {
            InvoiceExportManager.copyFileToUri(context, src, uri)
            Toast.makeText(context, strings.exportedToDevice, Toast.LENGTH_SHORT).show()
        }
    }

    if (showBuilder) {
        BackHandler { showBuilder = false }
        InvoiceBuilderScreen(
            state = state,
            generateNextInvoiceNumber = generateNextInvoiceNumber,
            onBack = { showBuilder = false },
            onSave = { inv, items, sync ->
                onSaveInvoice(inv, items, sync) { savedId ->
                    showBuilder = false
                    previewInvoice = InvoiceWithItems(inv.copy(id = savedId), items)
                }
            }
        )
        return
    }

    val filteredInvoices = remember(state.invoices, searchQuery) {
        state.invoices.filter {
            searchQuery.isBlank() ||
                it.invoice.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                it.invoice.customerName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBuilder = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("create_invoice_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = strings.newInvoice)
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
                Text(strings.invoicesTitle, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(10.dp))

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
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredInvoices.isEmpty()) {
                    EmptyStateCard(
                        icon = Icons.Rounded.ReceiptLong,
                        title = strings.noInvoicesTitle,
                        subtitle = strings.noInvoicesBody,
                        actionButton = {
                            Button(onClick = { showBuilder = true }) {
                                Icon(Icons.Rounded.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.newInvoice)
                            }
                        }
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 92.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredInvoices, key = { it.invoice.id }) { item ->
                            val inv = item.invoice
                            val (stamp, stampColor) = when {
                                inv.balanceDue <= 0.01 -> strings.paidStamp to ProfitGreen
                                inv.paidAmount > 0.0 -> strings.partialStamp to PayableAmber
                                else -> strings.unpaidStamp to DueRed
                            }
                            Surface(
                                onClick = { previewInvoice = item },
                                shape = RoundedCornerShape(18.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                                    .testTag("invoice_card_${inv.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                    ) {
                                        Icon(
                                            Icons.Rounded.ReceiptLong,
                                            contentDescription = inv.invoiceNumber,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${inv.invoiceNumber} • ${inv.customerName}",
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${Formatters.formatDate(inv.timestamp, state.prefs.language)} • ${item.items.size} items",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = Formatters.formatMoney(inv.totalAmount, currency),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = BrandMonoFont,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Surface(shape = CircleShape, color = stampColor.copy(alpha = 0.13f)) {
                                            Text(
                                                text = stamp,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = stampColor,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
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

    if (previewInvoice != null) {
        val invItem = previewInvoice!!
        val inv = invItem.invoice
        ModalBottomSheet(onDismissRequest = { previewInvoice = null }) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.previewInvoice, style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = {
                        onDeleteInvoice(inv)
                        previewInvoice = null
                    }) {
                        Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = DueRed)
                    }
                }

                // Visual Invoice Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(EmeraldPrimary)
                                .padding(16.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(state.prefs.businessName, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                    if (state.prefs.businessPhone.isNotBlank()) {
                                        Text(state.prefs.businessPhone, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(inv.invoiceNumber, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                    Text(Formatters.formatDate(inv.timestamp, state.prefs.language), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                                }
                            }
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(inv.customerName, style = MaterialTheme.typography.titleMedium)
                            if (inv.customerPhone.isNotBlank()) {
                                Text(inv.customerPhone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            HorizontalDivider()
                            invItem.items.forEach { line ->
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("${line.name} × ${if (line.quantity % 1.0 == 0.0) line.quantity.toInt() else line.quantity}", modifier = Modifier.weight(1f))
                                    Text(
                                        Formatters.formatMoney(line.total, currency),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = BrandMonoFont)
                                    )
                                }
                            }
                            HorizontalDivider()
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(strings.grandTotal, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    Formatters.formatMoney(inv.totalAmount, currency),
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = BrandMonoFont, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(strings.paidAmount, style = MaterialTheme.typography.bodyMedium)
                                Text(Formatters.formatMoney(inv.paidAmount, currency), style = MaterialTheme.typography.bodyMedium.copy(fontFamily = BrandMonoFont))
                            }
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(strings.balanceDue, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    Formatters.formatMoney(inv.balanceDue, currency),
                                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = BrandMonoFont, fontWeight = FontWeight.Bold),
                                    color = if (inv.balanceDue > 0.01) DueRed else ProfitGreen
                                )
                            }
                        }
                    }
                }

                // Export & Share Action Grid
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            val pdf = InvoiceExportManager.generateInvoicePdfFile(context, invItem, state.prefs, state.prefs.language)
                            pendingExportFile = pdf
                            savePdfLauncher.launch("${inv.invoiceNumber}.pdf")
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("download_invoice_pdf")
                    ) {
                        Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.downloadPdf)
                    }
                    OutlinedButton(
                        onClick = {
                            val png = InvoiceExportManager.generateInvoiceImageFile(context, invItem, state.prefs, state.prefs.language)
                            pendingExportFile = png
                            savePngLauncher.launch("${inv.invoiceNumber}.png")
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("download_invoice_png")
                    ) {
                        Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.downloadImage)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            val png = InvoiceExportManager.generateInvoiceImageFile(context, invItem, state.prefs, state.prefs.language)
                            val caption = "${state.prefs.businessName} - ${inv.invoiceNumber} (${Formatters.formatMoney(inv.totalAmount, currency)})"
                            InvoiceExportManager.shareFile(context, png, "image/png", caption, preferWhatsApp = true)
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("share_invoice_whatsapp")
                    ) {
                        Icon(Icons.Rounded.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.shareWhatsApp)
                    }
                    OutlinedButton(
                        onClick = {
                            val pdf = InvoiceExportManager.generateInvoicePdfFile(context, invItem, state.prefs, state.prefs.language)
                            val caption = "${state.prefs.businessName} - ${inv.invoiceNumber}"
                            InvoiceExportManager.shareFile(context, pdf, "application/pdf", caption, preferWhatsApp = false)
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("share_invoice_pdf")
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${strings.shareMore} PDF")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceBuilderScreen(
    state: MainUiState,
    generateNextInvoiceNumber: suspend () -> String,
    onBack: () -> Unit,
    onSave: (InvoiceEntity, List<InvoiceItemEntity>, Boolean) -> Unit
) {
    val strings = LocalAppStrings.current
    val currency = LocalCurrencySymbol.current

    var invoiceNumber by remember { mutableStateOf("INV-0001") }
    LaunchedEffect(Unit) {
        invoiceNumber = generateNextInvoiceNumber()
    }

    var selectedCustomerId by remember { mutableStateOf<Long?>(null) }
    var customerName by remember { mutableStateOf(strings.walkInCustomer) }
    var customerPhone by remember { mutableStateOf("") }
    var discountStr by remember { mutableStateOf("0") }
    var taxPercentStr by remember { mutableStateOf("0") }
    var paidStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var syncToKhata by remember { mutableStateOf(true) }
    var nextLineId by remember { mutableStateOf(2) }
    val lines = remember { mutableStateListOf(DraftInvoiceLine(1)) }
    var validationError by remember { mutableStateOf(false) }

    val parsedItems = lines.mapNotNull { line ->
        val qty = line.qty.toDoubleOrNull() ?: 0.0
        val price = line.price.toDoubleOrNull() ?: 0.0
        if (line.name.isNotBlank() && qty > 0 && price >= 0) {
            InvoiceItemEntity(
                invoiceId = 0L,
                name = line.name.trim(),
                quantity = qty,
                unitPrice = price,
                total = qty * price
            )
        } else null
    }
    val subtotal = parsedItems.sumOf { it.total }
    val discount = (discountStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val taxPercent = (taxPercentStr.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
    val taxable = (subtotal - discount).coerceAtLeast(0.0)
    val taxAmount = taxable * (taxPercent / 100.0)
    val grandTotal = taxable + taxAmount
    val paidAmount = (paidStr.toDoubleOrNull() ?: grandTotal).coerceIn(0.0, grandTotal)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.newInvoice) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = invoiceNumber,
                onValueChange = { invoiceNumber = it },
                label = { Text(strings.invoiceNo) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (state.customers.isNotEmpty()) {
                Text(strings.selectCustomer, style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    FilterChip(
                        selected = selectedCustomerId == null,
                        onClick = {
                            selectedCustomerId = null
                            customerName = strings.walkInCustomer
                            customerPhone = ""
                        },
                        label = { Text(strings.walkInCustomer) }
                    )
                    state.customers.forEach { summary ->
                        FilterChip(
                            selected = selectedCustomerId == summary.customer.id,
                            onClick = {
                                selectedCustomerId = summary.customer.id
                                customerName = summary.customer.name
                                customerPhone = summary.customer.phone
                            },
                            label = { Text(summary.customer.name) }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text(strings.customerName) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = customerPhone,
                    onValueChange = { customerPhone = it },
                    label = { Text(strings.phoneNumber) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider()

            lines.forEachIndexed { index, line ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = line.name,
                        onValueChange = { lines[index] = line.copy(name = it); validationError = false },
                        label = { Text(strings.itemName) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.8f)
                    )
                    OutlinedTextField(
                        value = line.qty,
                        onValueChange = { lines[index] = line.copy(qty = it) },
                        label = { Text(strings.qty) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.8f)
                    )
                    OutlinedTextField(
                        value = line.price,
                        onValueChange = { lines[index] = line.copy(price = it); validationError = false },
                        label = { Text(strings.unitPrice) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.1f)
                    )
                    if (lines.size > 1) {
                        IconButton(onClick = { lines.removeAt(index) }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Remove Item", tint = DueRed)
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { lines.add(DraftInvoiceLine(nextLineId++)) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.addItem)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = discountStr,
                    onValueChange = { discountStr = it },
                    label = { Text(strings.discount) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = taxPercentStr,
                    onValueChange = { taxPercentStr = it },
                    label = { Text(strings.taxPercent) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = paidStr,
                    onValueChange = { paidStr = it },
                    label = { Text(strings.paidAmount) },
                    placeholder = { Text(grandTotal.toInt().toString()) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(strings.notesLabel) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.recordInKhataToggle, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(checked = syncToKhata, onCheckedChange = { syncToKhata = it })
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(strings.subtotal)
                        Text(Formatters.formatMoney(subtotal, currency), fontFamily = BrandMonoFont)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(strings.grandTotal, style = MaterialTheme.typography.titleMedium)
                        Text(
                            Formatters.formatMoney(grandTotal, currency),
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = BrandMonoFont, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            if (validationError) {
                Text(strings.fillRequiredFields, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    if (parsedItems.isEmpty() || customerName.isBlank()) {
                        validationError = true
                    } else {
                        val invoice = InvoiceEntity(
                            invoiceNumber = invoiceNumber.trim().ifBlank { "INV-0001" },
                            customerId = selectedCustomerId,
                            customerName = customerName.trim(),
                            customerPhone = customerPhone.trim(),
                            subtotal = subtotal,
                            discount = discount,
                            taxPercent = taxPercent,
                            taxAmount = taxAmount,
                            totalAmount = grandTotal,
                            paidAmount = paidAmount,
                            notes = notes.trim()
                        )
                        onSave(invoice, parsedItems, syncToKhata)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("save_invoice_button")
            ) {
                Text(strings.saveInvoice)
            }
        }
    }
}
