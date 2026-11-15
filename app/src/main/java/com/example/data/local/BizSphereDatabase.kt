package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CustomerEntity::class,
        KhataEntryEntity::class,
        CashbookEntryEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BizSphereDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun ledgerDao(): LedgerDao
    abstract fun transactionDao(): TransactionDao
    abstract fun invoiceDao(): InvoiceDao

    fun khataDao(): LedgerDao = ledgerDao()
    fun cashbookDao(): TransactionDao = transactionDao()

    companion object {
        @Volatile
        private var INSTANCE: BizSphereDatabase? = null

        fun getInstance(context: Context): BizSphereDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BizSphereDatabase::class.java,
                    "bizsphere_khata.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
