package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.AppLanguage
import com.example.core.Formatters
import com.example.core.LocalAppStrings
import com.example.core.LocalCurrencySymbol
import com.example.ui.theme.BrandMonoFont
import com.example.ui.theme.DueRed
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.PayableAmber
import com.example.ui.theme.ProfitGreen
import com.example.ui.viewmodel.DailyChartPoint
import com.example.ui.viewmodel.UnifiedTransaction
import com.example.ui.viewmodel.UnifiedTxKind

@Composable
fun LanguageTogglePill(
    language: AppLanguage,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onToggle,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
            .heightIn(min = 40.dp)
            .testTag("language_toggle_button")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Translate,
                contentDescription = "Switch Language",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (language == AppLanguage.EN) "বাংলা" else "EN",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(containerColor)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (onClick != null) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = BrandMonoFont,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TransactionItemRow(
    tx: UnifiedTransaction,
    language: AppLanguage,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currency = LocalCurrencySymbol.current
    val (icon, tint, bgTint, prefix) = when (tx.kind) {
        UnifiedTxKind.KHATA_GIVEN -> Quad(
            Icons.Rounded.ArrowUpward,
            DueRed,
            DueRed.copy(alpha = 0.12f),
            "-"
        )
        UnifiedTxKind.KHATA_RECEIVED -> Quad(
            Icons.Rounded.ArrowDownward,
            ProfitGreen,
            ProfitGreen.copy(alpha = 0.12f),
            "+"
        )
        UnifiedTxKind.INCOME -> Quad(
            Icons.Rounded.ArrowDownward,
            ProfitGreen,
            ProfitGreen.copy(alpha = 0.12f),
            "+"
        )
        UnifiedTxKind.EXPENSE -> Quad(
            Icons.Rounded.ArrowUpward,
            PayableAmber,
            PayableAmber.copy(alpha = 0.14f),
            "-"
        )
        UnifiedTxKind.INVOICE -> Quad(
            Icons.Rounded.ReceiptLong,
            EmeraldPrimary,
            EmeraldAccent.copy(alpha = 0.14f),
            ""
        )
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgTint)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tx.title,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${tx.subtitle} • ${Formatters.formatDate(tx.timestamp, language)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$prefix${Formatters.formatMoney(tx.amount, currency)}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = BrandMonoFont,
                        fontWeight = FontWeight.Bold
                    ),
                    color = tint
                )
                Text(
                    text = tx.paymentMethod,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun CashflowBarChart(
    points: List<DailyChartPoint>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val maxVal = (points.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0).coerceAtLeast(100.0)
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 550),
        label = "chartAnim"
    )
    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = PayableAmber
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
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
                    Text(
                        text = strings.cashflowTrend,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.last7Days,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LegendDot(color = incomeColor, label = strings.incomeLabel)
                    LegendDot(color = expenseColor, label = strings.expenseLabel)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val count = points.size.coerceAtLeast(1)
                val groupWidth = size.width / count
                val barWidth = (groupWidth * 0.28f).coerceIn(8.dp.toPx(), 18.dp.toPx())
                val gap = 4.dp.toPx()

                // Horizontal guidelines
                for (i in 0..2) {
                    val yPos = size.height * (i / 2f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, yPos),
                        end = Offset(size.width, yPos),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                points.forEachIndexed { idx, pt ->
                    val centerX = groupWidth * idx + groupWidth / 2f
                    val incRatio = ((pt.income / maxVal).toFloat() * animProgress).coerceIn(0f, 1f)
                    val expRatio = ((pt.expense / maxVal).toFloat() * animProgress).coerceIn(0f, 1f)

                    val incHeight = (size.height * incRatio).coerceAtLeast(if (pt.income > 0) 6.dp.toPx() else 2.dp.toPx())
                    val expHeight = (size.height * expRatio).coerceAtLeast(if (pt.expense > 0) 6.dp.toPx() else 2.dp.toPx())

                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(incomeColor, incomeColor.copy(alpha = 0.7f))
                        ),
                        topLeft = Offset(centerX - barWidth - gap / 2f, size.height - incHeight),
                        size = Size(barWidth, incHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(expenseColor, expenseColor.copy(alpha = 0.7f))
                        ),
                        topLeft = Offset(centerX + gap / 2f, size.height - expHeight),
                        size = Size(barWidth, expHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                points.forEach { pt ->
                    Text(
                        text = Formatters.formatShortDay(pt.timestamp, language),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionButton: (@Composable () -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(22.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (actionButton != null) {
                Spacer(modifier = Modifier.height(16.dp))
                actionButton()
            }
        }
    }
}
