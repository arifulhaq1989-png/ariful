package com.example.core

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.CustomerEntity
import com.example.data.local.InvoiceWithItems
import com.example.data.local.KhataEntryEntity
import com.example.data.local.KhataType
import com.example.data.prefs.UserPreferences
import java.io.File
import java.io.FileOutputStream

object InvoiceExportManager {

    private fun getInvoiceCacheDir(context: Context): File {
        val dir = File(context.cacheDir, "invoices")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun generateInvoicePdfFile(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        prefs: UserPreferences,
        language: AppLanguage
    ): File {
        val file = File(getInvoiceCacheDir(context), "${invoiceWithItems.invoice.invoiceNumber}.pdf")
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 72dpi
        val page = document.startPage(pageInfo)
        drawInvoiceOnCanvas(
            canvas = page.canvas,
            width = 595f,
            height = 842f,
            invoiceWithItems = invoiceWithItems,
            prefs = prefs,
            language = language
        )
        document.finishPage(page)
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    fun generateInvoiceImageFile(
        context: Context,
        invoiceWithItems: InvoiceWithItems,
        prefs: UserPreferences,
        language: AppLanguage
    ): File {
        val file = File(getInvoiceCacheDir(context), "${invoiceWithItems.invoice.invoiceNumber}.png")
        val width = 1190
        val height = 1684
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.save()
        canvas.scale(2f, 2f)
        drawInvoiceOnCanvas(
            canvas = canvas,
            width = 595f,
            height = 842f,
            invoiceWithItems = invoiceWithItems,
            prefs = prefs,
            language = language
        )
        canvas.restore()
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        return file
    }

    private fun drawInvoiceOnCanvas(
        canvas: Canvas,
        width: Float,
        height: Float,
        invoiceWithItems: InvoiceWithItems,
        prefs: UserPreferences,
        language: AppLanguage
    ) {
        val inv = invoiceWithItems.invoice
        val items = invoiceWithItems.items
        val currency = prefs.currencySymbol

        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // Emerald header band
        val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#06754A") }
        canvas.drawRect(0f, 0f, width, 130f, greenPaint)

        val headerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D9F5E8")
            textSize = 11f
        }
        val invoiceBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 20f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val invoiceSubRightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D9F5E8")
            textSize = 11f
            textAlign = Paint.Align.RIGHT
        }

        canvas.drawText(prefs.businessName.ifBlank { "BizSphere Store" }, 36f, 48f, headerTitlePaint)
        if (prefs.businessPhone.isNotBlank()) {
            canvas.drawText("Tel: ${prefs.businessPhone}", 36f, 70f, headerSubPaint)
        }
        if (prefs.businessAddress.isNotBlank()) {
            canvas.drawText(prefs.businessAddress, 36f, 88f, headerSubPaint)
        }
        canvas.drawText("BizSphere Digital Khata", 36f, 112f, headerSubPaint)

        canvas.drawText("INVOICE", width - 36f, 48f, invoiceBadgePaint)
        canvas.drawText(inv.invoiceNumber, width - 36f, 70f, invoiceSubRightPaint)
        canvas.drawText(Formatters.formatDate(inv.timestamp, language), width - 36f, 88f, invoiceSubRightPaint)

        // Bill To section
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4A6355")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0E1F16")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0E1F16")
            textSize = 11.5f
        }
        val rightTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0E1F16")
            textSize = 11.5f
            textAlign = Paint.Align.RIGHT
        }
        val rightBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#06754A")
            textSize = 14f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        var y = 165f
        canvas.drawText("BILL TO", 36f, y, labelPaint)
        y += 18f
        canvas.drawText(inv.customerName, 36f, y, textBoldPaint)
        if (inv.customerPhone.isNotBlank()) {
            y += 16f
            canvas.drawText(inv.customerPhone, 36f, y, textPaint)
        }

        // Status stamp on right
        val isPaid = inv.balanceDue <= 0.01
        val stampColor = when {
            isPaid -> Color.parseColor("#059669")
            inv.paidAmount > 0 -> Color.parseColor("#D97706")
            else -> Color.parseColor("#DC2626")
        }
        val stampText = when {
            isPaid -> "PAID"
            inv.paidAmount > 0 -> "PARTIAL"
            else -> "DUE"
        }
        val stampBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stampColor
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val stampTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stampColor
            textSize = 13f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val stampRect = RectF(width - 130f, 155f, width - 36f, 185f)
        canvas.drawRoundRect(stampRect, 8f, 8f, stampBoxPaint)
        canvas.drawText(stampText, stampRect.centerX(), stampRect.centerY() + 5f, stampTextPaint)

        // Table Header
        y = 230f
        val tableHeaderBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#EAF8F1") }
        canvas.drawRoundRect(RectF(36f, y, width - 36f, y + 28f), 6f, 6f, tableHeaderBg)

        canvas.drawText("ITEM / DESCRIPTION", 48f, y + 18f, labelPaint)
        val colQtyX = 350f
        val colRateX = 440f
        val colTotalX = width - 48f
        val rightLabelPaint = Paint(labelPaint).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText("QTY", colQtyX, y + 18f, rightLabelPaint)
        canvas.drawText("RATE", colRateX, y + 18f, rightLabelPaint)
        canvas.drawText("AMOUNT", colTotalX, y + 18f, rightLabelPaint)

        y += 46f
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#DCEBE2")
            strokeWidth = 1f
        }

        items.take(16).forEach { item ->
            val cleanName = if (item.name.length > 34) item.name.take(32) + "..." else item.name
            canvas.drawText(cleanName, 48f, y, textPaint)
            val qtyStr = if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else String.format("%.2f", item.quantity)
            canvas.drawText(qtyStr, colQtyX, y, rightTextPaint)
            canvas.drawText(Formatters.formatMoney(item.unitPrice, currency), colRateX, y, rightTextPaint)
            canvas.drawText(Formatters.formatMoney(item.total, currency), colTotalX, y, rightTextPaint)
            y += 10f
            canvas.drawLine(36f, y, width - 36f, y, dividerPaint)
            y += 22f
        }

        // Totals Summary Box
        y = (y + 12f).coerceAtLeast(520f).coerceAtMost(660f)
        val summaryXLabel = 350f
        val summaryXVal = width - 44f
        canvas.drawText("Subtotal:", summaryXLabel, y, textPaint)
        canvas.drawText(Formatters.formatMoney(inv.subtotal, currency), summaryXVal, y, rightTextPaint)
        y += 20f

        if (inv.discount > 0.0) {
            canvas.drawText("Discount:", summaryXLabel, y, textPaint)
            canvas.drawText("-${Formatters.formatMoney(inv.discount, currency)}", summaryXVal, y, rightTextPaint)
            y += 20f
        }
        if (inv.taxAmount > 0.0) {
            canvas.drawText("Tax (${inv.taxPercent.toInt()}%):", summaryXLabel, y, textPaint)
            canvas.drawText(Formatters.formatMoney(inv.taxAmount, currency), summaryXVal, y, rightTextPaint)
            y += 20f
        }

        canvas.drawLine(summaryXLabel, y - 6f, width - 36f, y - 6f, dividerPaint)
        y += 10f
        canvas.drawText("Grand Total:", summaryXLabel, y, textBoldPaint)
        canvas.drawText(Formatters.formatMoney(inv.totalAmount, currency), summaryXVal, y, rightBoldPaint)
        y += 22f

        canvas.drawText("Paid:", summaryXLabel, y, textPaint)
        canvas.drawText(Formatters.formatMoney(inv.paidAmount, currency), summaryXVal, y, rightTextPaint)
        y += 20f

        val duePaint = Paint(rightBoldPaint).apply {
            color = if (inv.balanceDue > 0.01) Color.parseColor("#DC2626") else Color.parseColor("#059669")
            textSize = 12.5f
        }
        canvas.drawText("Balance Due:", summaryXLabel, y, textBoldPaint)
        canvas.drawText(Formatters.formatMoney(inv.balanceDue, currency), summaryXVal, y, duePaint)

        // Notes & Footer
        if (inv.notes.isNotBlank()) {
            canvas.drawText("Notes:", 36f, 735f, labelPaint)
            canvas.drawText(inv.notes.take(70), 36f, 752f, textPaint)
        }

        canvas.drawLine(36f, 795f, width - 36f, 795f, dividerPaint)
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4A6355")
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Generated by BizSphere – Digital Khata • ${prefs.businessName}",
            width / 2f,
            815f,
            footerPaint
        )
    }

    fun generateStatementPdfFile(
        context: Context,
        customer: CustomerEntity,
        entries: List<KhataEntryEntity>,
        prefs: UserPreferences,
        language: AppLanguage
    ): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) dir.mkdirs()
        val safeName = customer.name.replace(Regex("[^A-Za-z0-9]"), "_").take(20)
        val file = File(dir, "Statement_${safeName}.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val currency = prefs.currencySymbol

        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        val greenPaint = Paint().apply { color = Color.parseColor("#06754A") }
        canvas.drawRect(0f, 0f, 595f, 100f, greenPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D9F5E8")
            textSize = 11f
        }
        canvas.drawText("${prefs.businessName} — Customer Statement", 36f, 44f, titlePaint)
        canvas.drawText("Customer: ${customer.name} (${customer.phone})", 36f, 68f, subPaint)
        canvas.drawText("Generated: ${Formatters.formatDateTime(System.currentTimeMillis(), language)}", 36f, 84f, subPaint)

        val totalGiven = entries.filter { it.type == KhataType.CREDIT_GIVEN }.sumOf { it.amount }
        val totalReceived = entries.filter { it.type == KhataType.PAYMENT_RECEIVED }.sumOf { it.amount }
        val netDue = totalGiven - totalReceived

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0E1F16")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0E1F16")
            textSize = 11f
        }
        val rightPaint = Paint(bodyPaint).apply { textAlign = Paint.Align.RIGHT }

        var y = 132f
        canvas.drawText("Total Credit Given: ${Formatters.formatMoney(totalGiven, currency)}", 36f, y, boldPaint)
        canvas.drawText("Total Paid: ${Formatters.formatMoney(totalReceived, currency)}", 235f, y, boldPaint)
        canvas.drawText("Net Due: ${Formatters.formatMoney(netDue, currency)}", 420f, y, boldPaint)

        y += 28f
        val linePaint = Paint().apply { color = Color.parseColor("#DCEBE2"); strokeWidth = 1f }
        canvas.drawLine(36f, y, 559f, y, linePaint)
        y += 20f

        entries.take(26).forEach { entry ->
            val dateStr = Formatters.formatDate(entry.timestamp, language)
            val typeStr = if (entry.type == KhataType.CREDIT_GIVEN) "GAVE (+)" else "GOT (-)"
            val desc = entry.description.ifBlank { entry.paymentMethod }.take(28)
            canvas.drawText(dateStr, 36f, y, bodyPaint)
            canvas.drawText(typeStr, 150f, y, bodyPaint)
            canvas.drawText(desc, 235f, y, bodyPaint)
            canvas.drawText(Formatters.formatMoney(entry.amount, currency), 559f, y, rightPaint)
            y += 22f
        }

        document.finishPage(page)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun generateReportPdfFile(
        context: Context,
        periodTitle: String,
        totalSales: Double,
        totalCollections: Double,
        totalExpenses: Double,
        netProfit: Double,
        totalDueReceivable: Double,
        prefs: UserPreferences,
        language: AppLanguage
    ): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "BizSphere_Report_${System.currentTimeMillis()}.pdf")

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val currency = prefs.currencySymbol

        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        val greenPaint = Paint().apply { color = Color.parseColor("#06754A") }
        canvas.drawRect(0f, 0f, 595f, 110f, greenPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D9F5E8")
            textSize = 12f
        }
        canvas.drawText("${prefs.businessName} — Financial Report", 36f, 48f, titlePaint)
        canvas.drawText("Period: $periodTitle  |  ${Formatters.formatDateTime(System.currentTimeMillis(), language)}", 36f, 76f, subPaint)

        val rowLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0E1F16")
            textSize = 14f
        }
        val rowValuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#06754A")
            textSize = 15f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val divider = Paint().apply { color = Color.parseColor("#DCEBE2"); strokeWidth = 1f }

        val rows = listOf(
            "Total Sales & Income" to Formatters.formatMoney(totalSales, currency),
            "Khata Collections" to Formatters.formatMoney(totalCollections, currency),
            "Total Expenses" to Formatters.formatMoney(totalExpenses, currency),
            "Net Profit" to Formatters.formatMoney(netProfit, currency),
            "Total Outstanding Receivable" to Formatters.formatMoney(totalDueReceivable, currency)
        )

        var y = 160f
        rows.forEach { (label, value) ->
            canvas.drawText(label, 44f, y, rowLabelPaint)
            canvas.drawText(value, 550f, y, rowValuePaint)
            y += 16f
            canvas.drawLine(44f, y, 550f, y, divider)
            y += 34f
        }

        document.finishPage(page)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }

    fun copyFileToUri(context: Context, sourceFile: File, targetUri: Uri) {
        context.contentResolver.openOutputStream(targetUri)?.use { out ->
            sourceFile.inputStream().use { input ->
                input.copyTo(out)
            }
        }
    }

    fun shareFile(
        context: Context,
        file: File,
        mimeType: String,
        caption: String = "",
        preferWhatsApp: Boolean = false
    ) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val baseIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            if (caption.isNotBlank()) putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (preferWhatsApp) {
            val pm = context.packageManager
            val waPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
            for (pkg in waPackages) {
                val targeted = Intent(baseIntent).setPackage(pkg)
                if (targeted.resolveActivity(pm) != null) {
                    context.startActivity(targeted)
                    return
                }
            }
        }
        context.startActivity(Intent.createChooser(baseIntent, "Share via"))
    }

    fun sendWhatsAppReminder(context: Context, phone: String, message: String) {
        val cleanDigits = phone.filter { it.isDigit() }
        if (cleanDigits.isNotBlank()) {
            val url = "https://wa.me/$cleanDigits?text=${Uri.encode(message)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            try {
                context.startActivity(intent)
                return
            } catch (_: Exception) {
            }
        }
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Send Reminder"))
    }

    fun dialPhone(context: Context, phone: String) {
        if (phone.isBlank()) return
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    }
}
