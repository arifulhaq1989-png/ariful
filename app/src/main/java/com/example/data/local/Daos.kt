package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    fun observeAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    suspend fun getAllCustomersOnce(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :id")
    fun observeCustomerById(id: Long): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerByIdOnce(id: Long): CustomerEntity?

    @Transaction
    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun observeCustomerWithLedger(customerId: Long): Flow<CustomerWithLedger?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Long)

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()
}

@Dao
interface LedgerDao {
    @Query("SELECT * FROM khata_entries ORDER BY timestamp DESC")
    fun observeAllEntries(): Flow<List<KhataEntryEntity>>

    @Query("SELECT * FROM khata_entries ORDER BY timestamp DESC")
    suspend fun getAllEntriesOnce(): List<KhataEntryEntity>

    @Query("SELECT * FROM khata_entries WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun observeEntriesForCustomer(customerId: Long): Flow<List<KhataEntryEntity>>

    @Query("SELECT * FROM khata_entries WHERE id = :id")
    suspend fun getEntryByIdOnce(id: Long): KhataEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: KhataEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: KhataEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: KhataEntryEntity)

    @Query("DELETE FROM khata_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM khata_entries WHERE invoiceId = :invoiceId")
    suspend fun deleteEntriesByInvoiceId(invoiceId: Long)

    @Query("DELETE FROM khata_entries")
    suspend fun deleteAllEntries()
}

typealias KhataDao = LedgerDao

@Dao
interface TransactionDao {
    @Query("SELECT * FROM cashbook_entries ORDER BY timestamp DESC")
    fun observeAllEntries(): Flow<List<CashbookEntryEntity>>

    @Query("SELECT * FROM cashbook_entries WHERE type = :type ORDER BY timestamp DESC")
    fun observeEntriesByType(type: String): Flow<List<CashbookEntryEntity>>

    @Query("SELECT * FROM cashbook_entries WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun observeEntriesInRange(startTime: Long, endTime: Long): Flow<List<CashbookEntryEntity>>

    @Query("SELECT * FROM cashbook_entries ORDER BY timestamp DESC")
    suspend fun getAllEntriesOnce(): List<CashbookEntryEntity>

    @Query("SELECT * FROM cashbook_entries WHERE id = :id")
    suspend fun getEntryByIdOnce(id: Long): CashbookEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: CashbookEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: CashbookEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: CashbookEntryEntity)

    @Query("DELETE FROM cashbook_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM cashbook_entries")
    suspend fun deleteAllEntries()
}

typealias CashbookDao = TransactionDao

@Dao
interface InvoiceDao {
    @Transaction
    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    fun observeAllInvoicesWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    suspend fun getAllInvoicesWithItemsOnce(): List<InvoiceWithItems>

    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun getInvoiceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InvoiceItemEntity>)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)

    @Query("DELETE FROM invoices")
    suspend fun deleteAllInvoices()

    @Transaction
    suspend fun insertInvoiceWithItems(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): Long {
        val id = insertInvoice(invoice)
        if (items.isNotEmpty()) {
            insertItems(items.map { it.copy(invoiceId = id) })
        }
        return id
    }
}
