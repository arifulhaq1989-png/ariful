package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.Formatters
import com.example.core.LocalAppStrings
import com.example.core.LocalCurrencySymbol
import com.example.core.ThemeMode
import com.example.ui.components.CashflowBarChart
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.KpiMetricCard
import com.example.ui.components.LanguageTogglePill
import com.example.ui.components.TransactionItemRow
import com.example.ui.theme.BrandMonoFont
import com.example.ui.theme.DueRed
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryDark
import com.example.ui.theme.PayableAmber
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.MainUiState

@Composable
fun DashboardScreen(
    state: MainUiState,
    onToggleLanguage: () -> Unit,
    onCycleTheme: () -> Unit,
    onLockApp: () -> Unit,
    onNavigateCustomers: () -> Unit,
    onOpenCustomerDetail: (Long) -> Unit,
    onNavigateCashbook: () -> Unit,
    onNavigateInvoices: () -> Unit,
    onNavigateNewInvoice: () -> Unit,
    onNavigateTransactions: () -> Unit,
    onNavigateDueReminders: () -> Unit,
    onNavigateReports: () -> Unit
) {
    val strings = LocalAppStrings.current
    val currency = LocalCurrencySymbol.current

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
        ) {
            // 1. Emerald Hero Header Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(EmeraldPrimary, EmeraldPrimaryDark)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.18f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Storefront,
                                        contentDescription = state.prefs.businessName,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = state.prefs.businessName,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = strings.appSubtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LanguageTogglePill(
                                    language = state.prefs.language,
                                    onToggle = onToggleLanguage
                                )
                                IconButton(
                                    onClick = onCycleTheme,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.16f))
                                        .testTag("dashboard_theme_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (state.prefs.themeMode == ThemeMode.DARK) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                        contentDescription = strings.themeLabel,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                if (state.prefs.isPinEnabled) {
                                    IconButton(
                                        onClick = onLockApp,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.16f))
                                            .testTag("dashboard_lock_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Lock,
                                            contentDescription = strings.lockAppNow,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = strings.netProfit,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.82f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Formatters.formatMoney(state.netProfit, currency),
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontFamily = BrandMonoFont,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color.White
                                )
                            }
                            Surface(
                                onClick = onNavigateReports,
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.testTag("dashboard_reports_chip")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                        contentDescription = strings.reportsTitle,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.reportsTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. 2x2 KPI Summary Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        KpiMetricCard(
                            title = strings.totalSales,
                            value = Formatters.formatMoney(state.totalSales, currency),
                            icon = Icons.Rounded.PointOfSale,
                            accentColor = EmeraldPrimary,
                            containerColor = EmeraldAccent.copy(alpha = 0.14f),
                            onClick = onNavigateInvoices,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_total_sales")
                        )
                        KpiMetricCard(
                            title = strings.totalDueReceivable,
                            value = Formatters.formatMoney(state.totalDueReceivable, currency),
                            icon = Icons.Rounded.AccountBalanceWallet,
                            accentColor = DueRed,
                            containerColor = DueRed.copy(alpha = 0.12f),
                            onClick = onNavigateDueReminders,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_total_due")
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        KpiMetricCard(
                            title = strings.totalPayable,
                            value = Formatters.formatMoney(state.totalPayable, currency),
                            icon = Icons.Rounded.Payments,
                            accentColor = PayableAmber,
                            containerColor = PayableAmber.copy(alpha = 0.14f),
                            onClick = onNavigateCustomers,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_total_payable")
                        )
                        KpiMetricCard(
                            title = strings.netProfit,
                            value = Formatters.formatMoney(state.netProfit, currency),
                            icon = Icons.AutoMirrored.Rounded.TrendingUp,
                            accentColor = if (state.netProfit >= 0) ProfitGreen else DueRed,
                            containerColor = if (state.netProfit >= 0) ProfitGreen.copy(alpha = 0.12f) else DueRed.copy(alpha = 0.12f),
                            onClick = onNavigateReports,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_net_profit")
                        )
                    }
                }
            }

            // 3. Quick Actions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = strings.quickActions,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        QuickActionButton(
                            icon = Icons.Rounded.PersonAdd,
                            label = strings.addCustomer,
                            onClick = onNavigateCustomers,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_add_customer")
                        )
                        QuickActionButton(
                            icon = Icons.Rounded.Book,
                            label = strings.newKhata,
                            onClick = onNavigateCustomers,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_new_khata")
                        )
                        QuickActionButton(
                            icon = Icons.Rounded.AddCard,
                            label = strings.addCashEntry,
                            onClick = onNavigateCashbook,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_cash_entry")
                        )
                        QuickActionButton(
                            icon = Icons.Rounded.ReceiptLong,
                            label = strings.createInvoice,
                            onClick = onNavigateNewInvoice,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_create_invoice")
                        )
                    }
                }
            }

            // 4. Due Reminder Alert Banner
            item {
                AnimatedVisibility(visible = state.dueCustomersCount > 0) {
                    Card(
                        onClick = onNavigateDueReminders,
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PayableAmber.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PayableAmber.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                            .testTag("dashboard_due_banner")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(PayableAmber.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = strings.dueRemindersBanner,
                                    tint = PayableAmber
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.dueRemindersBanner,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${state.dueCustomersCount} ${strings.customersHaveDue}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = strings.viewAll,
                                style = MaterialTheme.typography.labelLarge,
                                color = PayableAmber
                            )
                        }
                    }
                }
            }

            // 5. 7-Day Cashflow Chart
            item {
                CashflowBarChart(
                    points = state.last7DaysChart,
                    language = state.prefs.language
                )
            }

            // 6. Recent Activity Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.recentTransactions,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (state.unifiedTransactions.isNotEmpty()) {
                        TextButton(onClick = onNavigateTransactions) {
                            Text(strings.viewAll)
                        }
                    }
                }
            }

            if (state.unifiedTransactions.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Rounded.ReceiptLong,
                        title = strings.noTransactionsYet,
                        subtitle = strings.startTip
                    )
                }
            } else {
                items(
                    items = state.unifiedTransactions.take(6),
                    key = { it.id }
                ) { tx ->
                    TransactionItemRow(
                        tx = tx,
                        language = state.prefs.language,
                        onClick = {
                            if (tx.customerId != null) {
                                onOpenCustomerDetail(tx.customerId)
                            } else {
                                onNavigateTransactions()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
            .heightIn(min = 86.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
