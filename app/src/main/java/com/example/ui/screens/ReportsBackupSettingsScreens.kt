package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.AppLanguage
import com.example.core.Formatters
import com.example.core.InvoiceExportManager
import com.example.core.LocalAppStrings
import com.example.core.LocalCurrencySymbol
import com.example.core.ThemeMode
import com.example.data.local.CashbookCategories
import com.example.data.local.CashbookType
import com.example.data.local.KhataType
import com.example.ui.components.CashflowBarChart
import com.example.ui.theme.BrandMonoFont
import com.example.ui.theme.DueRed
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PayableAmber
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.MainUiState
import java.util.Calendar

@Composable
fun MoreHubScreen(
    state: MainUiState,
    onNavigateCashbook: () -> Unit,
    onNavigateTransactions: () -> Unit,
    onNavigateDueReminders: () -> Unit,
    onNavigateReports: () -> Unit,
    onNavigateBackup: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val strings = LocalAppStrings.current
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            item {
                Text(strings.moreHubTitle, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(6.dp))
            }
            item {
                MoreToolCard(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = strings.cashbookTitle,
                    subtitle = "${state.cashbookEntries.size} entries",
                    accentColor = ProfitGreen,
                    onClick = onNavigateCashbook,
                    modifier = Modifier.testTag("more_cashbook_card")
                )
            }
            item {
                MoreToolCard(
                    icon = Icons.Rounded.History,
                    title = strings.allTransactions,
                    subtitle = "${state.unifiedTransactions.size} entries",
                    accentColor = EmeraldPrimary,
                    onClick = onNavigateTransactions,
                    modifier = Modifier.testTag("more_transactions_card")
                )
            }
            item {
                MoreToolCard(
                    icon = Icons.Rounded.NotificationsActive,
                    title = strings.dueReminders,
                    subtitle = "${state.dueCustomersCount} ${strings.customersHaveDue}",
                    accentColor = PayableAmber,
                    onClick = onNavigateDueReminders,
                    modifier = Modifier.testTag("more_due_reminders_card")
                )
            }
            item {
                MoreToolCard(
                    icon = Icons.Rounded.Assessment,
                    title = strings.reportsTitle,
                    subtitle = "${strings.daily} • ${strings.weekly} • ${strings.monthly}",
                    accentColor = ProfitGreen,
                    onClick = onNavigateReports,
                    modifier = Modifier.testTag("more_reports_card")
                )
            }
            item {
                MoreToolCard(
                    icon = Icons.Rounded.Backup,
                    title = strings.backupRestore,
                    subtitle = strings.exportBackupJson,
                    accentColor = EmeraldPrimary,
                    onClick = onNavigateBackup,
                    modifier = Modifier.testTag("more_backup_card")
                )
            }
            item {
                MoreToolCard(
                    icon = Icons.Rounded.Settings,
                    title = strings.settingsTitle,
                    subtitle = "${strings.businessProfile} • ${strings.languageLabel} • PIN",
                    accentColor = MaterialTheme.colorScheme.secondary,
                    onClick = onNavigateSettings,
                    modifier = Modifier.testTag("more_settings_card")
                )
            }
        }
    }
}

@Composable
private fun MoreToolCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.13f))
            ) {
                Icon(icon, contentDescription = title, tint = accentColor)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private enum class ReportPeriod { DAILY, WEEKLY, MONTHLY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    state: MainUiState,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val currency = LocalCurrencySymbol.current
    val context = LocalContext.current
    val isBn = state.prefs.language == AppLanguage.BN
    var period by remember { mutableStateOf(ReportPeriod.WEEKLY) }

    val startTime = remember(period) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        when (period) {
            ReportPeriod.DAILY -> cal.timeInMillis
            ReportPeriod.WEEKLY -> cal.timeInMillis - 6 * 86_400_000L
            ReportPeriod.MONTHLY -> cal.timeInMillis - 29 * 86_400_000L
        }
    }

    val periodInvoices = remember(state.invoices, startTime) {
        state.invoices.filter { it.invoice.timestamp >= startTime }
    }
    val periodCashbook = remember(state.cashbookEntries, startTime) {
        state.cashbookEntries.filter { it.timestamp >= startTime }
    }
    val periodKhata = remember(state.khataEntries, startTime) {
        state.khataEntries.filter { it.timestamp >= startTime }
    }

    val totalSales = periodInvoices.sumOf { it.invoice.totalAmount } +
        periodCashbook.filter { it.type == CashbookType.INCOME && it.category == "Sales" && !it.description.startsWith("INV-") }
            .sumOf { it.amount }
    val otherIncome = periodCashbook.filter { it.type == CashbookType.INCOME && it.category != "Sales" }.sumOf { it.amount }
    val totalCollections = periodKhata.filter { it.type == KhataType.PAYMENT_RECEIVED }.sumOf { it.amount }
    val totalExpenses = periodCashbook.filter { it.type == CashbookType.EXPENSE }.sumOf { it.amount }
    val netProfit = (totalSales + otherIncome) - totalExpenses

    val expenseByCategory = remember(periodCashbook) {
        periodCashbook.filter { it.type == CashbookType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.reportsTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val periodLabel = when (period) {
                            ReportPeriod.DAILY -> strings.daily
                            ReportPeriod.WEEKLY -> strings.weekly
                            ReportPeriod.MONTHLY -> strings.monthly
                        }
                        val pdf = InvoiceExportManager.generateReportPdfFile(
                            context = context,
                            periodTitle = periodLabel,
                            totalSales = totalSales + otherIncome,
                            totalCollections = totalCollections,
                            totalExpenses = totalExpenses,
                            netProfit = netProfit,
                            totalDueReceivable = state.totalDueReceivable,
                            prefs = state.prefs,
                            language = state.prefs.language
                        )
                        InvoiceExportManager.shareFile(context, pdf, "application/pdf")
                    }) {
                        Icon(Icons.Rounded.PictureAsPdf, contentDescription = strings.exportReportPdf)
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = period == ReportPeriod.DAILY,
                        onClick = { period = ReportPeriod.DAILY },
                        shape = SegmentedButtonDefaults.itemShape(0, 3)
                    ) { Text(strings.daily) }
                    SegmentedButton(
                        selected = period == ReportPeriod.WEEKLY,
                        onClick = { period = ReportPeriod.WEEKLY },
                        shape = SegmentedButtonDefaults.itemShape(1, 3)
                    ) { Text(strings.weekly) }
                    SegmentedButton(
                        selected = period == ReportPeriod.MONTHLY,
                        onClick = { period = ReportPeriod.MONTHLY },
                        shape = SegmentedButtonDefaults.itemShape(2, 3)
                    ) { Text(strings.monthly) }
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(16.dp)) {
                        ReportRow(strings.totalSales, Formatters.formatMoney(totalSales + otherIncome, currency), ProfitGreen)
                        HorizontalDivider()
                        ReportRow(strings.khataReceived, Formatters.formatMoney(totalCollections, currency), EmeraldPrimary)
                        HorizontalDivider()
                        ReportRow(strings.totalExpense, Formatters.formatMoney(totalExpenses, currency), PayableAmber)
                        HorizontalDivider()
                        ReportRow(strings.netProfit, Formatters.formatMoney(netProfit, currency), if (netProfit >= 0) ProfitGreen else DueRed)
                    }
                }

                CashflowBarChart(points = state.last7DaysChart, language = state.prefs.language)

                if (expenseByCategory.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(16.dp)) {
                            Text("${strings.expenseLabel} (${strings.categoryLabel})", style = MaterialTheme.typography.titleMedium)
                            val maxCat = expenseByCategory.first().value.coerceAtLeast(1.0)
                            expenseByCategory.forEach { (cat, amt) ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(CashbookCategories.localized(cat, isBn), style = MaterialTheme.typography.bodyMedium)
                                        Text(Formatters.formatMoney(amt, currency), fontFamily = BrandMonoFont)
                                    }
                                    LinearProgressIndicator(
                                        progress = { (amt / maxCat).toFloat().coerceIn(0f, 1f) },
                                        color = PayableAmber,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        val periodLabel = when (period) {
                            ReportPeriod.DAILY -> strings.daily
                            ReportPeriod.WEEKLY -> strings.weekly
                            ReportPeriod.MONTHLY -> strings.monthly
                        }
                        val pdf = InvoiceExportManager.generateReportPdfFile(
                            context = context,
                            periodTitle = periodLabel,
                            totalSales = totalSales + otherIncome,
                            totalCollections = totalCollections,
                            totalExpenses = totalExpenses,
                            netProfit = netProfit,
                            totalDueReceivable = state.totalDueReceivable,
                            prefs = state.prefs,
                            language = state.prefs.language
                        )
                        InvoiceExportManager.shareFile(context, pdf, "application/pdf")
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                ) {
                    Icon(Icons.Rounded.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.exportReportPdf)
                }
            }
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String, color: Color) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = BrandMonoFont, fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    state: MainUiState,
    onBack: () -> Unit,
    onExportBackup: (android.net.Uri, (Boolean) -> Unit) -> Unit,
    onRestoreBackup: (android.net.Uri, (Boolean) -> Unit) -> Unit,
    onSeedDemo: () -> Unit,
    onClearAll: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            onExportBackup(uri) { ok ->
                Toast.makeText(context, if (ok) strings.backupSuccess else strings.backupError, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onRestoreBackup(uri) { ok ->
                Toast.makeText(context, if (ok) strings.restoreSuccess else strings.backupError, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.backupRestore) },
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(Icons.Rounded.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(strings.localVaultActive, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(strings.cloudSyncNotice, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            val backupDate = if (state.prefs.lastBackupAt > 0L) {
                                Formatters.formatDateTime(state.prefs.lastBackupAt, state.prefs.language)
                            } else {
                                strings.neverBackedUp
                            }
                            Text(
                                "${strings.lastBackupLabel}: $backupDate",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Button(
                    onClick = { exportLauncher.launch("bizsphere_backup_${System.currentTimeMillis()}.json") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("export_backup_button")
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.exportBackupJson)
                }

                OutlinedButton(
                    onClick = { restoreLauncher.launch(arrayOf("application/json", "*/*")) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("restore_backup_button")
                ) {
                    Icon(Icons.Rounded.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.restoreBackupJson)
                }

                HorizontalDivider()

                OutlinedButton(
                    onClick = {
                        onSeedDemo()
                        Toast.makeText(context, strings.savedSuccess, Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("load_demo_data_button")
                ) {
                    Icon(Icons.Rounded.Science, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.loadDemoData)
                }

                OutlinedButton(
                    onClick = { showClearConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DueRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                ) {
                    Icon(Icons.Rounded.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.clearAllData)
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(strings.clearAllData) },
            text = { Text(strings.clearAllDataConfirm) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DueRed)
                ) { Text(strings.delete) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) { Text(strings.cancel) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: MainUiState,
    onBack: () -> Unit,
    onUpdateProfile: (String, String, String, String) -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetTheme: (ThemeMode) -> Unit,
    onSetCurrency: (String) -> Unit,
    onSetPin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onLockAppNow: () -> Unit,
    onLogout: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current

    var bizName by remember(state.prefs.businessName) { mutableStateOf(state.prefs.businessName) }
    var ownerName by remember(state.prefs.ownerName) { mutableStateOf(state.prefs.ownerName) }
    var phone by remember(state.prefs.businessPhone) { mutableStateOf(state.prefs.businessPhone) }
    var address by remember(state.prefs.businessAddress) { mutableStateOf(state.prefs.businessAddress) }

    var showPinDialog by remember { mutableStateOf(false) }
    var newPinInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settingsTitle) },
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Business Profile
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(16.dp)) {
                        Text(strings.businessProfile, style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = bizName,
                            onValueChange = { bizName = it },
                            label = { Text(strings.businessName) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text(strings.ownerName) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text(strings.businessPhone) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text(strings.businessAddress) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                onUpdateProfile(bizName, ownerName, phone, address)
                                Toast.makeText(context, strings.savedSuccess, Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.save)
                        }
                    }
                }

                // Preferences
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
                        Text(strings.preferences, style = MaterialTheme.typography.titleMedium)

                        Text(strings.languageLabel, style = MaterialTheme.typography.labelLarge)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = state.prefs.language == AppLanguage.EN,
                                onClick = { onSetLanguage(AppLanguage.EN) },
                                shape = SegmentedButtonDefaults.itemShape(0, 2)
                            ) { Text("English") }
                            SegmentedButton(
                                selected = state.prefs.language == AppLanguage.BN,
                                onClick = { onSetLanguage(AppLanguage.BN) },
                                shape = SegmentedButtonDefaults.itemShape(1, 2)
                            ) { Text("বাংলা") }
                        }

                        Text(strings.themeLabel, style = MaterialTheme.typography.labelLarge)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = state.prefs.themeMode == ThemeMode.SYSTEM,
                                onClick = { onSetTheme(ThemeMode.SYSTEM) },
                                shape = SegmentedButtonDefaults.itemShape(0, 3)
                            ) { Text(strings.themeSystem) }
                            SegmentedButton(
                                selected = state.prefs.themeMode == ThemeMode.LIGHT,
                                onClick = { onSetTheme(ThemeMode.LIGHT) },
                                shape = SegmentedButtonDefaults.itemShape(1, 3)
                            ) { Text(strings.themeLight) }
                            SegmentedButton(
                                selected = state.prefs.themeMode == ThemeMode.DARK,
                                onClick = { onSetTheme(ThemeMode.DARK) },
                                shape = SegmentedButtonDefaults.itemShape(2, 3)
                            ) { Text(strings.themeDark) }
                        }

                        Text(strings.currencyLabel, style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("৳", "$", "₹", "€").forEach { sym ->
                                FilterChip(
                                    selected = state.prefs.currencySymbol == sym,
                                    onClick = { onSetCurrency(sym) },
                                    label = { Text(sym) }
                                )
                            }
                        }
                    }
                }

                // Security
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
                        Text(strings.securityLabel, style = MaterialTheme.typography.titleMedium)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.pinLockToggle, modifier = Modifier.weight(1f))
                            Switch(
                                checked = state.prefs.isPinEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled) {
                                        newPinInput = ""
                                        showPinDialog = true
                                    } else {
                                        onDisablePin()
                                    }
                                }
                            )
                        }

                        if (state.prefs.isPinEnabled) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        newPinInput = ""
                                        showPinDialog = true
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(strings.setNewPin)
                                }
                                Button(
                                    onClick = onLockAppNow,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.lockAppNow)
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = onLogout,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DueRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.logoutAccount)
                        }
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(strings.setNewPin) },
            text = {
                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) newPinInput = it },
                    label = { Text(strings.enter4DigitPin) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    enabled = newPinInput.length == 4,
                    onClick = {
                        onSetPin(newPinInput)
                        showPinDialog = false
                        Toast.makeText(context, strings.savedSuccess, Toast.LENGTH_SHORT).show()
                    }
                ) { Text(strings.save) }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text(strings.cancel) }
            }
        )
    }
}
