package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

object KhataType {
    const val CREDIT_GIVEN = "CREDIT_GIVEN"       // Customer owes business (বাকি দিলাম)
    const val PAYMENT_RECEIVED = "PAYMENT_RECEIVED" // Customer paid business (জমা পেলাম)
}

object CashbookType {
    const val INCOME = "INCOME"
    const val EXPENSE = "EXPENSE"
}

object PaymentMethod {
    const val CASH = "Cash"
    const val BKASH = "bKash"
    const val NAGAD = "Nagad"
    const val BANK = "Bank"
    const val CARD = "Card"

    val all = listOf(CASH, BKASH, NAGAD, BANK, CARD)
}

object CashbookCategories {
    val income = listOf("Sales", "Service", "Commission", "Collection", "Other Income")
    val expense = listOf("Stock Purchase", "Rent", "Salary", "Utilities", "Transport", "Marketing", "Other Expense")

    fun localized(category: String, isBengali: Boolean): String {
        if (!isBengali) return category
        return when (category) {
            "Sales" -> "বিক্রি"
            "Service" -> "সার্ভিস"
            "Commission" -> "কমিশন"
            "Collection" -> "আদায়"
            "Other Income" -> "অন্যান্য আয়"
            "Stock Purchase" -> "মাল ক্রয়"
            "Rent" -> "দোকান ভাড়া"
            "Salary" -> "কর্মচারী বেতন"
            "Utilities" -> "বিদ্যুৎ ও বিল"
            "Transport" -> "পরিবহন"
            "Marketing" -> "প্রচারণা"
            "Other Expense" -> "অন্যান্য ব্যয়"
            else -> category
        }
    }
}

/**
 * Customer entity representing a business customer in the Digital Khata.
 */
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val businessName: String = "",
    val address: String = "",
    val notes: String = "",
    val dueReminderDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

typealias Customer = CustomerEntity

/**
 * Ledger entry entity (Digital Khata) linked to a Customer via foreign key.
 */
@Entity(
    tableName = "khata_entries",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId"), Index("timestamp")]
)
data class KhataEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val type: String, // KhataType.CREDIT_GIVEN or PAYMENT_RECEIVED
    val amount: Double,
    val description: String = "",
    val paymentMethod: String = PaymentMethod.CASH,
    val timestamp: Long = System.currentTimeMillis(),
    val invoiceId: Long? = null
)

typealias LedgerEntity = KhataEntryEntity
typealias Ledger = KhataEntryEntity

/**
 * Business Transaction entity (Cashbook Income & Expense).
 */
@Entity(
    tableName = "cashbook_entries",
    indices = [Index("timestamp"), Index("type")]
)
data class CashbookEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // CashbookType.INCOME or EXPENSE
    val category: String,
    val amount: Double,
    val description: String = "",
    val paymentMethod: String = PaymentMethod.CASH,
    val timestamp: Long = System.currentTimeMillis()
)

typealias TransactionEntity = CashbookEntryEntity
typealias BusinessTransaction = CashbookEntryEntity

data class CustomerWithLedger(
    @Embedded val customer: CustomerEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val ledgerEntries: List<KhataEntryEntity>
)

@Entity(
    tableName = "invoices",
    indices = [Index("timestamp"), Index("customerId")]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String,
    val customerPhone: String = "",
    val subtotal: Double,
    val discount: Double = 0.0,
    val taxPercent: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double,
    val paidAmount: Double = 0.0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val dueDate: Long? = null
) {
    val balanceDue: Double
        get() = (totalAmount - paidAmount).coerceAtLeast(0.0)
}

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("invoiceId")]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val name: String,
    val quantity: Double,
    val unitPrice: Double,
    val total: Double
)

data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItemEntity>
)
