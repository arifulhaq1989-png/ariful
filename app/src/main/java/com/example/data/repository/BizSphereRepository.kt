package com.example.data.repository

import androidx.room.withTransaction
import com.example.core.BackupCashbookEntry
import com.example.core.BackupCustomer
import com.example.core.BackupInvoice
import com.example.core.BackupInvoiceItem
import com.example.core.BackupKhataEntry
import com.example.core.BackupPayload
import com.example.data.local.BizSphereDatabase
import com.example.data.local.CashbookEntryEntity
import com.example.data.local.CashbookType
import com.example.data.local.CustomerEntity
import com.example.data.local.InvoiceEntity
import com.example.data.local.InvoiceItemEntity
import com.example.data.local.InvoiceWithItems
import com.example.data.local.KhataEntryEntity
import com.example.data.local.KhataType
import com.example.data.local.PaymentMethod
import com.example.data.prefs.UserPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow

class BizSphereRepository(private val db: BizSphereDatabase) {
    private val customerDao = db.customerDao()
    private val khataDao = db.khataDao()
    private val cashbookDao = db.cashbookDao()
    private val invoiceDao = db.invoiceDao()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val backupAdapter = moshi.adapter(BackupPayload::class.java)

    val customersFlow: Flow<List<CustomerEntity>> = customerDao.observeAllCustomers()
    val khataEntriesFlow: Flow<List<KhataEntryEntity>> = khataDao.observeAllEntries()
    val cashbookEntriesFlow: Flow<List<CashbookEntryEntity>> = cashbookDao.observeAllEntries()
    val invoicesFlow: Flow<List<InvoiceWithItems>> = invoiceDao.observeAllInvoicesWithItems()

    suspend fun upsertCustomer(customer: CustomerEntity): Long {
        val now = System.currentTimeMillis()
        return if (customer.id == 0L) {
            customerDao.insertCustomer(customer.copy(createdAt = now, updatedAt = now))
        } else {
            customerDao.updateCustomer(customer.copy(updatedAt = now))
            customer.id
        }
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDao.deleteCustomer(customer)
    }

    suspend fun updateCustomerDueReminder(customerId: Long, dueDate: Long?) {
        val existing = customerDao.getCustomerByIdOnce(customerId) ?: return
        customerDao.updateCustomer(
            existing.copy(dueReminderDate = dueDate, updatedAt = System.currentTimeMillis())
        )
    }

    suspend fun upsertKhataEntry(entry: KhataEntryEntity) {
        db.withTransaction {
            if (entry.id == 0L) {
                khataDao.insertEntry(entry)
            } else {
                khataDao.updateEntry(entry)
            }
            customerDao.getCustomerByIdOnce(entry.customerId)?.let { customer ->
                customerDao.updateCustomer(customer.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    suspend fun deleteKhataEntry(entry: KhataEntryEntity) {
        khataDao.deleteEntry(entry)
    }

    suspend fun upsertCashbookEntry(entry: CashbookEntryEntity) {
        if (entry.id == 0L) {
            cashbookDao.insertEntry(entry)
        } else {
            cashbookDao.updateEntry(entry)
        }
    }

    suspend fun deleteCashbookEntry(entry: CashbookEntryEntity) {
        cashbookDao.deleteEntry(entry)
    }

    suspend fun generateNextInvoiceNumber(): String {
        val count = invoiceDao.getInvoiceCount() + 1
        return "INV-${count.toString().padStart(4, '0')}"
    }

    suspend fun saveInvoice(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>,
        syncToKhataAndSales: Boolean
    ): Long {
        return db.withTransaction {
            val savedId = invoiceDao.insertInvoiceWithItems(invoice, items)
            if (syncToKhataAndSales) {
                val custId = invoice.customerId
                if (custId != null && custId > 0L) {
                    if (invoice.totalAmount > 0.0) {
                        khataDao.insertEntry(
                            KhataEntryEntity(
                                customerId = custId,
                                type = KhataType.CREDIT_GIVEN,
                                amount = invoice.totalAmount,
                                description = "Invoice ${invoice.invoiceNumber}",
                                paymentMethod = PaymentMethod.CASH,
                                timestamp = invoice.timestamp,
                                invoiceId = savedId
                            )
                        )
                    }
                    if (invoice.paidAmount > 0.0) {
                        khataDao.insertEntry(
                            KhataEntryEntity(
                                customerId = custId,
                                type = KhataType.PAYMENT_RECEIVED,
                                amount = invoice.paidAmount,
                                description = "Paid on ${invoice.invoiceNumber}",
                                paymentMethod = PaymentMethod.CASH,
                                timestamp = invoice.timestamp + 1L,
                                invoiceId = savedId
                            )
                        )
                    }
                    customerDao.getCustomerByIdOnce(custId)?.let { customer ->
                        customerDao.updateCustomer(
                            customer.copy(updatedAt = System.currentTimeMillis())
                        )
                    }
                }
                if (invoice.paidAmount > 0.0) {
                    cashbookDao.insertEntry(
                        CashbookEntryEntity(
                            type = CashbookType.INCOME,
                            category = "Sales",
                            amount = invoice.paidAmount,
                            description = "${invoice.invoiceNumber} - ${invoice.customerName}",
                            paymentMethod = PaymentMethod.CASH,
                            timestamp = invoice.timestamp
                        )
                    )
                }
            }
            savedId
        }
    }

    suspend fun deleteInvoice(invoice: InvoiceEntity) {
        db.withTransaction {
            khataDao.deleteEntriesByInvoiceId(invoice.id)
            invoiceDao.deleteInvoice(invoice)
        }
    }

    suspend fun exportBackupJson(prefs: UserPreferences): String {
        val customers = customerDao.getAllCustomersOnce()
        val khata = khataDao.getAllEntriesOnce()
        val cashbook = cashbookDao.getAllEntriesOnce()
        val invoices = invoiceDao.getAllInvoicesWithItemsOnce()

        val payload = BackupPayload(
            version = 1,
            exportedAt = System.currentTimeMillis(),
            businessName = prefs.businessName,
            ownerName = prefs.ownerName,
            businessPhone = prefs.businessPhone,
            businessAddress = prefs.businessAddress,
            currencySymbol = prefs.currencySymbol,
            customers = customers.map {
                BackupCustomer(
                    id = it.id,
                    name = it.name,
                    phone = it.phone,
                    businessName = it.businessName,
                    address = it.address,
                    notes = it.notes,
                    dueReminderDate = it.dueReminderDate,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt
                )
            },
            khataEntries = khata.map {
                BackupKhataEntry(
                    id = it.id,
                    customerId = it.customerId,
                    type = it.type,
                    amount = it.amount,
                    description = it.description,
                    paymentMethod = it.paymentMethod,
                    timestamp = it.timestamp,
                    invoiceId = it.invoiceId
                )
            },
            cashbookEntries = cashbook.map {
                BackupCashbookEntry(
                    id = it.id,
                    type = it.type,
                    category = it.category,
                    amount = it.amount,
                    description = it.description,
                    paymentMethod = it.paymentMethod,
                    timestamp = it.timestamp
                )
            },
            invoices = invoices.map { inv ->
                BackupInvoice(
                    id = inv.invoice.id,
                    invoiceNumber = inv.invoice.invoiceNumber,
                    customerId = inv.invoice.customerId,
                    customerName = inv.invoice.customerName,
                    customerPhone = inv.invoice.customerPhone,
                    subtotal = inv.invoice.subtotal,
                    discount = inv.invoice.discount,
                    taxPercent = inv.invoice.taxPercent,
                    taxAmount = inv.invoice.taxAmount,
                    totalAmount = inv.invoice.totalAmount,
                    paidAmount = inv.invoice.paidAmount,
                    notes = inv.invoice.notes,
                    timestamp = inv.invoice.timestamp,
                    dueDate = inv.invoice.dueDate,
                    items = inv.items.map { item ->
                        BackupInvoiceItem(
                            name = item.name,
                            quantity = item.quantity,
                            unitPrice = item.unitPrice,
                            total = item.total
                        )
                    }
                )
            }
        )
        return backupAdapter.toJson(payload)
    }

    suspend fun importBackupJson(json: String): BackupPayload {
        val payload = backupAdapter.fromJson(json)
            ?: throw IllegalArgumentException("Invalid backup format")
        db.withTransaction {
            invoiceDao.deleteAllInvoices()
            khataDao.deleteAllEntries()
            cashbookDao.deleteAllEntries()
            customerDao.deleteAllCustomers()

            val customerIdMap = mutableMapOf<Long, Long>()
            for (c in payload.customers) {
                val newId = customerDao.insertCustomer(
                    CustomerEntity(
                        name = c.name,
                        phone = c.phone,
                        businessName = c.businessName,
                        address = c.address,
                        notes = c.notes,
                        dueReminderDate = c.dueReminderDate,
                        createdAt = c.createdAt,
                        updatedAt = c.updatedAt
                    )
                )
                customerIdMap[c.id] = newId
            }

            val invoiceIdMap = mutableMapOf<Long, Long>()
            for (inv in payload.invoices) {
                val mappedCustomerId = inv.customerId?.let { customerIdMap[it] }
                val newInvId = invoiceDao.insertInvoiceWithItems(
                    invoice = InvoiceEntity(
                        invoiceNumber = inv.invoiceNumber,
                        customerId = mappedCustomerId,
                        customerName = inv.customerName,
                        customerPhone = inv.customerPhone,
                        subtotal = inv.subtotal,
                        discount = inv.discount,
                        taxPercent = inv.taxPercent,
                        taxAmount = inv.taxAmount,
                        totalAmount = inv.totalAmount,
                        paidAmount = inv.paidAmount,
                        notes = inv.notes,
                        timestamp = inv.timestamp,
                        dueDate = inv.dueDate
                    ),
                    items = inv.items.map { item ->
                        InvoiceItemEntity(
                            invoiceId = 0L,
                            name = item.name,
                            quantity = item.quantity,
                            unitPrice = item.unitPrice,
                            total = item.total
                        )
                    }
                )
                invoiceIdMap[inv.id] = newInvId
            }

            for (k in payload.khataEntries) {
                val mappedCustomerId = customerIdMap[k.customerId] ?: continue
                khataDao.insertEntry(
                    KhataEntryEntity(
                        customerId = mappedCustomerId,
                        type = k.type,
                        amount = k.amount,
                        description = k.description,
                        paymentMethod = k.paymentMethod,
                        timestamp = k.timestamp,
                        invoiceId = k.invoiceId?.let { invoiceIdMap[it] }
                    )
                )
            }

            for (cb in payload.cashbookEntries) {
                cashbookDao.insertEntry(
                    CashbookEntryEntity(
                        type = cb.type,
                        category = cb.category,
                        amount = cb.amount,
                        description = cb.description,
                        paymentMethod = cb.paymentMethod,
                        timestamp = cb.timestamp
                    )
                )
            }
        }
        return payload
    }

    suspend fun clearAllData() {
        db.withTransaction {
            invoiceDao.deleteAllInvoices()
            khataDao.deleteAllEntries()
            cashbookDao.deleteAllEntries()
            customerDao.deleteAllCustomers()
        }
    }

    suspend fun seedDemoData() {
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        db.withTransaction {
            val c1 = customerDao.insertCustomer(
                CustomerEntity(
                    name = "Rahim Traders",
                    phone = "+8801711223344",
                    businessName = "Rahim Enterprise",
                    address = "Motijheel, Dhaka",
                    notes = "Wholesale regular buyer",
                    dueReminderDate = now + 2 * day,
                    createdAt = now - 6 * day,
                    updatedAt = now - day
                )
            )
            val c2 = customerDao.insertCustomer(
                CustomerEntity(
                    name = "Karim General Store",
                    phone = "+8801819887766",
                    businessName = "Karim Mart",
                    address = "Chawkbazar, Dhaka",
                    dueReminderDate = now + 5 * day,
                    createdAt = now - 5 * day,
                    updatedAt = now - 2 * day
                )
            )
            val c3 = customerDao.insertCustomer(
                CustomerEntity(
                    name = "Nusrat Fashion House",
                    phone = "+8801912345678",
                    businessName = "Nusrat Boutique",
                    address = "Uttara Sector 7",
                    createdAt = now - 4 * day,
                    updatedAt = now
                )
            )

            khataDao.insertEntry(
                KhataEntryEntity(
                    customerId = c1,
                    type = KhataType.CREDIT_GIVEN,
                    amount = 14500.0,
                    description = "Bulk rice & oil supply",
                    paymentMethod = PaymentMethod.CASH,
                    timestamp = now - 5 * day
                )
            )
            khataDao.insertEntry(
                KhataEntryEntity(
                    customerId = c1,
                    type = KhataType.PAYMENT_RECEIVED,
                    amount = 6000.0,
                    description = "Partial bKash transfer",
                    paymentMethod = PaymentMethod.BKASH,
                    timestamp = now - 2 * day
                )
            )
            khataDao.insertEntry(
                KhataEntryEntity(
                    customerId = c2,
                    type = KhataType.CREDIT_GIVEN,
                    amount = 5200.0,
                    description = "Monthly grocery stock",
                    paymentMethod = PaymentMethod.CASH,
                    timestamp = now - 3 * day
                )
            )
            khataDao.insertEntry(
                KhataEntryEntity(
                    customerId = c3,
                    type = KhataType.PAYMENT_RECEIVED,
                    amount = 3000.0,
                    description = "Advance deposit for order",
                    paymentMethod = PaymentMethod.NAGAD,
                    timestamp = now - day
                )
            )

            cashbookDao.insertEntry(
                CashbookEntryEntity(
                    type = CashbookType.INCOME,
                    category = "Sales",
                    amount = 18500.0,
                    description = "Counter cash sales",
                    paymentMethod = PaymentMethod.CASH,
                    timestamp = now - 4 * day
                )
            )
            cashbookDao.insertEntry(
                CashbookEntryEntity(
                    type = CashbookType.EXPENSE,
                    category = "Stock Purchase",
                    amount = 9800.0,
                    description = "Supplier restock",
                    paymentMethod = PaymentMethod.BANK,
                    timestamp = now - 3 * day
                )
            )
            cashbookDao.insertEntry(
                CashbookEntryEntity(
                    type = CashbookType.INCOME,
                    category = "Sales",
                    amount = 12400.0,
                    description = "Afternoon retail sales",
                    paymentMethod = PaymentMethod.BKASH,
                    timestamp = now - day
                )
            )
            cashbookDao.insertEntry(
                CashbookEntryEntity(
                    type = CashbookType.EXPENSE,
                    category = "Utilities",
                    amount = 2150.0,
                    description = "Shop electricity & internet",
                    paymentMethod = PaymentMethod.NAGAD,
                    timestamp = now
                )
            )

            invoiceDao.insertInvoiceWithItems(
                invoice = InvoiceEntity(
                    invoiceNumber = "INV-0001",
                    customerId = c1,
                    customerName = "Rahim Traders",
                    customerPhone = "+8801711223344",
                    subtotal = 14500.0,
                    discount = 0.0,
                    taxPercent = 0.0,
                    taxAmount = 0.0,
                    totalAmount = 14500.0,
                    paidAmount = 6000.0,
                    notes = "Thank you for your business!",
                    timestamp = now - 5 * day
                ),
                items = listOf(
                    InvoiceItemEntity(0L, 0L, "Miniket Rice (50kg)", 2.0, 3850.0, 7700.0),
                    InvoiceItemEntity(0L, 0L, "Soybean Oil (5L)", 8.0, 850.0, 6800.0)
                )
            )
        }
    }
}
