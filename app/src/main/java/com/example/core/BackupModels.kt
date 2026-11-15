package com.example.core

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupPayload(
    val version: Int = 1,
    val exportedAt: Long,
    val businessName: String,
    val ownerName: String,
    val businessPhone: String,
    val businessAddress: String,
    val currencySymbol: String,
    val customers: List<BackupCustomer>,
    val khataEntries: List<BackupKhataEntry>,
    val cashbookEntries: List<BackupCashbookEntry>,
    val invoices: List<BackupInvoice>
)

@JsonClass(generateAdapter = true)
data class BackupCustomer(
    val id: Long,
    val name: String,
    val phone: String,
    val businessName: String,
    val address: String,
    val notes: String,
    val dueReminderDate: Long?,
    val createdAt: Long,
    val updatedAt: Long
)

@JsonClass(generateAdapter = true)
data class BackupKhataEntry(
    val id: Long,
    val customerId: Long,
    val type: String,
    val amount: Double,
    val description: String,
    val paymentMethod: String,
    val timestamp: Long,
    val invoiceId: Long?
)

@JsonClass(generateAdapter = true)
data class BackupCashbookEntry(
    val id: Long,
    val type: String,
    val category: String,
    val amount: Double,
    val description: String,
    val paymentMethod: String,
    val timestamp: Long
)

@JsonClass(generateAdapter = true)
data class BackupInvoice(
    val id: Long,
    val invoiceNumber: String,
    val customerId: Long?,
    val customerName: String,
    val customerPhone: String,
    val subtotal: Double,
    val discount: Double,
    val taxPercent: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val paidAmount: Double,
    val notes: String,
    val timestamp: Long,
    val dueDate: Long?,
    val items: List<BackupInvoiceItem>
)

@JsonClass(generateAdapter = true)
data class BackupInvoiceItem(
    val name: String,
    val quantity: Double,
    val unitPrice: Double,
    val total: Double
)
